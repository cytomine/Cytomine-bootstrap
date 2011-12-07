package be.cytomine.security

import be.cytomine.processing.Job

class UserJob extends  SecUser {

    User user
    Job job

    static constraints = {
    }

    String toString() {
        "Job"+ id + " ( " + user.toString() + " )"
    }
}
