package be.cytomine.processing

import be.cytomine.CytomineDomain
import grails.converters.JSON

class Software extends CytomineDomain {

    String name

    static hasMany = [softwareUsers: SoftwareUsers, softwareProjects: SoftwareProjects, softwareParameters : SoftwareParameter]

    static constraints = {
        name(nullable: false, unique: true)
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Software.class
        JSON.registerObjectMarshaller(Software) {
            def software = [:]
            software.id = it.id
            software.name = it.name
            software.parameters = it.softwareParameters
            return software
        }
    }

    String toString() {
        name
    }

}
