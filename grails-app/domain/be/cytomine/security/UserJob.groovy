package be.cytomine.security

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

import be.cytomine.processing.Job
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields

@RestApiObject(name = "user job", description="A cytomine software user")
class UserJob extends SecUser {

    def springSecurityService

    /**
     * Human user that launch algo
     */
    @RestApiObjectField(description = "Human user that launch the job")
    User user

    @RestApiObjectField(description = "The related job")
    Job job

    @RestApiObjectField(description = "The rate succes of the job", useForCreation = false, defaultValue = "-1")
    double rate = -1d

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "humanUsername", description = "The username of the user that launch this job",allowedType = "string",useForCreation = false)
    ])
    static transients = []

    static constraints = {
        job(nullable: true)
    }


    def beforeInsert() {
        super.beforeInsert()
    }

    def beforeUpdate() {
        super.beforeUpdate()
    }

    String toString() {
        "Job"+ id + " ( " + user.toString() + " )"
    }

    /**
     * Username of the human user back to this user
     * If User => humanUsername is username
     * If Algo => humanUsername is user that launch algo username
     */
    String humanUsername() {
        return user?.username
    }

    /**
     * Check if user is a job
     */
    boolean algo() {
        return true
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = SecUser.getDataFromDomain(domain)
        returnArray['humanUsername']= domain?.humanUsername()
        returnArray['publicKey'] = domain?.publicKey
        returnArray['privateKey'] = domain?.privateKey
        returnArray['job'] = domain?.job?.id
        returnArray['user'] = domain?.user?.id
        returnArray['rate'] = domain?.rate
        returnArray
    }
}
