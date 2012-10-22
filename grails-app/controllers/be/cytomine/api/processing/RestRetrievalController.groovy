package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.security.SecUser
import be.cytomine.ontology.UserAnnotation

class RestRetrievalController extends RestController {

    def retrievalService
    def cytomineService

    def search = {
        log.info "List with id userannotation:" + params.iduserannotation
        responseSuccess(retrievalService.loadAnnotationSimilarities(UserAnnotation.read(params.iduserannotation)))
    }

    def listSimilarAnnotationAndBestTerm = {
        log.info "List with id userannotation:" + params.iduserannotation
        try {
            UserAnnotation annotation = UserAnnotation.read(params.iduserannotation)
            def data = retrievalService.listSimilarAnnotationAndBestTerm(annotation.project, annotation)
            response.status = 200
            responseSuccess(data)
        } catch (java.net.ConnectException ex) {
            response.status = 500
            log.error "Retrieval connexion: " + ex.toString()
        }
    }

    def index = {
        log.info "index with id userannotation:" + params.iduserannotation
        SecUser user = cytomineService.currentUser
        retrievalService.indexAnnotationAsynchronous(UserAnnotation.read(params.iduserannotation))
        responseSuccess([])
    }

    def missingAnnotation = {
        log.info "get missing annotation"
        retrievalService.indexMissingAnnotation()
        responseSuccess([])
    }
}
