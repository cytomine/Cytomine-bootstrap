package be.cytomine.api

import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.processing.Job
import be.cytomine.Exception.CytomineException

class RestSuggestedTermController extends RestController {

    def springSecurityService
    def transactionService

    def projectService
    def annotationService
    def termService
    def jobService
    def suggestedTermService

    def list = {
        Annotation annotation = annotationService.read(params.idannotation)
        if (params.idannotation) {
            if (annotation)
                responseSuccess(suggestedTermService.list(annotation))
            else
                responseNotFound("SuggestedTerm", "Annotation", params.idannotation)
        } else responseSuccess(suggestedTermService.list())
    }

    def show = {
        Term term = termService.read(params.idterm)
        Annotation annotation = annotationService.read(params.idannotation)
        Job job = jobService.read(params.idjob)
        SuggestedTerm suggestedTerm = suggestedTermService.read(annotation, term, job)
        if (suggestedTerm) responseSuccess(suggestedTerm)
        else responseNotFound("SuggestedTerm", "Term", params.idterm, "Annotation", params.idannotation, "Job", params.idjob)
    }

    def worstAnnotation = {
        Project project = projectService.read(params.idproject)
        int max = params.max ? Integer.parseInt(params.max) : 20
        responseSuccess(suggestedTermService.listWorst(project, max))
    }

    def worstTerm = {
        Project project = projectService.read(params.idproject)
        int max = params.max ? Integer.parseInt(params.max) : 20
        responseSuccess(suggestedTermService.listWorstTerm(project, max))
    }

    def add = {
        try {
            def result = suggestedTermService.add(request.JSON)
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
            def result = suggestedTermService.delete(params.idannotation, params.idterm, params.idjob)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

}
