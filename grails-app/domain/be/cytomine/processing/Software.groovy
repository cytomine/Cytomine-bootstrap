package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON

class Software extends CytomineDomain {

    String name
    String serviceName
    def service
    def projectService
    String resultName
    String description

    static hasMany = [softwareProjects: SoftwareProject, softwareParameters : SoftwareParameter]

    static constraints = {
        name(nullable: false, unique: true)
        resultName(nullable:true)
        description(nullable:true, blank : false)
    }

    static mapping = {
        description type: 'text'
    }

     def afterLoad = {
            if (!service) {
                service = grailsApplication.getMainContext().getBean(serviceName)
            }
     }

    void checkAlreadyExist() {
        Software.withNewSession {
            Software softwareSameName = Software.findByName(name)
            if(softwareSameName && (softwareSameName.id!=id))  throw new AlreadyExistException("Software "+softwareSameName.name + " already exist!")
        }
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + Software.class
        JSON.registerObjectMarshaller(Software) {
            def software = [:]
            software.id = it.id
            software.name = it.name
            software.created = it.created
            software.serviceName = it.serviceName
            software.resultName = it.resultName
            software.description = it.description
            try {
                software.parameters = SoftwareParameter.findAllBySoftware(it,[sort: "index",order: "asc"])
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
        if (!jsonSoftware.description.toString().equals("null"))
            software.description = jsonSoftware.description
        if (!jsonSoftware.serviceName.toString().equals("null"))
            software.serviceName = jsonSoftware.serviceName
        else throw new WrongArgumentException("Software service-name cannot be null")

        software.resultName = jsonSoftware.resultName
        //try to loard service if exist
        def service
        try {
            service = grailsApplication.getMainContext().getBean(jsonSoftware.serviceName)
        } catch(Exception e) {
           throw new WrongArgumentException("Software service-name cannot be launch:"+e)
        }
        if(!service)  throw new WrongArgumentException("Software service-name cannot be found with name:"+jsonSoftware.serviceName)

        return software;
    }
}
