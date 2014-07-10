package be.cytomine.processing.job

import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON
import grails.util.Holders

/**
 * Launch compute term area job
 * Compute area % between terms for all images annotations
 */
class LaunchLocalScriptService extends AbstractJobService{

    static transactional = true

    def jobParameterService


    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("host",job,Holders.getGrailsApplication().config.grails.serverURL).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("publicKey",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("privateKey",job,userJob.privateKey).encodeAsJSON()))
        //Execute Job
        log.info "Execute Job..."
    }

    def execute(Job job, UserJob userJob, boolean preview) {

        //get params defined for this job
        String[] jobParams = getParametersValues(job)

        //get the executed command value and all its hard-coded parameters
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

        job.discard()
        printStartJobInfo(job,allArgs)
        launchSoftware(allArgs,job)
        printStopJobInfo(job,allArgs)
    }

    @Override
    Double computeRate(Job job) {
        return null;
    }
}
