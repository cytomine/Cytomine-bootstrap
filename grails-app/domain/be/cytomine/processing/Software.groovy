package be.cytomine.processing

import be.cytomine.CytomineDomain
import grails.converters.JSON
import be.cytomine.Exception.WrongArgumentException
import org.codehaus.groovy.grails.commons.ApplicationHolder

import be.cytomine.Exception.AlreadyExistException

class Software extends CytomineDomain {

    String name
    String serviceName
    def service
    def projectService
    String resultName

    static hasMany = [softwareProjects: SoftwareProject, softwareParameters : SoftwareParameter]

    static constraints = {
        name(nullable: false, unique: true)
        resultName(nullable:true)
    }

     def afterLoad = {
            if (!service) {
                service = ApplicationHolder.application.getMainContext().getBean(serviceName)
            }

     }

    void checkAlreadyExist() {
        Software.withNewSession {
            Software softwareSameName = Software.findByName(name)
            if(softwareSameName && (softwareSameName.id!=id))  throw new AlreadyExistException("Software "+softwareSameName.name + " already exist!")
        }
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Software.class
        JSON.registerObjectMarshaller(Software) {
            def software = [:]
            software.id = it.id
            software.name = it.name
            software.serviceName = it.serviceName
            software.resultName = it.resultName
            try {
                software.parameters = SoftwareParameter.findAllBySoftware(it,[sort: "name",order: "asc"])
                software.numberOfJob = Job.countBySoftware(it);

                software.numberOfNotLaunch = Job.countBySoftwareAndStatus(it,Job.NOTLAUNCH);
                software.numberOfInQueue = Job.countBySoftwareAndStatus(it,Job.INQUEUE);
                software.numberOfRunning = Job.countBySoftwareAndStatus(it,Job.RUNNING);
                software.numberOfSuccess = Job.countBySoftwareAndStatus(it,Job.SUCCESS);
                software.numberOfFailed = Job.countBySoftwareAndStatus(it,Job.FAILED);
                software.numberOfIndeterminate = Job.countBySoftwareAndStatus(it,Job.INDETERMINATE);
                software.numberOfWait = Job.countBySoftwareAndStatus(it,Job.WAIT);

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
