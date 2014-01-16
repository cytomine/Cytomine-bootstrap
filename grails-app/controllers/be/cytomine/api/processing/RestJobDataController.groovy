package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.JobDataBinaryValue
import grails.converters.JSON
import org.springframework.web.multipart.MultipartHttpServletRequest

/**
 *  Controller that handle job data files
 *  Files can be saved on filesystem or in database
 */
class RestJobDataController extends RestController {

    def jobDataService

    /**
     * List all job data
     */
    def list = {
        responseSuccess(jobDataService.list())
    }

    /**
     * List all data files info by job
     */
    def listByJob = {
        Job job = Job.read(params.long('id'))
        if(job) {
            responseSuccess(jobDataService.list(job))
        } else {
            responseNotFound("Job", params.id)
        }
    }

    /**
     * Get a specific data file info
     */
    def show = {
        JobData jobData = jobDataService.read(params.long('id'))
        if (jobData) {
            responseSuccess(jobData)
        } else {
            responseNotFound("JobData", params.id)
        }
    }

    /**
     * Add a new data file description
     * We must call then "upload" action to upload the file
     */
    def add = {
        println "Job Data Controller"
        println params
        add(jobDataService, request.JSON)
    }

    /**
     * Update file info
     */
    def update = {
        update(jobDataService, request.JSON)
    }

    /**
     * Delete file description
     */
    def delete = {
        delete(jobDataService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Upload a file
     */
    def upload = {
        log.info "Upload file = " + params.getLong('id')

        JobData jobData = jobDataService.read(params.getLong('id'))
        JobDataBinaryValue value = new JobDataBinaryValue(jobData:jobData)
        jobDataService.saveDomain(value)
        jobData.value = value
        jobDataService.saveDomain(jobData)

        byte[] bytes

        if(request instanceof MultipartHttpServletRequest) {
            def file = request.getFile('files[]')
            bytes = file.getBytes()
        } else {
            bytes = request.inputStream.bytes
        }

        if(!jobData) {
            responseNotFound("JobData", params.id)
        } else {
            jobData = jobDataService.save(jobData,bytes)
            responseSuccess(jobData)
        }
    }

    /**
     * View a job data file
     */
    def view = {
        log.info "View file jobdata = " + params.getLong('id')
        JobData jobData = jobDataService.read(params.getLong('id'))
        if(!jobData) {
            responseNotFound("JobData", params.id)
        } else {
            //response.setContentType "image/png"
            response.setHeader "Content-disposition", "inline"
            response.outputStream << jobDataService.read(jobData)
            response.outputStream.flush()
        }
    }

    /**
     * Download a job data file
     */
    def download = {
        log.info "Download file jobdata = " + params.getLong('id')
        JobData jobData = jobDataService.read(params.getLong('id'))
        if(!jobData) {
            responseNotFound("JobData", params.id)
        } else {
            response.setContentType "application/octet-stream"
            response.setHeader "Content-disposition", "attachment; filename=${jobData.filename}"
            response.outputStream << jobDataService.read(jobData)
            response.outputStream.flush()
        }
    }


}
