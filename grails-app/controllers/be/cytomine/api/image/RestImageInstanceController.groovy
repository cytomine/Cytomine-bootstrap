package be.cytomine.api.image

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.test.Infos
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LinearRing
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import ij.ImagePlus
import org.hibernate.criterion.Restrictions
import org.hibernatespatial.criterion.SpatialRestrictions
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import be.cytomine.AnnotationDomain
import be.cytomine.ontology.UserAnnotation
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 */

class RestImageInstanceController extends RestController {

    def segmentationService
    def imageProcessingService
    def imageInstanceService
    def projectService
    def abstractImageService
    def userService

    def show = {
        log.info "show"
        ImageInstance image = imageInstanceService.read(params.long('id'))

        if (image) {
            imageInstanceService.checkAuthorization(image.project)
            responseSuccess(image)
        }
        else responseNotFound("ImageInstance", params.id)
    }

    def showByProjectAndImage = {
        Project project = projectService.read(params.long('idproject'), new Project())
        AbstractImage image = abstractImageService.read(params.long('idimage'))
        ImageInstance imageInstance = imageInstanceService.get(project, image)
        if (imageInstance) {
            imageInstanceService.checkAuthorization(project)
            responseSuccess(imageInstance)
        }
        else responseNotFound("ImageInstance", "Project", params.idproject, "Image", params.idimage)
    }

    def listByProject = {
        Project project = projectService.read(params.long('id'), new Project())
        if (project && params.dataTables) responseSuccess(imageInstanceService.listDatatables(project))
        else if (project && params.inf && params.sup) responseSuccess(imageInstanceService.list(project, Integer.parseInt(params.inf), Integer.parseInt(params.sup)))
        else if (project && params.tree && Boolean.parseBoolean(params.tree))  responseSuccess(imageInstanceService.listTree(project))
        else if (project) responseSuccess(imageInstanceService.list(project))
        else responseNotFound("ImageInstance", "Project", params.id)
    }

    def add = {
        try {
            def json = request.JSON
            if(!json.project || !Project.read(json.project)) throw new WrongArgumentException("Image Instance must have a valide project:"+json.project)
            imageInstanceService.checkAuthorization(Long.parseLong(json.project.toString()), new Project())
            log.debug("add")
            def result = imageInstanceService.add(json)
            log.debug("result")
            responseResult(result)
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }


    def update = {
        def json = request.JSON
        try {
            def domain = imageInstanceService.retrieve(json)
            try {Infos.printRight(domain?.project) } catch(Exception e) {log.info e}
            imageInstanceService.checkAuthorization(domain?.project)
            def result = imageInstanceService.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def delete = {
        def json = JSON.parse("{id : $params.id}")
        log.info "delete image instance:" + json.toString()
        try {
            log.info "retrieve domain"
            def domain = imageInstanceService.retrieve(json)
            log.info "checkAuthorization"
            imageInstanceService.checkAuthorization(domain?.project)
            log.info "delete"
            def result = imageInstanceService.delete(domain,json)
            log.info "responseResult"
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def window = {
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
        String geometrySTR = params.geometry
        def geometry = new WKTReader().read(geometrySTR)
        def annotation = new UserAnnotation(location: geometry)
        annotation.image = ImageInstance.read(params.id)
        responseImage(abstractImageService.crop(annotation, null))
    }

    def mask = {
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
            println "alphamaskReviewedAnnotation"
            def annotation = ReviewedAnnotation.read(params.annotation)
            println "alphamaskReviewedAnnotation"
            println "params.id="+params.annotation
            def cropURL = alphamask(annotation,params)
            responseBufferedImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    private def alphamask(AnnotationDomain annotation, def params) {
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

    private BufferedImage applyMaskToAlpha(BufferedImage image, BufferedImage mask)
    {
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
