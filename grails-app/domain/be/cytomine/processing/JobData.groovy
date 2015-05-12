package be.cytomine.processing

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.CytomineDomain
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields

/**
 * Data created by a job
 * This concerns only data files (annotation or term are store in domain database)
 */
@RestApiObject(name = "job data", description = "Data created by a job. This concerns only data files (annotation or term are store in domain database). If config cytomine.jobdata.filesystem is true, file are stored in filesystem, otherwise they are store in database.")
class JobData extends CytomineDomain {

    /**
     * File key (what's the file)
     */
    @RestApiObjectField(description = "File key (what's the file)")
    String key

    /**
     * Data filename with extension
     */
    @RestApiObjectField(description = "Data filename with extension")
    String filename

    /**
     * ???
     */
    @RestApiObjectField(description = "File full path if 'cytomine.jobdata.filesystem' config is true", useForCreation = false)
    String dir

    /**
     * If data file is store on database (blob field), link to the file
     */
    @RestApiObjectField(description = "File data (from blob field) if 'cytomine.jobdata.filesystem' config is false", useForCreation = false)
    JobDataBinaryValue value

    /**
     * Data size (in Bytes)
     */
    @RestApiObjectField(description = "Data size (in Bytes)", useForCreation = false)
    Long size

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "job", description = "The job that store the data",allowedType = "long",useForCreation = true)
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
