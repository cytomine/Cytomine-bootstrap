package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.project.Project
import grails.converters.JSON

class RestSuggestedTermController extends RestController {

    def projectService
    def annotationService
    def termService
    def jobService
    def suggestedTermService

    def list = {
        Annotation annotation = annotationService.read(params.long('idannotation'))
        if (params.idannotation) {
            if (annotation)
                responseSuccess(suggestedTermService.list(annotation))
            else
                responseNotFound("SuggestedTerm", "Annotation", params.idannotation)
        } else responseSuccess(suggestedTermService.list())
    }

    def show = {
        Term term = termService.read(params.idterm)
        Annotation annotation = annotationService.read(params.long('idannotation'))
        Job job = jobService.read(params.long('idjob'))
        SuggestedTerm suggestedTerm = suggestedTermService.read(annotation, term, job)
        if (suggestedTerm) responseSuccess(suggestedTerm)
        else responseNotFound("SuggestedTerm", "Term", params.idterm, "Annotation", params.idannotation, "Job", params.idjob)
    }

    def worstAnnotation = {
        Project project = projectService.read(params.long('idproject'))
        int max = params.max ? Integer.parseInt(params.max) : 20
        responseSuccess(suggestedTermService.listWorst(project, max))
    }

    def worstTerm = {
        Project project = projectService.read(params.long('idproject'))
        int max = params.max ? Integer.parseInt(params.max) : 20
        responseSuccess(suggestedTermService.listWorstTerm(project, max))
    }

    def add = {
        add(suggestedTermService, request.JSON)
    }

    def update = {
        update(suggestedTermService, request.JSON)
    }

    def delete = {
        delete(suggestedTermService, JSON.parse("{annotation : $params.idannotation, term : $params.idterm,job : $params.idjob}"))
    }

}
