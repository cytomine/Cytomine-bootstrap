package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.project.Project
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * A job parameter is an instance of software parameter
 * When a job is created, we create a job parameter for each software parameter.
 */
class JobParameter extends CytomineDomain implements Comparable {

    /**
     * Job parameter value
     */
    String value

    static belongsTo = [job: Job, softwareParameter: SoftwareParameter]

    static constraints = {
    }

    static mapping = {
        id generator: "assigned"
        softwareParameter fetch: 'join'
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        JobParameter.withNewSession {
            JobParameter jobParamAlreadyExist = JobParameter.findByJobAndSoftwareParameter(job, softwareParameter)
            if (jobParamAlreadyExist != null && (jobParamAlreadyExist.id != id)) {
                throw new AlreadyExistException("Parameter " + softwareParameter?.name + " already exist for job " + job?.id)
            }
        }
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static JobParameter createFromDataWithId(def json) {
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
    static JobParameter createFromData(def json) {
        def jobParameter = new JobParameter()
        insertDataIntoDomain(jobParameter, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static JobParameter insertDataIntoDomain(def domain, def json) {
        domain.value = JSONUtils.getJSONAttrStr(json, 'value')
        domain.job = JSONUtils.getJSONAttrDomain(json, "job", new Job(), true)
        domain.softwareParameter = JSONUtils.getJSONAttrDomain(json, "softwareParameter", new SoftwareParameter(), true)
        return domain;
    }

    int compareTo(Object t) {
        if (this.softwareParameter.index < t.softwareParameter.index) return -1
        else if (this.softwareParameter.index > t.softwareParameter.index) return 1
        else return 0
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + JobParameter.class)
        JSON.registerObjectMarshaller(JobParameter) {
            def jobParameter = [:]
            jobParameter.id = it.id
            jobParameter.value = it.value
            jobParameter.job = it.job.id
            SoftwareParameter softwareParam = it.softwareParameter
            jobParameter.softwareParameter = softwareParam.id
            jobParameter.name = softwareParam.name
            jobParameter.type = softwareParam.type
            jobParameter.index = softwareParam.index
            jobParameter.uri = softwareParam.uri
            jobParameter.uriPrintAttribut = softwareParam.uriPrintAttribut
            jobParameter.uriSortAttribut = softwareParam.uriSortAttribut
            return jobParameter
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
