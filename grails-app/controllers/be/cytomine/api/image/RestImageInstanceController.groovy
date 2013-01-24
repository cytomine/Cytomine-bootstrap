package be.cytomine.api.image

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.SecurityCheck
import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LinearRing
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import ij.ImagePlus
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

import be.cytomine.ontology.*

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Controller that handle request for project images.
 * TODO:: doc/test/coverage this controller!
 * TODOSTEVBEN: clean unused method + doc method
 */
class RestImageInstanceController extends RestController {

    def segmentationService
    def imageProcessingService
    def imageInstanceService
    def projectService
    def abstractImageService

    def show = {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("ImageInstance", params.id)
        }
    }

    def listByProject = {
        Project project = projectService.read(params.long('id'))
        if (project && params.inf && params.sup) {
            responseSuccess(imageInstanceService.list(project, Integer.parseInt(params.inf), Integer.parseInt(params.sup)))
        }
        else if (project && params.tree && Boolean.parseBoolean(params.tree))  {
            responseSuccess(imageInstanceService.listTree(project))
        }
        else if (project) {
            responseSuccess(imageInstanceService.list(project))
        }
        else {
            responseNotFound("ImageInstance", "Project", params.id)
        }
    }


    def add = {
        try {
            responseResult(imageInstanceService.add(request.JSON, new SecurityCheck()))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        } catch (AccessDeniedException e) {
             log.error(e)
             response([success: false, errors: e], 403)
        }
    }

    def update = {
        update(imageInstanceService, request.JSON)
    }

    def delete = {
        delete(imageInstanceService, JSON.parse("{id : $params.id}"))
    }

    def window = {
        //TODO:: document this method
        ImageInstance image = ImageInstance.read(params.long('id'))
        AbstractImage abstractImage = image.getBaseImage()
        int x = Integer.parseInt(params.x)
        int y = abstractImage.getHeight() - Integer.parseInt(params.y)
        int w = Integer.parseInt(params.w)
        int h = Integer.parseInt(params.h)
        int maxZoom = abstractImage.getZoomLevels().max
        int zoom = (params.zoom != null && params.zoom != "") ? Math.max(Math.min(Integer.parseInt(params.zoom), maxZoom), 0) : 0
        int resizeWidth = w / Math.pow(2, zoom)
        int resizeHeight = h / Math.pow(2, zoom)
        try {
            String url = abstractImage.getCropURL(x, y, w, h)
            BufferedImage bufferedImage = getImageFromURL(url)
            //Resize image here, scaling with IIP return "strange" results. Mail was send to the creator of the project
            if (resizeWidth != w || resizeHeight != h ) {
                BufferedImage resizedImage = new BufferedImage(resizeWidth, resizeHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resizedImage.createGraphics();
                g.drawImage(bufferedImage, 0, 0, resizeWidth, resizeHeight, null);
                g.dispose();
                bufferedImage = resizedImage
            }

            responseBufferedImage(bufferedImage)
        } catch (Exception e) {
            log.error("GetThumb:" + e);
        }
    }

    def cropGeometry = {
        //TODO:: document this method
        String geometrySTR = params.geometry
        def geometry = new WKTReader().read(geometrySTR)
        def annotation = new UserAnnotation(location: geometry)
        annotation.image = ImageInstance.read(params.id)
        responseImage(abstractImageService.crop(annotation, null))
    }

    def mask = {
        //TODO:: document this method
        ImageInstance image = ImageInstance.read(params.long('id'))
        AbstractImage abstractImage = image.getBaseImage()
        int x = Integer.parseInt(params.x)
        int y = Integer.parseInt(params.y)
        int w = Integer.parseInt(params.w)
        int h = Integer.parseInt(params.h)
        int termID = Integer.parseInt(params.term)

        try {
            //Get the image, compute ratio between asked and received
            String url = abstractImage.getCropURL(x, abstractImage.getHeight() - y, w, h)
            BufferedImage window = getImageFromURL(url)
            BufferedImage mask = new BufferedImage(window.getWidth(),window.getHeight(),BufferedImage.TYPE_INT_RGB)
            double x_ratio = window.getWidth() / w
            double y_ratio = window.getHeight() / h

            //Fetch annotations with the requested term on the request image
            Term term = Term.read(termID)

            //Create a geometry corresponding to the ROI of the request (x,y,w,h)
            //1. Compute points
            Coordinate[] roiPoints = new Coordinate[5]
            roiPoints[0] = new Coordinate(x, abstractImage.getHeight() - y)
            roiPoints[1] = new Coordinate(x + w, abstractImage.getHeight() - y)
            roiPoints[2] = new Coordinate(x + w, abstractImage.getHeight() - (y + h))
            roiPoints[3] = new Coordinate(x, abstractImage.getHeight() - (y + h))
            roiPoints[4] = roiPoints[0]
            //Build geometry
            LinearRing linearRing = new GeometryFactory().createLinearRing(roiPoints)
            Geometry roiGeometry = new GeometryFactory().createPolygon(linearRing)

            Collection<UserAnnotation> annotations = []
            Collection<UserAnnotation> annotations_in_roi = UserAnnotation.createCriteria()
                    .add(Restrictions.eq("image", image))
                    .add(SpatialRestrictions.within("location",roiGeometry))
                    .list()

            if (!annotations_in_roi.isEmpty()) {
                annotations = (Collection<UserAnnotation>) AnnotationTerm.createCriteria().list {
                    inList("term", [term])
                    join("userAnnotation")
                    createAlias("userAnnotation", "a")
                    projections {
                        inList("a.id", annotations_in_roi.collect{it.id})
                        groupProperty("userAnnotation")
                    }
                }
            }

            mask = segmentationService.colorizeWindow(abstractImage, mask, annotations.collect{it.getLocation()}, Color.decode(term.color), x, y, x_ratio, y_ratio)
            responseBufferedImage(mask)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    private BufferedImage getMaskImage(AnnotationDomain annotation, Term term, Integer zoom, Boolean withAlpha) {
        //TODO:: document this method
        BufferedImage crop = getImageFromURL(abstractImageService.crop(annotation, zoom))
        BufferedImage mask = new BufferedImage(crop.getWidth(),crop.getHeight(),BufferedImage.TYPE_INT_ARGB);
        AbstractImage abstractImage = annotation.getImage().getBaseImage()
        Collection<Geometry> geometries = new LinkedList<Geometry>()

        geometries.add(annotation.getLocation())
        def boundaries = annotation.getBoundaries()
        double x_ratio = crop.getWidth() / boundaries.width
        double y_ratio = crop.getHeight() / boundaries.height

        mask = segmentationService.colorizeWindow(abstractImage, mask, geometries, Color.decode(term.color), boundaries.topLeftX, abstractImage.getHeight() - boundaries.topLeftY, x_ratio, y_ratio)

        if (withAlpha)
            return applyMaskToAlpha(crop, mask)
        else
            return mask
    }

    def alphamaskUserAnnotation = {
        try {
            def annotation = UserAnnotation.read(params.annotation)
            def cropURL = alphamask(annotation,params)
            if(cropURL!=null) responseBufferedImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    def alphamaskAlgoAnnotation = {
        try {
            def annotation = AlgoAnnotation.read(params.annotation)
            def cropURL = alphamask(annotation,params)
            responseBufferedImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    def alphamaskReviewedAnnotation = {
        try {
            def annotation = ReviewedAnnotation.read(params.annotation)
            def cropURL = alphamask(annotation,params)
            responseBufferedImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    private def alphamask(AnnotationDomain annotation, def params) {
        //TODO:: document this method
        if (!annotation) {
            responseNotFound("Annotation", params.annotation)
        }
        Term term = Term.read(params.term)
        if (!term) {
            responseNotFound("Term", params.term)
        }
        if (!annotation.termsId().contains(term.id)) {
            response([ error : "Term not associated with annotation", annotation : annotation.id, term : term.id])
        }
        Integer zoom = null
        if (params.zoom != null) zoom = Integer.parseInt(params.zoom)
        if (annotation == null) {
            responseNotFound("Crop", "Annotation", params.annotation)
        }

        def zoomMinMax = annotation.getImage().getBaseImage().getZoomLevels()
        if ((params.zoom != null) && (zoom > zoomMinMax.max)) {
            zoom = zoomMinMax.max
        } else if ((params.zoom != null) && (zoom < zoomMinMax.min)) {
            zoom = zoomMinMax.min
        }

        println "zoom=$zoom"
        println "zoomMinMax=$zoomMinMax"

        try {
            return getMaskImage(annotation, term, zoom, true)
        } catch (Exception e) {
            log.error("GetThumb:" + e);
        }
        return null;
    }

    def cropmask = {
        //TODO:: document this method
        UserAnnotation annotation = UserAnnotation.read(params.annotation)
        if (!annotation) {
            responseNotFound("Annotation", params.annotation)
        }
        Term term = Term.read(params.term)
        if (!term) {
            responseNotFound("Term", params.term)
        }
        if (!annotation.termsId().contains(term.id)) {
            response([ error : "Term not associated with userAnnotation", annotation : annotation.id, term : term.id])
        }
        Integer zoom = null
        if (params.zoom != null) zoom = Integer.parseInt(params.zoom)
        log.info "zoom====$zoom"
        if (annotation == null)
            responseNotFound("Crop", "Annotation", params.annotation)
        else if ((params.zoom != null) && (zoom < annotation.getImage().getBaseImage().getZoomLevels().min || zoom > annotation.getImage().getBaseImage().getZoomLevels().max))
            responseNotFound("Crop", "Zoom", zoom)
        else {
            try {
                responseBufferedImage(getMaskImage(annotation, term, zoom, false))
            } catch (Exception e) {
                log.error("GetThumb:" + e);
            }
        }

    }

    private BufferedImage applyMaskToAlpha(BufferedImage image, BufferedImage mask) {
        //TODO:: document this method
        int width = image.getWidth()
        int height = image.getHeight()
        int[] imagePixels = image.getRGB(0, 0, width, height, null, 0, width)
        int[] maskPixels = mask.getRGB(0, 0, width, height, null, 0, width)
        int black_rgb = Color.BLACK.getRGB()
        for (int i = 0; i < imagePixels.length; i++)
        {
            int color = imagePixels[i] & 0x00FFFFFF; // mask away any alpha present
            int alphaValue = (maskPixels[i] == black_rgb) ? 0x00 : 0xFF
            int maskColor = alphaValue << 24 // shift value into alpha bits
            imagePixels[i] = color | maskColor
        }
        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        combined.setRGB(0, 0, width, height, imagePixels, 0, width)
        return combined
    }




    def putMask = {
        //TODO:: document this method
        //Load request attachment
        MultipartFile uploadedFile = ((MultipartHttpServletRequest)request).getFile('mask')
        ImagePlus original = new ImagePlus("ori", ImageIO.read ( new ByteArrayInputStream ( uploadedFile.getBytes() )))
        ImagePlus copy = new ImagePlus("copy", ImageIO.read ( new ByteArrayInputStream ( uploadedFile.getBytes() )))

        //Extract params
        double scale = Integer.parseInt(params.oriwidth) / original.getWidth()

        // Get polygons
        Collection<Coordinate[]> components = imageProcessingService.getConnectedComponents(original, copy, 50)
        String[] polygons = new String[components.size()]
        int i = 0
        components.each { coordinates ->
            coordinates.each { coordinate ->
                coordinate.y = original.getHeight() - coordinate.y
                coordinate.x = coordinate.x * scale
                coordinate.y = coordinate.y * scale

            }
            polygons[i] = imageProcessingService.getWKTPolygon(coordinates)
            i++
        }
        response(["polygons" : polygons])
    }

}
