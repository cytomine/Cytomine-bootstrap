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

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static JobData createFromDataWithId(def json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static JobData createFromData(def json) {
        def jobdata = new JobData()
        insertDataIntoDomain(jobdata, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static JobData insertDataIntoDomain(def domain, def json) {
        String key = json.key.toString()
        if (!key.equals("null"))
            domain.key = json.key
        else throw new WrongArgumentException("Key name cannot be null")

        domain.filename = json.filename

        if (!json.job.toString().equals("null"))
            domain.job = Job.read(json.job)

        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
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
