package be.cytomine.security

import be.cytomine.processing.Job
import grails.converters.JSON
import org.apache.log4j.Logger

class UserJob extends SecUser {

    def springSecurityService

    /**
     * Human user that launch algo
     */
    User user

    Job job

    double rate = -1d

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
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + UserJob.class)
        JSON.registerObjectMarshaller(UserJob) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['username'] = it.username
            returnArray['humanUsername']= it.humanUsername()
            returnArray['publicKey'] = it.publicKey
            returnArray['privateKey'] = it.privateKey
            returnArray['job'] = it.job?.id
            returnArray['user'] = it.user?.id
            returnArray['rate'] = it.rate
            returnArray['created'] = it.created?.time?.toString()
            returnArray['updated'] = it.updated?.time?.toString()
            returnArray['algo'] = it.algo()
            return returnArray
        }
    }
}
