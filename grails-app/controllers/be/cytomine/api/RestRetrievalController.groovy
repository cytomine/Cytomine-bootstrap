package be.cytomine.api

import be.cytomine.ontology.Annotation
import be.cytomine.project.Project

class RestRetrievalController extends RestController {

    def retrievalService

    def search = {
        log.info "List with id annotation:" + params.idannotation
        responseSuccess(retrievalService.loadAnnotationSimilarities(Annotation.read(params.idannotation)))
    }

    def listSimilarAnnotationAndBestTerm = {
        log.info "List with id annotation:" + params.idannotation
        try {
            Annotation annotation =  Annotation.read(params.idannotation)
            Project project = annotation.project()
            def data = retrievalService.listSimilarAnnotationAndBestTerm(project,annotation)
            response.status = 200
            responseSuccess(data)
        } catch (java.net.ConnectException ex) {
            response.status = 500
            log.error "Retrieval connexion: " + ex.toString()
        }
    }

    def index = {
        log.info "index with id annotation:" + params.idannotation
        retrievalService.indexAnnotationAsynchronous(Annotation.read(params.idannotation))
        responseSuccess([])
    }
}
