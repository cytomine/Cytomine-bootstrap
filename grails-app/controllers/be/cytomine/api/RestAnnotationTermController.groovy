package be.cytomine.api

import be.cytomine.security.User
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.image.ImageInstance
import be.cytomine.Exception.CytomineException

class RestAnnotationTermController extends RestController {

    def springSecurityService
    def termService
    def annotationService
    def annotationTermService
    def transactionService

    def listTermByAnnotation = {
        if (params.idannotation == "undefined") responseNotFound("Annotation Term", "Annotation", params.idannotation)
        else {
            Annotation annotation = annotationService.read(params.idannotation)
            if (annotation && !params.idUser) responseSuccess(annotationTermService.list(annotation))
            else if (annotation && params.idUser) {
                User user = User.read(params.idUser)
                if (user) responseSuccess(termService.list(annotation,user))
                else responseNotFound("Annotation Term", "User", params.idUser)
            }
            else responseNotFound("Annotation Term", "Annotation", params.idannotation)
        }

    }

    def listAnnotationTermByUserNot = {
        if (params.idannotation == "undefined") responseNotFound("Annotation Term", "Annotation", params.idannotation)
        else {
            Annotation annotation = annotationService.read(params.idannotation)
            if (annotation != null && params.idNotUser) {
                User user = User.read(params.idNotUser)
                if (user) responseSuccess(annotationTermService.listNotUser(annotation, user))
                else responseNotFound("Annotation Term", "User", params.idUser)
            }
        }
    }

    def listAnnotationByProjectAndImageInstance = {
        Term term = Term.read(params.idterm)
        def annotations = []
        Annotation.findAllByImage(ImageInstance.read(params.idimageinstance)).each { annotation ->
            annotation.annotationTerm.each { annotationTerm ->
                if (annotationTerm.getTerm() == term) annotations << annotation
            }
        }
        responseSuccess(annotations)
    }


    def show = {
        Annotation annotation = annotationService.read(params.idannotation)
        Term term = termService.read(params.idterm)

        if(!annotation) responseNotFound("Annotation",params.idannotation)
        if(!term) responseNotFound("Term",params.idterm)
        else {
           if(params.idUser && User.read(params.idUser)) {
                def annoterm = annotationTermService.read(annotation,term,User.read(params.idUser))
                if(annoterm) responseSuccess(annoterm)
                else responseNotFound("Annotation Term", "Term", "Annotation", "User", params.idterm, params.idannotation, params.idUser)
           } else {
               def annoterm =  annotationTermService.read(annotation,term,null)
                if(annoterm) responseSuccess(annoterm)
               else responseNotFound("Annotation Term", "Term", "Annotation", params.idterm, params.idannotation)
           }

        }
    }
    def add = {
        try {
            def result = annotationTermService.addAnnotationTerm(request.JSON)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def delete = {
        try {

            def result = annotationTermService.deleteAnnotationTerm(params.idannotation, params.idterm)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    /**
     * Add annotation-term for an annotation and delete all annotation-term that where already map with this annotation by this user
     */
    def addWithDeletingOldTerm = {
        try {
            def result = annotationTermService.addAnnotationTerm(params.idannotation, params.idterm)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

}