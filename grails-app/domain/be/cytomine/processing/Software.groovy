package be.cytomine.processing

import be.cytomine.CytomineDomain

class Software extends CytomineDomain {

    String name

    static hasMany = [softwareUsers: SoftwareUsers, softwareProjects: SoftwareProjects]

    static constraints = {
        name(nullable: false, unique: true)
    }

    String toString() {
        name
    }

}
