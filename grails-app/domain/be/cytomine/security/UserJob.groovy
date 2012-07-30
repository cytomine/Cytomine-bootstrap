package be.cytomine.security

import be.cytomine.processing.Job
import grails.converters.JSON

class UserJob extends SecUser {

    def springSecurityService

    User user
    Job job
    double rate = -1d

    static constraints = {
        job(nullable: true)
    }

    String toString() {
        "Job"+ id + " ( " + user.toString() + " )"
    }

    String realUsername() {
        return "$user.firstname $user.lastname ($user.username)"
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

    def userGroups() {
        user.userGroups()
    }

    def groups() {
        user.groups()
    }

    def ontologies() {
        user.ontologies()
    }

    def projects() {
        user.projects()
    }

    def abstractimages() {
        user.abstractimages()
    }

    def abstractimage(int max, int first, String col, String order, String filename, Date dateAddedStart, Date dateAddedStop) {
        user.abstractimage(max,first,col,order,filename,dateAddedStart,dateAddedStop)
    }

    def slides() {
        user.slides()
    }

    def slides(int max, int first, String col, String order) {
        user.slides(max,first,col,order)
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + UserJob.class
        JSON.registerObjectMarshaller(UserJob) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['username'] = it.username
            try {
                returnArray['realUsername']= it.realUsername()
            } catch (Exception e) {println e}
            returnArray['publicKey'] = it.publicKey
            returnArray['privateKey'] = it.privateKey
            returnArray['job'] = it.job?.id
            returnArray['user'] = it.user?.id
            returnArray['rate'] = it.rate
            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null
            return returnArray
        }
    }
}
