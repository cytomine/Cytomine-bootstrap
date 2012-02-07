package be.cytomine.security

import be.cytomine.processing.Job
import grails.converters.JSON

class UserJob extends SecUser {

    def springSecurityService

    User user
    Job job


    static constraints = {
        job(nullable: true)
    }

    String toString() {
        "Job"+ id + " ( " + user.toString() + " )"
    }

    def beforeInsert() {
        super.beforeInsert()
    }

    def beforeUpdate() {
        super.beforeUpdate()
    }

    boolean algo() {
        return true
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + UserJob.class
        JSON.registerObjectMarshaller(UserJob) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['username'] = it.username
            returnArray['publicKey'] = it.publicKey
            returnArray['privateKey'] = it.privateKey
            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null
            return returnArray
        }
    }
}
