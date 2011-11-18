package be.cytomine.api

import be.cytomine.image.ImageInstance
import be.cytomine.security.User
import be.cytomine.image.AbstractImage
import grails.converters.*

import be.cytomine.command.Command

import be.cytomine.project.Project

import be.cytomine.api.RestController
import be.cytomine.command.imageinstance.AddImageInstanceCommand
import be.cytomine.command.imageinstance.EditImageInstanceCommand
import be.cytomine.command.imageinstance.DeleteImageInstanceCommand
import be.cytomine.ontology.Annotation

import be.cytomine.ontology.AnnotationTerm
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand

import be.cytomine.command.TransactionController
import be.cytomine.command.annotation.DeleteAnnotationCommand
import java.awt.image.BufferedImage
import be.cytomine.ontology.Term
import java.awt.Color
import com.vividsolutions.jts.geom.Coordinate
import com.vividsolutions.jts.geom.GeometryFactory
import com.vividsolutions.jts.geom.Geometry
import com.vividsolutions.jts.geom.LinearRing
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.command.suggestedTerm.DeleteSuggestedTermCommand
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 9:40
 * To change this template use File | Settings | File Templates.
 */
class RestImageInstanceController extends RestController {

    def springSecurityService
    def transactionService
    def segmentationService

    def index = {
        redirect(controller: "image")
    }
    def list = {
        response(ImageInstance.list())
    }

    def show = {
        ImageInstance image = ImageInstance.read(params.id)
        if(image!=null) responseSuccess(image)
        else responseNotFound("ImageInstance",params.id)
    }


    def showByProjectAndImage = {
        Project project = Project.read(params.idproject)
        AbstractImage image = AbstractImage.read(params.idimage)
        ImageInstance imageInstance = ImageInstance.findByBaseImageAndProject(image,project)
        if(imageInstance!=null) responseSuccess(imageInstance)
        else responseNotFound("ImageInstance","Project",params.idproject,"Image",params.idimage)
    }

    def listByUser = {
        User user = User.read(params.id)
        if(user!=null) responseSuccess(ImageInstance.findAllByUser(user))
        else responseNotFound("ImageInstance","User",params.id)
    }

    def listByImage = {
        AbstractImage image = AbstractImage.read(params.id)
        if(image!=null) responseSuccess(ImageInstance.findAllByBaseImage(image))
        else responseNotFound("ImageInstance","AbstractImage",params.id)
    }

    def listByProject = {
        Project project = Project.read(params.id)
        def images = ImageInstance.createCriteria().list {
            createAlias("slide", "s")
            eq("project", project)
            order("slide")
            order("s.index", "asc")
        }

        if(project!=null) responseSuccess(images)
        else responseNotFound("ImageInstance","Project",params.id)
    }

    def add = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result
        synchronized(this.getClass()) {
            result = processCommand(new AddImageInstanceCommand(user: currentUser), request.JSON)
        }
        response(result)
    }

    def update = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new EditImageInstanceCommand(user: currentUser), request.JSON)
        response(result)
    }

    def delete = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        Project project = Project.read(params.idproject)
        AbstractImage image = AbstractImage.read(params.idimage)
        ImageInstance imageInstance = ImageInstance.findByBaseImageAndProject(image,project)
        if(!imageInstance)
        {
            responseNotFound("ImageInstance","Project",params.idproject,"Image",params.idimage)
            return
        }
        synchronized(this.getClass()) {
            //Start transaction
            TransactionController transaction = new TransactionController();
            transaction.start()

            //Delete annotation
            def annotations = Annotation.findAllByImage(imageInstance)
            annotations.each { annotation ->
                new RestAnnotationController().deleteAnnotation(annotation,currentUser,false)
            }

            //Delete image
            def json = JSON.parse("{id : $imageInstance.id}")
            def result = processCommand(new DeleteImageInstanceCommand(user: currentUser), json)

            //Stop transaction
            transaction.stop()
            response(result)
        }

    }

    def window = {
        println "WINDOW REQUEST " + params.toString()
        ImageInstance image = ImageInstance.read(params.id)
        AbstractImage abstractImage = image.getBaseImage()
        int x = Integer.parseInt(params.x)
        int y = abstractImage.getHeight() - Integer.parseInt(params.y)
        int w = Integer.parseInt(params.w)
        int h = Integer.parseInt(params.h)
        try {
            String url = abstractImage.getCropURL(x,y,w,h)
            log.info("Window : " + url)
            responseImage(url)
        } catch ( Exception e) {
            log.error("GetThumb:"+e);
        }
    }

    def mask = {
        println "WINDOW REQUEST " + params.toString()
        ImageInstance image = ImageInstance.read(params.id)
        AbstractImage abstractImage = image.getBaseImage()
        int x = Integer.parseInt(params.x)
        int y =  Integer.parseInt(params.y)
        int w = Integer.parseInt(params.w)
        int h = Integer.parseInt(params.h)
        int termID = Integer.parseInt(params.term)

        try {
            //Get the image, compute ratio between asked and received
            String url = abstractImage.getCropURL(x,abstractImage.getHeight() - y,w,h)
            BufferedImage window = getImageFromURL(url)
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
            roiPoints[0] = new Coordinate(x   , abstractImage.getHeight() - y);
            roiPoints[1] = new Coordinate(x+w , abstractImage.getHeight() - y);
            roiPoints[2] = new Coordinate(x+w , abstractImage.getHeight() - (y+h));
            roiPoints[3] = new Coordinate(x   , abstractImage.getHeight() - (y+h));
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
            window = segmentationService.colorizeWindow(abstractImage, window, intersectGeometries, Color.decode(term.color), x,y,x_ratio,y_ratio)
            responseBufferedImage(window)
        } catch ( Exception e) {
            log.error("GetThumb:"+e);
        }
    }
}
