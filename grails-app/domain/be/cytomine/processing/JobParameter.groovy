package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.command.ResponseService
import be.cytomine.security.UserJob
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * A job parameter is an instance of software parameter
 * When a job is created, we create a job parameter for each software parameter.
 */
@ApiObject(name = "job parameter", description = "A job parameter is an instance of software parameter. When a job is created, we create a job parameter for each software parameter.")
class JobParameter extends CytomineDomain implements Comparable {

    /**
     * Job parameter value
     */
    @ApiObjectFieldLight(description = "Job parameter value")
    String value

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "job", description = "The job of the parameter",allowedType = "long",useForCreation = true),
        @ApiObjectFieldLight(apiFieldName = "softwareParameter", description = "The parameter id of the software",allowedType = "long",useForCreation = true),
        @ApiObjectFieldLight(apiFieldName = "name", description = "The parameter name", useForCreation = false, allowedType = "string"),
        @ApiObjectFieldLight(apiFieldName = "type", description = "The parameter data type (Number, String, Date, Boolean, Domain (e.g: image instance id,...), ListDomain )", useForCreation = false, allowedType = "string"),
        @ApiObjectFieldLight(apiFieldName = "index",description = "Index for parameter position. When launching software, parameter will be send ordered by index (asc).", useForCreation = false, allowedType = "string"),
        @ApiObjectFieldLight(apiFieldName = "uri", description = "Used for UI. If parameter has '(List)Domain' type, the URI will provide a list of choice. E.g. if uri is 'api/project.json', the choice list will be cytomine project list", useForCreation = false, allowedType = "string"),
        @ApiObjectFieldLight(apiFieldName = "uriPrintAttribut", description = "Used for UI. JSON Fields to print in choice list. E.g. if uri is api/project.json and uriPrintAttribut is 'name', the choice list will contains project name ", useForCreation = false, allowedType = "string"),
        @ApiObjectFieldLight(apiFieldName = "uriSortAttribut", description = "Used for UI. JSON Fields used to sort choice list. E.g. if uri is api/project.json and uriSortAttribut is 'id', projects will be sort by id (not by name) ", useForCreation = false, allowedType = "string")
    ])
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
            getDataFromDomain(it)
        }
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['value'] = domain?.value
        returnArray['job'] = domain?.job?.id
        SoftwareParameter softwareParameter = domain?.softwareParameter
        returnArray['softwareParameter'] = softwareParameter?.id
        returnArray['name'] = softwareParameter?.name
        returnArray['type'] = softwareParameter?.type
        returnArray['index'] = softwareParameter?.index
        returnArray['uri'] = softwareParameter?.uri
        returnArray['uriPrintAttribut'] = softwareParameter?.uriPrintAttribut
        returnArray['uriSortAttribut'] = softwareParameter?.uriSortAttribut
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return job.container();
    }
}
