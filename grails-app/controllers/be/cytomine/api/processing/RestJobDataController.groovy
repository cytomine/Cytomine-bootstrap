package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.JobDataBinaryValue
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import org.jsondoc.core.pojo.ApiParamType
import org.springframework.web.multipart.MultipartHttpServletRequest

/**
 *  Controller that handle job data files
 *  Files can be saved on filesystem or in database
 */
@Api(name = "job services", description = "Methods for managing job data file. Files can be saved on filesystem or in database.")
class RestJobDataController extends RestController {

    def jobDataService

    /**
     * List all job data
     */
    @ApiMethodLight(description="Get all job data", listing = true)
    def list() {
        responseSuccess(jobDataService.list())
    }

    /**
     * List all data files info by job
     */
    @ApiMethodLight(description="Get all data for a job", listing = true)
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job id")
    ])
    def listByJob() {
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
    @ApiMethodLight(description="Get a specific data file info")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job data id")
    ])
    def show() {
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
    @ApiMethodLight(description="Add a new data file description. After that, call then 'upload' action to upload the file")
    def add() {
        add(jobDataService, request.JSON)
    }

    /**
     * Update file info
     */
    @ApiMethodLight(description="Edit a job data")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job data id")
    ])
    def update() {
        update(jobDataService, request.JSON)
    }

    /**
     * Delete file description
     */
    @ApiMethodLight(description="Delete a job data")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job data id")
    ])
    def delete() {
        delete(jobDataService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Upload a file
     */
    @ApiMethodLight(description="Upload and add file to a job data")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job data id")
    ])
    def upload() {
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
    @ApiMethodLight(description="View a job data file in the browser")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job data id")
    ])
    def view() {
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
    @ApiMethodLight(description="Download a job data file")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The job data id")
    ])
    def download() {
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
