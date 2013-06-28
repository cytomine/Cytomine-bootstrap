package be.cytomine.processing.job

import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON

/**
 * Launch compute term area job
 * Compute area % between terms for all images annotations
 */
class ComputeTermAreaJobService extends AbstractJobService{

//    static transactional = false

    def jobParameterService

    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("publicKey",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("privateKey",job,userJob.privateKey).encodeAsJSON()))
        //Execute Job
        log.info "Execute Job..."
    }

    def execute(Job job, UserJob userJob, boolean preview) {

        //get job params
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

        printStartJobInfo(job,allArgs)
        launchAndWaitSoftware(allArgs,job)
        printStopJobInfo(job,allArgs)
    }

    @Override
    Double computeRate(Job job) {
        return null;
    }
}
