package be.cytomine.security

import be.cytomine.processing.Job

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
}
