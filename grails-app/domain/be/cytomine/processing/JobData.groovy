package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON

class JobData extends CytomineDomain {

    String key
    byte[] data
    String filename

    static belongsTo = [job: Job]

    static constraints = {
        key(nullable: false, blank: false, unique: false)
        data(nullable: true)
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
        println "Register custom JSON renderer for " + JobData.class
        JSON.registerObjectMarshaller(JobData) { jobData ->
            def returnArray = [:]
            returnArray['class'] = jobData.class
            returnArray['id'] = jobData.id
            returnArray['key'] = jobData.key
            returnArray['job'] = jobData.job.id
            returnArray['filename'] = jobData.filename
            returnArray['created'] = jobData.created ? jobData.created.time.toString() : null
            returnArray['updated'] = jobData.updated ? jobData.updated.time.toString() : null
            return returnArray
        }
    }

}
