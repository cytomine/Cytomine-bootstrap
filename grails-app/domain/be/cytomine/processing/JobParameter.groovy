package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON
import org.apache.log4j.Logger

class JobParameter  extends CytomineDomain implements Comparable{

    String value

    static belongsTo = [job: Job, softwareParameter : SoftwareParameter]

    static constraints = {
    }

    static mapping = {
        id generator: "assigned"
        softwareParameter fetch: 'join'
    }

     void checkAlreadyExist() {
        JobParameter.withNewSession {
            JobParameter jobParamAlreadyExist=JobParameter.findByJobAndSoftwareParameter(job,softwareParameter)
            if(jobParamAlreadyExist!=null && (jobParamAlreadyExist.id!=id))  throw new AlreadyExistException("Parameter " + softwareParameter?.name + " already exist fro job " + job?.id)
        }
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + JobParameter.class)
        JSON.registerObjectMarshaller(JobParameter) {
            def jobParameter = [:]
            jobParameter.id = it.id
            jobParameter.value = it.value
            jobParameter.job = it.job.id
            SoftwareParameter softwareParam =  it.softwareParameter
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
        if (!json.value.toString().equals("null"))
            domain.value = json.value

        domain.job = Job.get(json.job.toString())
        domain.softwareParameter = SoftwareParameter.get(json.softwareParameter.toString())

        if(!domain.job) throw new WrongArgumentException("Job ${json.job.toString()} doesn't exist!")
        if(!domain.softwareParameter) throw new WrongArgumentException("SoftwareParameter ${json.softwareParameter.toString()} doesn't exist!")
        return domain;
    }

    int compareTo(Object t) {
        if(this.softwareParameter.index<t.softwareParameter.index) return -1
        else if(this.softwareParameter.index>t.softwareParameter.index) return 1
        else return 0
    }
}
