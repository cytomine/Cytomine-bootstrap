package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.security.SecUser
import be.cytomine.ontology.UserAnnotation
import be.cytomine.AnnotationDomain
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.Exception.CytomineException

/**
 * Controller that handle Retrieval request
 *
 */
class RestRetrievalController extends RestController {

    def retrievalService
    def cytomineService

    /**
     * Look for similar annotation and term suggested for annotation in params
     */
    def listSimilarAnnotationAndBestTerm = {

        log.info "List with id userannotation:" + params.idannotation
        try {

            AnnotationDomain annotation = getAnnotationDomain(params.idannotation)

            if(!annotation) {
                responseNotFound("AnnotationDomain",params.idannotation)
            } else {
                def data = retrievalService.listSimilarAnnotationAndBestTerm(annotation.project, annotation)
               response.status = 200
               responseSuccess(data)
            }
        } catch (CytomineException e) {
                log.error(e)
                response([success: false, errors: e.msg], e.code)
         }catch (java.net.ConnectException ex) {
            response.status = 500
            log.error "Retrieval connexion: " + ex.toString()
        }
    }

    def missingAnnotation = {
        log.info "get missing annotation"
        retrievalService.indexMissingAnnotation()
        responseSuccess([])
    }
}
