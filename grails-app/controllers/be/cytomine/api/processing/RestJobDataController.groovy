package be.cytomine.api.processing

import be.cytomine.Exception.ServerException
import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.JobDataBinaryValue
import grails.converters.JSON
import grails.util.GrailsUtil
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
        delete(jobDataService, JSON.parse("{id : $params.id}"))
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
            if(!grailsApplication.config.cytomine.jobdata.filesystem) {
                jobData = saveInDatabase(jobData,bytes)
            } else {
                jobData = saveInFileSystem(jobData,bytes)
            }
            responseSuccess(jobData)
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
            response.setHeader "Content-disposition", "attachment; filename=${jobData.filename}"
            response.contentType = "application/octet-stream"
            if(!grailsApplication.config.cytomine.jobdata.filesystem) {
                response.outputStream << readFromDatabase(jobData)
            } else {
                response.outputStream << readFromFileSystem(jobData)
            }
            response.outputStream.flush()
        }
    }

    /**
     * Save a job data on database
     * @param jobData Job data description
     * @param data Data bytes
     * @return job data
     */
    private JobData saveInDatabase(JobData jobData, byte[] data) {
        jobData.value.data = data
        jobData.value.save(flush: true)
        jobData.size = data.length;
        return jobData
    }

    /**
     * Read job data files from database
     * @param jobData Job data description
     * @return Job data bytes
     */
    private byte[] readFromDatabase(JobData jobData) {
        return jobData.value.data
    }

    /**
     * Save a job data on disk file system
     * @param jobData Job data description
     * @param data data bytes
     */
    private void saveInFileSystem(JobData jobData, byte[] data) {
        File dir = new File(grailsApplication.config.cytomine.jobdata.filesystemPath + GrailsUtil.environment + "/"+jobData.job.id +"/" + jobData.key )
        File f = new File(dir.getAbsolutePath()+ "/"+jobData.filename)
        jobData.size = data.length;
        jobData.save(flush:true)
        try {
            dir.mkdirs()
            new FileOutputStream(f).withWriter { w ->
                w << new BufferedInputStream( new ByteArrayInputStream(data) )
            }
        } catch(Exception e) {
            e.printStackTrace()
            throw new ServerException("Cannot create file: " + e)
        }
    }

    /**
     * Read job data files from disk file system
     * @param jobData Job data description
     * @return Job data bytes
     */
    private byte[] readFromFileSystem(JobData jobData)  {
        File f = new File(grailsApplication.config.cytomine.jobdata.filesystemPath + GrailsUtil.environment + "/"+ jobData.job.id +"/" + jobData.key + "/"+ jobData.filename)
        try {
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
