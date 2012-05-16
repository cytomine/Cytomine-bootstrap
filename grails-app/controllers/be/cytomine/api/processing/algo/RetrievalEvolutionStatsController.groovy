package be.cytomine.api.processing.algo

import be.cytomine.api.RestController
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob

class RetrievalEvolutionStatsController extends RestController {

    def annotationService
    def termService
    def algoAnnotationTermService
    def jobService
    def retrievalSuggestedTermJobService
    def retrievalEvolutionJobService

    /**
     * If params.project && params.software, get the last userJob from this software from this project
     * If params.job, get userjob with job
     * @param params
     * @return
     */
    private UserJob retrieveUserJobFromParams(def params) {
        log.info "retrieveUserJobFromParams:" + params
        SecUser userJob = null
        if (params.project != null && params.software != null) {
            Project project = Project.read(params.project)
            Software software = Software.read(params.software)
            if(project && software) userJob = jobService.getLastUserJob(project, software)
        } else if (params.job != null) {
            userJob = UserJob.findByJob(Job.read(params.long('job')))
        }
        return userJob
    }


    def statRetrievalEvolution = {
        UserJob userJob = retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def data = []
        def evolution = retrievalEvolutionJobService.listAVGEvolution(userJob.job)
        if (evolution) data = ['evolution': evolution]
        responseSuccess(data)
    }

    def statRetrievalEvolutionByTerm = {
        UserJob userJob = retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def data = []
        if (params.term!=null) {
            Term term = Term.read(params.term)
            def evolution = retrievalEvolutionJobService.listAVGEvolution(userJob.job,term)
            if (evolution) data = ['evolution': evolution]
            responseSuccess(data)            
        } else {
            responseNotFound("Term", params.term)
        }

    }
}
