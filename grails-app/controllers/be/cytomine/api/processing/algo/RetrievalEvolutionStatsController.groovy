package be.cytomine.api.processing.algo

import be.cytomine.api.RestController
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob

/**
 * Controller for request on stats with retrieval evolution job
 * Retrieval evolution computes the success rate of prediction during the time
 * -> Each iteration is a different user job (ex: 01/01/2012 - user 1 ; 01/04/2012 - user 2...) and their share
 * a common job domain
 */
class RetrievalEvolutionStatsController extends RestController {

    def termService
    def algoAnnotationTermService
    def jobService
    def retrievalEvolutionJobService

    /**
     * Compute the retrieval evolution success for all annotation
     */
    def statRetrievalEvolution = {
        UserJob userJob = jobService.retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def data = []
        def evolution = retrievalEvolutionJobService.listAVGEvolution(userJob.job)
        if (evolution) {
            data = ['evolution': evolution]
        }
        responseSuccess(data)
    }

    /**
     * Compute the retrieval evolution success for annotation of a specific term
     */
    def statRetrievalEvolutionByTerm = {
        UserJob userJob = jobService.retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def data = []
        if (params.term!=null) {
            Term term = Term.read(params.term)
            def evolution = retrievalEvolutionJobService.listAVGEvolution(userJob.job,term)
            if (evolution) {
                data = ['evolution': evolution]
            }
            responseSuccess(data)            
        } else {
            responseNotFound("Term", params.term)
        }
    }
}
