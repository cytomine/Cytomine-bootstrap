package be.cytomine.api.processing

import grails.plugins.springsecurity.Secured

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import grails.converters.JSON

class RestJobDataController extends RestController {

    def jobDataService

    def list = {
         responseSuccess(jobDataService.list())
     }

     def listByJob = {
         Job job = Job.read(params.long('id'))
         if(job) responseSuccess(jobDataService.list(job))
         else responseNotFound("Job", params.id)
     }

     def show = {
         JobData jobData = jobDataService.read(params.long('id'), new JobData())
         if (jobData) {
             responseSuccess(jobData)
         }
         else responseNotFound("JobData", params.id)
     }

     def add = {
         add(jobDataService, request.JSON)
     }

     def update = {
         update(jobDataService, request.JSON)
     }

     def delete = {
         delete(jobDataService, JSON.parse("{id : $params.id}"))
     }

    def upload = {
        log.info "Upload file = " + params.getLong('id')
        JobData jobData = JobData.read(params.getLong('id'))
        jobData = saveInDatabase(jobData,request.inputStream.bytes)
        responseSuccess(jobData)
    }
    
    def download = {
        log.info "Download file jobdata = " + params.getLong('id')
        JobData jobData = JobData.read(params.getLong('id'))
        response.setHeader "Content-disposition", "attachment; filename=${jobData.filename}"
        response.contentType = "application/octet-stream"
        response.outputStream << readFromDatabase(jobData)
        response.outputStream.flush()
    }

    private JobData saveInDatabase(JobData jobData, byte[] data) {
        jobData.data = data;
        jobData.save(flush: true)
        return jobData
    }

    private byte[] readFromDatabase(JobData jobData) {
        return jobData.data
    }

    private void saveInFileSystem() {

    }
    private byte[] readFromFileSystem()  {
        return null;
    }
}
