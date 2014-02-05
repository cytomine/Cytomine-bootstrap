package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * Data created by a job
 * This concerns only data files (annotation or term are store in domain database)
 */
@ApiObject(name = "job data", description = "Data created by a job. This concerns only data files (annotation or term are store in domain database). If config cytomine.jobdata.filesystem is true, file are stored in filesystem, otherwise they are store in database.")
class JobData extends CytomineDomain {

    /**
     * File key (what's the file)
     */
    @ApiObjectFieldLight(description = "File key (what's the file)")
    String key

    /**
     * Data filename with extension
     */
    @ApiObjectFieldLight(description = "Data filename with extension")
    String filename

    /**
     * ???
     */
    @ApiObjectFieldLight(description = "File full path if 'cytomine.jobdata.filesystem' config is true", useForCreation = false)
    String dir

    /**
     * If data file is store on database (blob field), link to the file
     */
    @ApiObjectFieldLight(description = "File data (from blob field) if 'cytomine.jobdata.filesystem' config is false", useForCreation = false)
    JobDataBinaryValue value

    /**
     * Data size (in Bytes)
     */
    @ApiObjectFieldLight(description = "Data size (in Bytes)", useForCreation = false)
    Long size

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "job", description = "The job that store the data",allowedType = "long",useForCreation = true)
    ])
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
        sort "id"
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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['key'] = domain?.key
        returnArray['job'] = domain?.job?.id
        returnArray['filename'] = domain?.filename
        returnArray['size'] = domain?.size
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
