package be.cytomine.processing

import be.cytomine.SequenceDomain

class Software extends SequenceDomain {

    String name

    static hasMany = [ softwareUsers : SoftwareUsers, softwareProjects : SoftwareProjects]

    static constraints = {
        name (nullable: false, unique: true)
    }

    String toString() {
        name
    }

}
