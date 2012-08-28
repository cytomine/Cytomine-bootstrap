package be.cytomine.api.processing

import grails.plugins.springsecurity.Secured

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import grails.converters.JSON
import grails.util.GrailsUtil
import be.cytomine.Exception.ServerException
import be.cytomine.processing.JobDataBinaryValue
import org.springframework.web.multipart.MultipartHttpServletRequest

class RestJobDataController extends RestController {

    def jobDataService
    def grailsApplication
    def domainService

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
        JobDataBinaryValue value = new JobDataBinaryValue(jobData:jobData)
        domainService.saveDomain(value)
        jobData.value = value
        domainService.saveDomain(jobData)
        println "UPLOADING...."

        byte[] bytes = null

        if(request instanceof MultipartHttpServletRequest) {
            def file = request.getFile('files[]')
            if (!file) {
                //error in upload

            }
            bytes = file.getBytes()
        } else {
            bytes = request.inputStream.bytes
        }

        if(!jobData) responseNotFound("JobData", params.id)
        else {
            if(!grailsApplication.config.cytomine.jobdata.filesystem)
                jobData = saveInDatabase(jobData,bytes)
            else
                jobData = saveInFileSystem(jobData,bytes)
            responseSuccess(jobData)
        }
    }

    def download = {
        log.info "Download file jobdata = " + params.getLong('id')
        JobData jobData = JobData.read(params.getLong('id'))
        if(!jobData) responseNotFound("JobData", params.id)
        else {
            response.setHeader "Content-disposition", "attachment; filename=${jobData.filename}"
            response.contentType = "application/octet-stream"
            if(!grailsApplication.config.cytomine.jobdata.filesystem)
                response.outputStream << readFromDatabase(jobData)
            else
                response.outputStream << readFromFileSystem(jobData)
            response.outputStream.flush()
        }
    }

    private JobData saveInDatabase(JobData jobData, byte[] data) {
        jobData.value.data = data
        jobData.value.save(flush: true)
        return jobData
    }

    private byte[] readFromDatabase(JobData jobData) {
        return jobData.value.data
    }

    private void saveInFileSystem(JobData jobData, byte[] data) {
        File dir = new File(grailsApplication.config.cytomine.jobdata.filesystemPath + GrailsUtil.environment + "/"+jobData.job.id +"/" + jobData.key )
        File f = new File(dir.getAbsolutePath()+ "/"+jobData.filename)

        try {
            log.info "save data in file = " + f.absolutePath

            dir.mkdirs()

            log.info "write data in file = " + f.absolutePath
            new FileOutputStream(f).withWriter { w ->
                w << new BufferedInputStream( new ByteArrayInputStream(data) )
            }
            log.info "end file"
        } catch(Exception e) {
            e.printStackTrace()
            throw new ServerException("Cannot create file: " + e)
        }

    }
    private byte[] readFromFileSystem(JobData jobData)  {
        File f = new File(grailsApplication.config.cytomine.jobdata.filesystemPath + GrailsUtil.environment + "/"+ jobData.job.id +"/" + jobData.key + "/"+ jobData.filename)
        try {
            log.info "read data in file = " + f.absolutePath
            InputStream inputStream = new FileInputStream(f);

            int offset = 0;
            int bytesRead;
            // Get the byte array
            byte[] bytes = new byte[(int) f.length()];
            // Iterate the byte array
            while (offset < bytes.length && (bytesRead = inputStream.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += bytesRead;
            }
            // Close after use
            inputStream.close();

            return bytes
        } catch(Exception e) {
            throw new ServerException("Cannot read file: " + e)
        }

    }
}
