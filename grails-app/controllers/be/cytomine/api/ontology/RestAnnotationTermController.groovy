package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SecurityCheck
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.ontology.AnnotationTerm

/**
 * Controller that handle link between an annotation and a term
 * This controller carry request for (user)annotationterm and algoannotationterm
 */
class RestAnnotationTermController extends RestController {

    def termService
    def annotationTermService
    def userAnnotationService
    def algoAnnotationService
    def algoAnnotationTermService
    def cytomineService

    /**
     * List all term map with an annotation
     */
    def listTermByAnnotation = {

        if (params.idannotation == "undefined") {
            responseNotFound("Annotation Term", "Annotation", params.idannotation)
        }
        else {
            AnnotationDomain annotation = userAnnotationService.read(params.long('idannotation'))
            if (!annotation) {
                annotation = algoAnnotationService.read(params.long('idannotation'))
            }

            if (annotation && !params.idUser && annotation instanceof UserAnnotation) {
                responseSuccess(annotationTermService.list(annotation))
            } else if (annotation && !params.idUser && annotation instanceof AlgoAnnotation) {
                responseSuccess(algoAnnotationTermService.list(annotation))
            } else if (annotation && params.idUser) {
                User user = User.read(params.long('idUser'))
                if (user) {
                    responseSuccess(termService.list(annotation, user))
                }
                else {
                    responseNotFound("Annotation Term", "User", params.idUser)
                }
            }
            else {
                responseNotFound("Annotation Term", "Annotation", params.idannotation)
            }
        }
    }

    /**
     * Get all term link with an annotation by all user except  params.idUser
     */
    def listAnnotationTermByUserNot = {
        if (params.idannotation == "undefined") {
            responseNotFound("Annotation Term", "Annotation", params.idannotation)
        } else {
            UserAnnotation annotation = userAnnotationService.read(params.long('idannotation'))
            if (annotation != null && params.idNotUser) {
                User user = User.read(params.idNotUser)
                if (user) {
                    responseSuccess(annotationTermService.listNotUser(annotation, user))
                }else {
                    responseNotFound("Annotation Term", "User", params.idUser)
                }
            }
        }
    }

    def listAnnotationByProjectAndImageInstance = {
        Term term = Term.read(params.idterm)
        def annotations = []
        UserAnnotation.findAllByImage(ImageInstance.read(params.idimageinstance)).each { annotation ->
            AnnotationTerm.findAllByUserAnnotation(annotation).each { annotationTerm ->
                if (annotationTerm.getTerm() == term) {
                    annotations << annotation
                }
            }
        }
        responseSuccess(annotations)
    }

    def show = {
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(params.long('idannotation'))

        println "User annotation 3 = ${annotation.id}"

        Term term = termService.read(params.long('idterm'))

        if (!annotation) {
            println "User annotation 4 = ${annotation?.id}"
            responseNotFound("Annotation", params.idannotation)
        }
        if (!term) {
            responseNotFound("Term", params.idterm)
        }
        else {
            println "User annotation 5 = ${annotation?.id}"
            if (params.idUser && SecUser.read(params.idUser)) {
                //user is set, get a specific annotation-term link from user
                if(!cytomineService.isUserAlgo()) {
                    def annoterm = annotationTermService.read(annotation, term, SecUser.read(params.idUser))
                    if (annoterm) responseSuccess(annoterm)
                    else responseNotFound("Annotation Term", "Term", "Annotation", "User", params.idterm, params.idannotation, params.idUser)
                } else {
                    def annoterm = algoAnnotationTermService.read(annotation, term, SecUser.read(params.idUser))
                    if (annoterm) responseSuccess(annoterm)
                    else responseNotFound("Algo Annotation Term", "Term", "Annotation", "User", params.idterm, params.idannotation, params.idUser)
                }
            } else {
                //user is not set, we will get the annotation-term from all user
                if(!cytomineService.isUserAlgo()) {
                    def annoterm = annotationTermService.read(annotation, term, null)
                    if (annoterm) responseSuccess(annoterm)
                    else responseNotFound("Annotation Term", "Term", "Annotation", params.idterm, params.idannotation)
                } else {
                     def annoterm = algoAnnotationTermService.read(annotation, term, null)
                    if (annoterm) responseSuccess(annoterm)
                    else responseNotFound("Algo Annotation Term", "Term", "Annotation", params.idterm, params.idannotation)
                }

            }

        }
    }

    def add = {
        def json = request.JSON
        try {
            if(!cytomineService.isUserAlgo()) {
                if(!json.userannotation || !UserAnnotation.read(json.userannotation)) {
                    throw new WrongArgumentException("AnnotationTerm must have a valide userannotation:"+json.userannotation)
                }
                def result = annotationTermService.add(json)
                responseResult(result)
            } else {
                //TODO:: won't work if we add an annotation term to a algoannotation
                AnnotationDomain annotation = AlgoAnnotation.read(json.annotationIdent)
                if(!annotation) annotation = UserAnnotation.read(json.annotationIdent)
                if(!json.annotationIdent || !annotation) {
                    throw new WrongArgumentException("AlgoAnnotationTerm must have a valide annotation:"+json.annotationIdent)
                }
                def result = algoAnnotationTermService.add(json)
                responseResult(result)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    def delete = {
        if(cytomineService.isUserAlgo()) {
            throw new InvalidRequestException("A annotatation term from userJob cannot delete term")
        }
        def idUser = params.idUser!=null ? params.idUser : cytomineService.getCurrentUser().id
        def json = JSON.parse("{userannotation: $params.idannotation, term: $params.idterm, user: $idUser}")
        delete(annotationTermService, json,null)
    }

    /**
     * Add annotation-term for an annotation and delete all annotation-term that where already map with this annotation by this user
     */
    def addWithDeletingOldTerm = {
        try {
            if(cytomineService.isUserAlgo()) {
                throw new InvalidRequestException("A userJob cannot delete user term from userannotation")
            }
            def result = annotationTermService.addWithDeletingOldTerm(params.idannotation, params.idterm)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        }
    }

}