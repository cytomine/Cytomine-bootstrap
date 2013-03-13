package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
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
        sort "id"
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
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static JobParameter insertDataIntoDomain(def json,def domain=new JobParameter()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
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
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return job.container();
    }
}
