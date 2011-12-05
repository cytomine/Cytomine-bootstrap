package be.cytomine.processing

import grails.converters.JSON

class SoftwareParameter {

    Software software
    String name
    String type
    String defaultValue
    Boolean required = false

    static belongsTo = [Software]

    static constraints = {
        name (nullable: false, blank : false)
        type (inList: ["String", "Boolean", "Number"])
        defaultValue (nullable: true, blank : true)
    }

    String toString() {
        return (this as JSON).toString()
    }

     static void registerMarshaller() {
        println "Register custom JSON renderer for " + SoftwareParameter.class
        JSON.registerObjectMarshaller(SoftwareParameter) {
            def softwareParameter = [:]
            softwareParameter.name = it.name
            softwareParameter.type = it.type
            softwareParameter.defaultValue = it.defaultValue
            softwareParameter.required = it.required
            return softwareParameter
        }
    }
}
