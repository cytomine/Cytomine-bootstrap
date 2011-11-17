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
            log.debug "annotations.size=" +  annotations.size()
            annotations.each { annotation ->

                //Delete Annotation-Term before deleting Annotation
                def annotationTerm = AnnotationTerm.findAllByAnnotation(annotation)
                log.debug "annotation.terms.size=" + terms.size()
                annotationTerm.each { annotterm ->
                    log.info "unlink annotterm:" + annotterm.id
                    def jsonDataRT = ([term: annotterm.term.id, annotation: annotterm.annotation.id, user: annotterm.user.id]) as JSON
                    def result = processCommand(new DeleteAnnotationTermCommand(user: currentUser, printMessage: false), jsonDataRT)
                }

                //Delete Suggested-Term before deleting Annotation
                def suggestTerm = SuggestedTerm.findAllByAnnotation(annotation)
                log.info "suggestTerm= " + suggestTerm.size()
                suggestTerm.each { suggestterm ->
                    log.info "unlink suggestterm:" + suggestterm.id
                    def jsonDataRT = ([term: suggestterm.term.id, annotation: suggestterm.annotation.id, job: suggestterm.job.id]) as JSON
                    def result = processCommand(new DeleteSuggestedTermCommand(user: currentUser, printMessage: false), jsonDataRT)
                }

                //Delete annotation
                Annotation annotationDeleted =  annotation
                log.info "delete term " +annotationDeleted
                def jsonDataAnnotation = ([id : annotationDeleted.id]) as JSON
                def result = processCommand(new DeleteAnnotationCommand(user: currentUser,printMessage:false), jsonDataAnnotation)
            }
            log.info "delete image"

            //Delete image
            def json = ([id : imageInstance.id]) as JSON
            def result = processCommand(new DeleteImageInstanceCommand(user: currentUser), json)

            //Stop transaction
            transaction.stop()
            response(result)
        }

    }
}
