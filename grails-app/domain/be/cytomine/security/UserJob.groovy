package be.cytomine.security

import be.cytomine.processing.Job
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.jsondoc.core.annotation.ApiObject

@ApiObject(name = "user job", description="A cytomine software user")
class UserJob extends SecUser {

    def springSecurityService

    /**
     * Human user that launch algo
     */
    @ApiObjectFieldLight(description = "Human user that launch the job")
    User user

    @ApiObjectFieldLight(description = "The related job")
    Job job

    @ApiObjectFieldLight(description = "The rate succes of the job", useForCreation = false, defaultValue = "-1")
    double rate = -1d

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "humanUsername", description = "The username of the user that launch this job",allowedType = "string",useForCreation = false)
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
