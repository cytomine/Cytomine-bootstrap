package be.cytomine.processing

import be.cytomine.CytomineDomain
import grails.converters.JSON
import be.cytomine.Exception.WrongArgumentException

class Software extends CytomineDomain {

    String name

    static hasMany = [softwareProjects: SoftwareProject, softwareParameters : SoftwareParameter]

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

    static Software createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static Software createFromData(jsonSoftware) {
        def software = new Software()
        getFromData(software, jsonSoftware)
    }

    static Software getFromData(software, jsonSoftware) {
        if (!jsonSoftware.name.toString().equals("null"))
            software.name = jsonSoftware.name
        else throw new WrongArgumentException("Software name cannot be null")
        return software;
    }

}
