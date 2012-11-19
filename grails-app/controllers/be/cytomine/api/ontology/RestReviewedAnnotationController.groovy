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
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.security.UserJob

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
        if (image && params.bbox) responseSuccess(reviewedAnnotationService.list(image,(String) params.bbox))
        else if(image) responseSuccess(reviewedAnnotationService.list(image))
        else responseNotFound("Image", params.idImage)
    }

//
//    //listByImageAndUser
//    def listByImageAndUser = {
//        def image = imageInstanceService.read(params.long('idImage'))
//        def user = userService.read(params.idUser)
//        if (image && user && params.bbox) {
//            responseSuccess(reviewedAnnotationService.list(image, user, (String) params.bbox))
//        }
//        else if (image && user) responseSuccess(reviewedAnnotationService.list(image, user))
//        else if (!user) responseNotFound("User", params.idUser)
//        else if (!image) responseNotFound("Image", params.idImage)
//    }


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
        if ((params.users == null || params.users == "null") && (params.images == null || params.images == "null") && (params.terms == null || params.terms == "null"))
            forward(action: "listByProject")
        Project project = projectService.read(params.long('idProject'), new Project())
        if (project) {
            Integer offset = params.offset != null ? params.getInt('offset') : 0
            Integer max = params.max != null ? params.getInt('max') : Integer.MAX_VALUE
            Collection<SecUser> userList = []
            if (params.users != null && params.users != "null" && params.users != "") {
                userList = userService.list(project, params.users.split("_").collect { Long.parseLong(it)})
            }
            else {
                userList = userService.list(project)
            }
            Collection<ImageInstance> imageInstanceList = []
            if (params.images != null && params.images != "null" && params.images != "") {
                imageInstanceList = imageInstanceService.list(project, params.images.split("_").collect { Long.parseLong(it)})
            } else {
                imageInstanceList = imageInstanceService.list(project)
            }
            Collection<Term> termList = []
            if (params.terms != null && params.terms != "null" && params.terms != "") {
                termList = termService.list(project, params.terms.split("_").collect { Long.parseLong(it)})
            } else {
                termList = termService.list(project)
                log.info "termList=$termList"
            }

            if (userList.isEmpty()) {
                responseNotFound("User", params.users)
            } else if (imageInstanceList.isEmpty()) {
                responseNotFound("ImageInstance", params.images)
            } else if (termList.isEmpty()) {
                responseNotFound("Term", params.terms)
            } else {
                def list = reviewedAnnotationService.list(project, userList, imageInstanceList, termList)
                if (params.offset != null) responseSuccess([size: list.size(), collection: substract(list, offset, max)])
                else responseSuccess(list)
            }
        }
        else responseNotFound("Project", params.idProject)
    }

    //show
    def show = {
        log.info "show annotation = " + params.long('id')
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
        if (offset >= collection.size()) return []

        def maxForCollection = Math.min(collection.size() - offset, max)
        log.info "collection=${collection.size()} offset=$offset max=$max compute=${collection.size() - offset} maxForCollection=$maxForCollection"
        return collection.subList(offset, offset + maxForCollection)
    }



    def startImageInstanceReview = {
        try {
            def image = imageInstanceService.read(params.long("id"))
            def response = [:]

            if (image) {
                image.reviewStart = new Date()
                image.reviewUser = cytomineService.currentUser
                if (!image.validate()) throw new WrongArgumentException("Cannot review (validate) image instance:" + image.errors)
                if (image.save(flush: true) == null) throw new WrongArgumentException("Cannot review (add) image instance:" + image.errors)

                response.message = image.reviewUser.username + " start reviewing on " + image.baseImage.filename
                response.imageinstance = image
                responseSuccess(response)
            } else responseNotFound("Image", params.idImage)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def stopImageInstanceReview = {
        try {
            def image = imageInstanceService.read(params.long("id"))
            def response = [:]

            boolean isCancel = params.getBoolean("cancel")
            if (image) {
                if (image.reviewStart == null || image.reviewUser == null) throw new WrongArgumentException("Image is not in review mode: image.reviewStart=${image.reviewStart} and image.reviewUser=${image.reviewUser}")
                if (cytomineService.currentUser != image.reviewUser) throw new WrongArgumentException("Review can only be validate or stop by "+image.reviewUser.username)

                if(isCancel) {
                    if(!image.reviewStop) {
                        //cancel reviewing
                        image.reviewStart = null
                        image.reviewUser = null
                    } else {
                        //cancel finish reviewing (validate)
                        image.reviewStop = null
                    }

                } else {
                    image.reviewStop = new Date()
                }
                if (!image.validate()) throw new WrongArgumentException("Cannot stop review (validate) image instance:" + image.errors)
                if (image.save(flush: true) == null) throw new WrongArgumentException("Cannot stop review (add) image instance:" + image.errors)

                response.imageinstance = image

                if(isCancel)
                    response.message = cytomineService.currentUser.username + " cancel review or validate on " + image.baseImage.filename
                else response.message = cytomineService.currentUser.username + " validate reviewing on " + image.baseImage.filename
                responseSuccess(response)
            } else responseNotFound("Image", params.idImage)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def addAnnotationReview = {
        try {
            AnnotationDomain basedAnnotation = getAnnotationDomain(params.long('id'))
            if(!basedAnnotation.image.isInReviewMode()) throw new WrongArgumentException("Cannot review annotation, enable image review mode!")
            if(basedAnnotation.image.reviewUser && basedAnnotation.image.reviewUser.id!=cytomineService.currentUser.id) throw new WrongArgumentException("You must be the image reviewer to review annotation. Image reviewer is ${basedAnnotation.image.reviewUser?.username}.")
            if(ReviewedAnnotation.findByParentIdent(basedAnnotation.id)) throw new AlreadyExistException("Annotation is already review!")

            ReviewedAnnotation reviewedAnnotation = reviewAnnotation(basedAnnotation)
            def response = [:]
            response.reviewedannotation = reviewedAnnotation
            response.message = "Annotation review is added"
            responseSuccess(response,201)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def deleteAnnotationReview = {
        try {
            ReviewedAnnotation reviewedAnnotation = ReviewedAnnotation.read(params.long('id'))
            if (!reviewedAnnotation) throw new ObjectNotFoundException("Review Annotation ${params.long('id')} not found!")
            def json = reviewedAnnotation.encodeAsJSON()
            def response = [:]
            response.reviewedannotation = json
            response.message = "Annotation review is deleted"
            domainService.deleteDomain(reviewedAnnotation);
            responseSuccess(response)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    private ReviewedAnnotation reviewAnnotation(AnnotationDomain annotation) {
        ReviewedAnnotation review = new ReviewedAnnotation()
        review.parentIdent = annotation.id
        review.parentClassName = annotation.class.name
        review.status = 1
        review.user = cytomineService.currentUser
        review.location = annotation.location
        review.image = annotation.image
        review.project = annotation.project
        review.geometryCompression = annotation.geometryCompression

        List<Term> terms = annotation.termsForReview()
        terms.each {
            review.addToTerm(it)
        }
        review.reviewUser = cytomineService.currentUser
        domainService.saveDomain(review)
        review
    }

    private AnnotationDomain getAnnotationDomain(long id) {
        AnnotationDomain basedAnnotation = UserAnnotation.read(id)
        if (!basedAnnotation)
            basedAnnotation = AlgoAnnotation.read(id)
        if (basedAnnotation) return basedAnnotation
        else throw new ObjectNotFoundException("Annotation ${id} not found")

    }

    def reviewLayer = {

        try {
            log.info "params.users="+params.users
            log.info("image="+params.image)

            String[] layersParam = params.users.split(",")
            List<SecUser> users = layersParam.collect {
                SecUser.read(Long.parseLong(it))
            }
            ImageInstance image = ImageInstance.read(params.long('image'))
            if(!image) throw new WrongArgumentException("Image ${params.image} was not found!")
            if(!image.isInReviewMode()) throw new WrongArgumentException("Cannot review annotation, enable image review mode!")
            if(image.reviewUser && image.reviewUser.id!=cytomineService.currentUser.id) throw new WrongArgumentException("You must be the image reviewer to review annotation. Image reviewer is ${image.reviewUser?.username}.")


            if(users.isEmpty())
                throw new WrongArgumentException("There is no layer:"+params.users)
            if(!image)
                responseNotFound("ImageInstance",params.image)
            else {
                def data = []

                List<AnnotationDomain> annotations = []

                users.eachWithIndex { user, indexUser ->
                    if(user.algo())
                        annotations.addAll(AlgoAnnotation.findAllByUserAndImage(user,image))
                    else
                        annotations.addAll(UserAnnotation.findAllByUserAndImage(user,image))
                }

                println "annotations="+annotations

                annotations.eachWithIndex { annotation, indexAnnotation ->
                    println "annotation="+annotation
                    if(!ReviewedAnnotation.findByParentIdent(annotation.id)) {
                        println "review"
                        def review = reviewAnnotation(annotation)
                        data << review.id
                    }
                }
                responseSuccess(data)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }

    }

}
