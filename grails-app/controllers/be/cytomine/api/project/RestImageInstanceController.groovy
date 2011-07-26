package be.cytomine.api.project

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
import be.cytomine.ontology.RelationTerm
import be.cytomine.command.relationterm.DeleteRelationTermCommand
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.command.annotationterm.DeleteAnnotationTermCommand
import be.cytomine.ontology.Term
import be.cytomine.command.term.DeleteTermCommand
import be.cytomine.command.TransactionController
import be.cytomine.command.annotation.DeleteAnnotationCommand
import org.perf4j.StopWatch
import org.perf4j.LoggingStopWatch
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

    def index = {
        redirect(controller: "image")
    }
    def list = {
        log.info "list"
        response(ImageInstance.list())
    }

    def show = {
        log.info "show " + params.id
        ImageInstance image = ImageInstance.read(params.id)
        if(image!=null) responseSuccess(image)
        else responseNotFound("ImageInstance",params.id)
    }


    def showByProjectAndImage = {
        log.info "show project: " + params.idproject + " " +  " image: " + params.idimage
        Project project = Project.read(params.idproject)
        AbstractImage image = AbstractImage.read(params.idimage)
        ImageInstance imageInstance = ImageInstance.findByBaseImageAndProject(image,project)
        if(imageInstance!=null) responseSuccess(imageInstance)
        else responseNotFound("ImageInstance","Project",params.idproject,"Image",params.idimage)
    }

    def listByUser = {
        log.info "List with id user:"+params.id
        User user = User.read(params.id)
        if(user!=null) responseSuccess(ImageInstance.findAllByUser(user))
        else responseNotFound("ImageInstance","User",params.id)
    }

    def listByImage = {
        log.info "List with id user:"+params.id
        AbstractImage image = AbstractImage.read(params.id)
        if(image!=null) responseSuccess(ImageInstance.findAllByBaseImage(image))
        else responseNotFound("ImageInstance","AbstractImage",params.id)
    }

    def listByProject = {
        StopWatch stopWatch = new LoggingStopWatch();
        log.info "List with id project:"+params.id
        Project project = Project.read(params.id)
        StopWatch stopWatchfind = new LoggingStopWatch();
        def images = ImageInstance.findAllByProject(project)
        stopWatchfind.stop("RestImageInstanceController.findAllByProject");
        stopWatchfind = new LoggingStopWatch();
        if(project!=null) responseSuccess(images)
        else responseNotFound("ImageInstance","Project",params.id)
         stopWatchfind.stop("RestImageInstanceController.response");
        stopWatch.stop("RestImageInstanceController.listByProject");
    }

    def add = {
        log.info "Add"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command addImageInstanceCommand = new AddImageInstanceCommand(postData : request.JSON.toString(), user: currentUser)
        def result
        synchronized(this.getClass()) {
            result = processCommand(addImageInstanceCommand, currentUser)
        }
        response(result)
    }

    def update = {
        log.info "Update"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " request:" + request.JSON.toString()
        Command editImageInstanceCommand = new EditImageInstanceCommand(postData : request.JSON.toString(), user: currentUser)
        def result = processCommand(editImageInstanceCommand, currentUser)
        response(result)
    }

    def delete = {
        log.info "Delete"
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        log.info "User:" + currentUser.username + " params.idproject=" + params.idproject+ " params.idimage=" + params.idimage
        Project project = Project.read(params.idproject)
        AbstractImage image = AbstractImage.read(params.idimage)
        ImageInstance imageInstance = ImageInstance.findByBaseImageAndProject(image,project)
        if(!imageInstance)
        {
            responseNotFound("ImageInstance","Project",params.idproject,"Image",params.idimage)
            return
        }
        synchronized(this.getClass()) {
            log.info "TransactionController"
            TransactionController transaction = new TransactionController();
            transaction.start()

            def annotations = Annotation.findAllByImage(imageInstance)
            log.debug "annotations.size=" +  annotations.size()
            annotations.each { annotation ->

                def terms = annotation.terms()
                log.debug "annotation.terms.size=" + terms.size()
                terms.each { term ->

                    def annotationTerm = AnnotationTerm.findAllByTermAndAnnotation(term,annotation)
                    log.info "annotationTerm= " +annotationTerm.size()

                    annotationTerm.each{ annotterm ->
                        log.info "unlink annotterm:" +annotterm.id
                        def postDataRT = ([term: annotterm.term.id,annotation: annotterm.annotation.id]) as JSON
                        Command deleteAnnotationTermCommand = new DeleteAnnotationTermCommand(postData :postDataRT.toString() ,user: currentUser,printMessage:false)
                        def result = processCommand(deleteAnnotationTermCommand, currentUser)
                    }
                }

                Annotation annotationDeleted =  annotation
                log.info "delete term " +annotationDeleted
                def postDataAnnotation = ([id : annotationDeleted.id]) as JSON
                Command deleteAnnotationCommand = new DeleteAnnotationCommand(postData :postDataAnnotation.toString() ,user: currentUser,printMessage:false)
                def result = processCommand(deleteAnnotationCommand, currentUser)
            }
            log.info "delete image"

            def postData = ([id : imageInstance.id]) as JSON
            Command deleteImageInstanceCommand = new DeleteImageInstanceCommand(postData : postData.toString(), user: currentUser)
            def result

            result = processCommand(deleteImageInstanceCommand, currentUser)

            transaction.stop()
            response(result)
        }

    }
}
