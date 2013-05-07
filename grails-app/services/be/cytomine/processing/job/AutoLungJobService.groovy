package be.cytomine.processing.job

import be.cytomine.processing.AutoLungJob
import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 6/05/13
 * Time: 10:05
 */
class AutoLungJobService  extends AbstractJobService {

    static transactional = false

    def cytomineService
    def commandService
    def modelService

    def jobParameterService

    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_host",job,"localhost:8080").encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_base_path",job,"/api/").encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_public_key",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_private_key",job,userJob.privateKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_project",job,job.getProject().id.toString()).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_software",job,job.getSoftware().id.toString()).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_job",job,job.id.toString()).encodeAsJSON()))
    }

    def execute(Job job) {
        AutoLungJob.triggerNow([ job : job])
    }


    @Override
    Double computeRate(Job job) {
        return null;
    }
}
