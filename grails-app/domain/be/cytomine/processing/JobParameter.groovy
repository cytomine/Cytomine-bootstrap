package be.cytomine.processing

class JobParameter {

    String value

    static belongsTo = [job: Job, softwareParameter : SoftwareParameter]

    static constraints = {
    }
}
