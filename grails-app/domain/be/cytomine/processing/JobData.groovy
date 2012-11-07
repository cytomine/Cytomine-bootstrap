package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON
import org.apache.log4j.Logger

class JobData extends CytomineDomain {

    String key
    String filename
    String dir
    JobDataBinaryValue value
    Long size
    static belongsTo = [job: Job]

    static constraints = {
        key(nullable: false, blank: false, unique: false)
        filename(nullable: false, blank: false)
        dir(nullable: true,blank: true)
        value(nullable: true)
        size(nullable: true)
    }

    static mapping = {
        value lazy: false
        id generator: "assigned"
    }

    static JobData createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static JobData createFromData(jsonJobData) {
        def jobdata = new JobData()
        getFromData(jobdata, jsonJobData)
    }

    static JobData getFromData(jobData, jsonJobData) {
        String key = jsonJobData.key.toString()
        if (!key.equals("null"))
            jobData.key = jsonJobData.key
        else throw new WrongArgumentException("Key name cannot be null")

        jobData.filename = jsonJobData.filename

        if (!jsonJobData.job.toString().equals("null"))
            jobData.job = Job.read(jsonJobData.job)

        return jobData;
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + JobData.class)
        JSON.registerObjectMarshaller(JobData) { jobData ->
            def returnArray = [:]
            returnArray['class'] = jobData.class
            returnArray['id'] = jobData.id
            returnArray['key'] = jobData.key
            returnArray['job'] = jobData.job.id
            returnArray['filename'] = jobData.filename
            returnArray['size'] = jobData.size
            returnArray['created'] = jobData.created ? jobData.created.time.toString() : null
            returnArray['updated'] = jobData.updated ? jobData.updated.time.toString() : null
            return returnArray
        }
    }

}
