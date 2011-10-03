package be.cytomine.processing

class JobData {

    String key
    byte[] data
    Job job

    static belongsTo = [ job : Job]

    static constraints = {
        key(nullable: false, blank : false, unique: false)
        data(nullable : false)
    }

}
