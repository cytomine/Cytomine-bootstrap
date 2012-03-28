package be.cytomine.api.image

import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.project.Project

import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.LinearRing
import grails.converters.JSON
import java.awt.Color
import java.awt.image.BufferedImage
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.test.Infos
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.MultipartFile
import javax.imageio.ImageIO
import ij.ImagePlus

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
        Project project = projectService.read(params.long('idproject'))
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
        if (project && !params.dataTables) responseSuccess(imageInstanceService.list(project))
        if (project && params.dataTables) responseSuccess(imageInstanceService.listDatatables(project))
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
        def json = JSON.parse("{project : $params.idproject, image : $params.idimage}")
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
        println "WINDOW REQUEST " + params.toString()
        ImageInstance image = ImageInstance.read(params.long('id'))
        AbstractImage abstractImage = image.getBaseImage()
        int x = Integer.parseInt(params.x)
        int y = abstractImage.getHeight() - Integer.parseInt(params.y)
        int w = Integer.parseInt(params.w)
        int h = Integer.parseInt(params.h)
        try {
            String url = abstractImage.getCropURL(x, y, w, h)
            log.info("Window : " + url)
            responseImage(url)
        } catch (Exception e) {
            log.error("GetThumb:" + e);
        }
    }

    def mask = {
        println "WINDOW REQUEST " + params.toString()
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

            Collection<Annotation> annotations = (Collection<Annotation>) AnnotationTerm.createCriteria().list {
                inList("term", [term])
                join("annotation")
                createAlias("annotation", "a")
                projections {
                    inList("a.image", [image])
                    groupProperty("annotation")
                }
            }
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
            //Filter annotation which intersects the ROI
            Collection<Geometry> intersectGeometries = new LinkedList<Geometry>()
            annotations.each { annotation ->
                if (roiGeometry.intersects(annotation.getLocation())) {
                    intersectGeometries.add(annotation.getLocation())
                }
            }
            mask = segmentationService.colorizeWindow(abstractImage, mask, intersectGeometries, Color.decode(term.color), x, y, x_ratio, y_ratio)
            responseBufferedImage(mask)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    def cropmask = {
        Annotation annotation = Annotation.read(params.annotation)
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
        def zoom
        if (params.zoom != null) zoom = Integer.parseInt(params.zoom)
        if (annotation == null)
            responseNotFound("Crop", "Annotation", params.annotation)
        else if ((params.zoom != null) && (zoom < annotation.getImage().getBaseImage().getZoomLevels().min || zoom > annotation.getImage().getBaseImage().getZoomLevels().max))
            responseNotFound("Crop", "Zoom", zoom)
        else {
            try {
                BufferedImage crop = getImageFromURL(abstractImageService.crop(annotation, zoom))
                BufferedImage window = new BufferedImage(crop.getWidth(),crop.getHeight(),BufferedImage.TYPE_INT_ARGB);
                AbstractImage abstractImage = annotation.getImage().getBaseImage()
                Collection<Geometry> geometries = new LinkedList<Geometry>()
                geometries.add(annotation.getLocation())
                def boundaries = annotation.getBoundaries()
                double x_ratio = crop.getWidth() / boundaries.width
                double y_ratio = crop.getHeight() / boundaries.height
                window = segmentationService.colorizeWindow(abstractImage, window, geometries, Color.decode(term.color), boundaries.topLeftX, abstractImage.getHeight() - boundaries.topLeftY, x_ratio, y_ratio)
                responseBufferedImage(window)
            } catch (Exception e) {
                log.error("GetThumb:" + e);
            }
        }
    }



    def putMask = {
        //Load request attachment
        MultipartFile uploadedFile = ((MultipartHttpServletRequest)request).getFile('mask')
        ImagePlus original = new ImagePlus("ori", ImageIO.read ( new ByteArrayInputStream ( uploadedFile.getBytes() )))
        ImagePlus copy = new ImagePlus("copy", ImageIO.read ( new ByteArrayInputStream ( uploadedFile.getBytes() )))

        //Extract params
        double scale = Integer.parseInt(params.w) / original.getWidth()

        // Get polygons
        Collection<Coordinate[]> components = imageProcessingService.getConnectedComponents(original, copy, 500)
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
