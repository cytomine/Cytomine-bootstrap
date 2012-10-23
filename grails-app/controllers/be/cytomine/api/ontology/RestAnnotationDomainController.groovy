package be.cytomine.api.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.social.SharedAnnotation
import grails.converters.JSON

import java.awt.image.BufferedImage
import java.text.SimpleDateFormat
import javax.imageio.ImageIO

class RestAnnotationDomainController extends RestController {

    def exportService
    def grailsApplication
    def userAnnotationService
    def domainService
    def termService
    def imageInstanceService
    def userService
    def projectService
    def cytomineService
    def mailService
    def dataSource

    def listByProject = {
        Project project = projectService.read(params.long('id'), new Project())

        if (project) {
            Collection<SecUser> userList = []
            if (params.users != null && params.users != "null" && params.users != "") {
                userList = userService.list(project, params.users.split("_").collect{ Long.parseLong(it)})
                if(!userList.isEmpty() && userList.get(0)?.algo()) {
                   forward(controller: "restAlgoAnnotation", action: "listByProject")
               } else {
                   forward(controller: "restUserAnnotation", action: "listByProject")
               }
            }
            else {
                forward(controller: "restUserAnnotation", action: "listByProject")
            }
        }
        else responseNotFound("Project", params.id)
    }


    def listByImageAndUser = {
        def user = SecUser.read(params.idUser)
        if (user) {
            if(user.algo()) {
                forward(controller: "restAlgoAnnotation", action: "listByImageAndUser")
            } else {
                forward(controller: "restUserAnnotation", action: "listByImageAndUser")
            }
        }
        else if (!user) responseNotFound("User", params.idUser)
    }


    def listAnnotationByProjectAndTerm = {
        log.info "listAnnotationByProjectAndTerm"
        Project project = projectService.read(params.long('idproject'), new Project())

        Collection<SecUser> userList = []
        if (params.users != null && params.users != "null" && project) {
            if (params.users != "") {
                userList = userService.list(project, params.users.split("_").collect{ Long.parseLong(it)})
                if(!userList.isEmpty() && userList.get(0)?.algo()) {
                   forward(controller: "restAlgoAnnotation", action: "listAnnotationByProjectAndTerm")
               } else {
                   forward(controller: "restUserAnnotation", action: "listAnnotationByProjectAndTerm")
               }
            }
        }
        else {
            forward(controller: "restUserAnnotation", action: "listAnnotationByProjectAndTerm")
        }


    }


    def downloadDocumentByProject = {  //and filter by users and terms !
        // Export service provided by Export plugin

        def users = []
        if (params.users != null && params.users != "") {
            params.users.split(",").each { id ->
                users << Long.parseLong(id)
            }
        }

        if(!users.isEmpty() && SecUser.read(users.first()).algo()) {
            forward(controller: "restAlgoAnnotation", action: "downloadDocumentByProject")
        } else   forward(controller: "restUserAnnotation", action: "downloadDocumentByProject")
    }

    def show = {
        UserAnnotation annotation = userAnnotationService.read(params.long('id'))
        if(annotation) forward(controller: "restUserAnnotation", action: "show")
        else forward(controller: "restAlgoAnnotation", action: "show")
    }


    def add = {
        try {
            SecUser user = cytomineService.currentUser
            def result
            if(user.algo()) {
                forward(controller: "restAlgoAnnotation", action: "add")
            } else {
                forward(controller: "restUserAnnotation", action: "add")
            }
            //responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def update= {
        //def json = request.JSON
        try {
            SecUser user = cytomineService.currentUser
            def result
            if(user.algo()) {
                forward(controller: "restAlgoAnnotation", action: "update")
            } else {
                forward(controller: "restUserAnnotation", action: "update")
            }
            //responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def delete = {
        try {
            SecUser user = cytomineService.currentUser
            def result
            if(user.algo()) {
                forward(controller: "restAlgoAnnotation", action: "delete")
            } else {
                forward(controller: "restUserAnnotation", action: "delete")
            }
            //responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

}
