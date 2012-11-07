package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import groovy.sql.Sql
import org.codehaus.groovy.grails.web.json.JSONArray

import java.text.SimpleDateFormat
import be.cytomine.ontology.ReviewedAnnotation

class RestReviewedAnnotationController extends RestController {

    def exportService
    def grailsApplication
    def algoAnnotationService
    def domainService
    def termService
    def imageInstanceService
    def userService
    def projectService
    def cytomineService
    def dataSource
    def reviewedAnnotationService


    //list all
    def list = {
        def annotations = []
        def projects = projectService.list()
        projects.each {
            annotations.addAll(reviewedAnnotationService.list(it))
        }
        responseSuccess(annotations)
    }

    //list all by image
    def listByImage = {
        log.info "listByImage"
        ImageInstance image = imageInstanceService.read(params.long('idImage'))
        if (image) responseSuccess(reviewedAnnotationService.list(image))
        else responseNotFound("Image", params.idImage)
    }

    //list all by project
    def listByProject = {
        log.info "listByProject"
        Project project = projectService.read(params.long('idProject'), new Project())
        if (project) responseSuccess(reviewedAnnotationService.list(project))
        else responseNotFound("Project", params.idProject)
    }

    //list all by project, term and user
    def listByProjectImageTermAndUser = {
        log.info "listByProjectImageTermAndUser"
        if((params.users == null || params.users == "null") && (params.images == null || params.images == "null") && (params.terms == null || params.terms == "null") && (params.conflict == null || params.conflict == "null" || params.conflict == "false"))
            forward(action: "listByProject")
        Project project = projectService.read(params.long('idProject'), new Project())
        if (project) {
            Integer offset = params.offset!=null? params.getInt('offset') : 0
            Integer max = params.max!=null? params.getInt('max') : Integer.MAX_VALUE
            Collection<SecUser> userList = []
            if (params.users != null && params.users != "null" && params.users != "")  {
                userList = userService.list(project, params.users.split("_").collect{ Long.parseLong(it)})
            }
            else {
                userList = userService.list(project)
            }
            Collection<ImageInstance> imageInstanceList = []
            if (params.images != null && params.images != "null" && params.images != "") {
                imageInstanceList = imageInstanceService.list(project, params.images.split("_").collect{ Long.parseLong(it)})
            } else {
                imageInstanceList = imageInstanceService.list(project)
            }
            Collection<Term> termList = []
            if (params.terms != null && params.terms != "null" && params.terms != "") {
                termList = termService.list(project, params.terms.split("_").collect{ Long.parseLong(it)})
            } else {
                termList = termService.list(project)
                log.info "termList=$termList"
            }

            if (userList.isEmpty()) {
                responseNotFound("User", params.users)
            } else if (imageInstanceList.isEmpty()){
                responseNotFound("ImageInstance", params.images)
            } else if (termList.isEmpty()){
                 responseNotFound("Term", params.terms)
            }else {
                def list = reviewedAnnotationService.list(project, userList, imageInstanceList,termList,(params.conflict == "true"))
                if(params.offset!=null) responseSuccess([size:list.size(),collection:substract(list,offset,max)])
                else responseSuccess(list)
            }
        }
        else responseNotFound("Project", params.idProject)
    }

    //show
    def show = {
        ReviewedAnnotation annotation = reviewedAnnotationService.read(params.long('id'))
        if (annotation) {
            reviewedAnnotationService.checkAuthorization(annotation.project)
            responseSuccess(annotation)
        }
        else responseNotFound("ReviewedAnnotation", params.id)
    }

    //add
    def add = {
        add(reviewedAnnotationService, request.JSON)
    }

    def addAllJobImageAnnotation = {
        //TODO::

    }

    def addAllUserImageAnnotation = {
       //TODO::
    }

    //update
    def update = {
        update(reviewedAnnotationService, request.JSON)
    }

    //delete
    def delete = {
        delete(reviewedAnnotationService, JSON.parse("{id : $params.id}"))
    }

    //listByImageAndUser
    def listByImageAndUser = {
        def image = imageInstanceService.read(params.long('idImage'))
        def user = userService.read(params.idUser)
        if (image && user && params.bbox) {
            responseSuccess(reviewedAnnotationService.list(image, user, (String) params.bbox))
        }
        else if (image && user) responseSuccess(reviewedAnnotationService.list(image, user))
        else if (!user) responseNotFound("User", params.idUser)
        else if (!image) responseNotFound("Image", params.idImage)
    }

    private def substract(List collection, Integer offset, Integer max) {
        //TODO:: extract
        if (offset>=collection.size()) return []

        def maxForCollection = Math.min(collection.size()-offset,max)
        log.info "collection=${collection.size()} offset=$offset max=$max compute=${collection.size()-offset} maxForCollection=$maxForCollection"
        return collection.subList(offset,offset+maxForCollection)
    }
}
