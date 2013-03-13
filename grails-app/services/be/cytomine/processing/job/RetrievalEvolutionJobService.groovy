package be.cytomine.processing.job

import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON

/**
 * Software that compute suggest term of each annotation of a project
 * It compute prediction every X days (or week,month, year,...) between two dates
 */
class RetrievalEvolutionJobService extends AbstractJobService{

    static transactional = false
    def springSecurityService
    def cytomineService
    def commandService
    def modelService
    def jobParameterService
    def algoAnnotationTermService

    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("publicKey",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("privateKey",job,userJob.privateKey).encodeAsJSON()))
        //Execute Job
        log.info "Execute Job..."
    }

    def execute(Job job) {

        String[] jobParams = getParametersValues(job)
        String[] mainArgs = createArgsArray(job)
        String[] allArgs = new String[mainArgs.length+jobParams.length+2]

        int index = 0
        mainArgs.each {
            allArgs[index] = mainArgs[index]
            index++
        }
        //build software params
        allArgs[index++] = job.id
        allArgs[index++] = UserJob.findByJob(job).id

        jobParams.each {
            allArgs[index++] = it
        }

        println "allArgs=$allArgs"

        printStartJobInfo(job,allArgs)
        launchAndWaitSoftware(allArgs,job)
        printStopJobInfo(job,allArgs)
    }
    
    def listAVGEvolution(Job job) {
        List<UserJob> userJobs = UserJob.findAllByJob(job, [sort : "created", order: "desc"])
        return algoAnnotationTermService.listAVGEvolution(userJobs, job.project)
    }

    def listAVGEvolution(Job job, Term term) {
        List<UserJob> userJobs = UserJob.findAllByJob(job, [sort : "created", order: "desc"])
        return algoAnnotationTermService.listAVGEvolution(userJobs, job.project,term)
    }

    @Override
    Double computeRate(Job job) {
        if(job.rate==-1 && job.status==Job.SUCCESS) {
            def result = listAVGEvolution(job)
            log.info "result="+result
            if(!result.isEmpty()) {
                job.rate = result.first().avg
//                job.save(flush: true)
            }
        }
        return job.rate
    }
}
