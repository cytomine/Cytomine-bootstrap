package be.cytomine.processing.job

import be.cytomine.processing.HelloWorldJob
import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON

/**
 * Simple software example
 * Just print hello world
 */
class HelloWorldJobService extends AbstractJobService {

    static transactional = false

    def grailsApplication
    def cytomineService
    def commandService
    def modelService

    def jobParameterService
    def jobDataService


    def init(Job job, UserJob userJob) {
        def serverUrl = grailsApplication.config.grails.serverURL.replace("http://", "").replace("https://", "")
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_host",job, serverUrl).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_base_path",job,"/api/").encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_public_key",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_private_key",job,userJob.privateKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_project",job,job.getProject().id.toString()).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_software",job,job.getSoftware().id.toString()).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_job",job,job.id.toString()).encodeAsJSON()))
    }

    def execute(Job job, UserJob userJob, boolean preview) {
        log.info "Hello world"
        HelloWorldJob.triggerNow([ job : job, userJob: userJob, preview : preview, jobParameters : jobParameterService.list(job)])
    }


    @Override
    Double computeRate(Job job) {
        return null;
    }
}
