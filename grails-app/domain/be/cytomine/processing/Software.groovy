package be.cytomine.processing

import be.cytomine.CytomineDomain
import grails.converters.JSON
import be.cytomine.Exception.WrongArgumentException
import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.util.GrailsNameUtils

class Software extends CytomineDomain {

    String name
    String serviceName
    def service
    def projectService

    static hasMany = [softwareProjects: SoftwareProject, softwareParameters : SoftwareParameter]

    static constraints = {
        name(nullable: false, unique: true)
    }

     def afterLoad = {
         println "ON LOAD:" + id
         println "ON LOAD:" + name
         println "ON LOAD:" + serviceName
            if (!service) {
                service = ApplicationHolder.application.getMainContext().getBean(serviceName)
            }

     }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Software.class
        JSON.registerObjectMarshaller(Software) {
            def software = [:]
            software.id = it.id
            software.name = it.name
            software.parameters = it.softwareParameters
            software.serviceName = it.serviceName
            try {
                software.numberOfJob = Job.countBySoftware(it);
                software.numberOfJobSuccesfull = software.numberOfJob==0? 0 : Job.countBySoftwareAndSuccessful(it,true);
                software.ratioOfJobSuccesfull = software.numberOfJob==0? 0 :  (double)(software.numberOfJobSuccesfull/software.numberOfJob)
            } catch(Exception e) { println e; e.printStackTrace()}

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
        if (!jsonSoftware.serviceName.toString().equals("null"))
            software.serviceName = jsonSoftware.serviceName
        else throw new WrongArgumentException("Software service-name cannot be null")
        //try to loard service if exist
        def service
        try {
            service = ApplicationHolder.application.getMainContext().getBean(jsonSoftware.serviceName)
        } catch(Exception e) {
           throw new WrongArgumentException("Software service-name cannot be launch:"+e)
        }
        if(!service)  throw new WrongArgumentException("Software service-name cannot be found with name:"+jsonSoftware.serviceName)

        return software;
    }
}
