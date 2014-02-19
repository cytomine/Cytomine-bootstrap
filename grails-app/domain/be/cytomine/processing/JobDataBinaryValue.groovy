package be.cytomine.processing

import be.cytomine.CytomineDomain

//* Its better to have a specific domain link than a simple 'byte[] value'.
//* Each time we load a job data

/**
 * A job data file stored in database
 * Used in JobData domain, it's better to do:
 * -JobDataBinaryValue value
 * Than
 * -byte[] value
 * With the first one, we only load the JobDataBinaryValue id
 * With the second one, evey time we load a JobData, we load the full byte[] automaticaly
 */
class JobDataBinaryValue extends CytomineDomain {

    /**
     * File data
     */
    byte[] data

    static belongsTo = [jobData: JobData]

    static constraints = {
        data(nullable: true)
    }
    static mapping = {
        id generator: "assigned"
        sort "id"
    }

    public beforeValidate() {
        super.beforeValidate()
    }
}
