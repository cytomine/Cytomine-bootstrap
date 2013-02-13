package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Data created by a job
 * This concerns only data files (annotation or term are store in domain database)
 */
class JobData extends CytomineDomain {

    /**
     * File key (what's the file)
     */
    String key

    /**
     * Data filename with extension
     */
    String filename

    /**
     * ???
     */
    String dir

    /**
     * If data file is store on database (blob field), link to the file
     */
    JobDataBinaryValue value

    /**
     * Data size (in Bytes)
     */
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
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static JobData insertDataIntoDomain(def json, def domain = new JobData()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.key = JSONUtils.getJSONAttrStr(json, 'key', true)
        domain.filename = JSONUtils.getJSONAttrStr(json, 'filename',true)
        domain.job = JSONUtils.getJSONAttrDomain(json, "job", new Job(), true)
        return domain
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + JobData.class)
        JSON.registerObjectMarshaller(JobData) { jobData ->
            def returnArray = [:]
            returnArray['class'] = jobData.class
            returnArray['id'] = jobData.id
            returnArray['key'] = jobData.key
            returnArray['job'] = jobData.job.id
            returnArray['filename'] = jobData.filename
            returnArray['size'] = jobData.size
            returnArray['created'] = jobData.created?.time?.toString()
            returnArray['updated'] = jobData.updated?.time?.toString()
            return returnArray
        }
    }

    /**
     * Return domain project (annotation project, image project...)
     * By default, a domain has no project.
     * You need to override projectDomain() in domain class
     * @return Domain project
     */
    public Project projectDomain() {
        return job.project;
    }

}
