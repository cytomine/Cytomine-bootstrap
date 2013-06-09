package be.cytomine.processing.job

import be.cytomine.processing.AutoLungJob
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
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

    static rabbitQueue = 'jobQueue'

    def cytomineService
    def commandService
    def modelService

    def jobParameterService
    def jobDataService

    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_host",job,"localhost:8080").encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_base_path",job,"/api/").encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_public_key",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_private_key",job,userJob.privateKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_project",job,job.getProject().id.toString()).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_software",job,job.getSoftware().id.toString()).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_job",job,job.id.toString()).encodeAsJSON()))
    }

    def execute(Job job, UserJob userJob, boolean preview) {
        println "execute job $job, preview : $preview"
        AutoLungJob.triggerNow([ job : job, userJob: userJob, preview : preview])
    }

    public boolean previewAvailable() {
        return true
    }

    def getPreviewROI(Job job) {
        return jobDataService.getJobDataBinaryValue(job, "preview_before")
    }

    def getPreview(Job job) {
        return jobDataService.getJobDataBinaryValue(job, "preview_after")
    }

    @Override
    Double computeRate(Job job) {
        return null;
    }
}
