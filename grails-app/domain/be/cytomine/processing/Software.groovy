package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import grails.converters.JSON
import org.apache.log4j.Logger

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
        id generator: "assigned"
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

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Software.class)
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

            } catch(Exception e) { log.info e; e.printStackTrace()}

            return software
        }
    }

    String toString() {
        name
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Software createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Software createFromData(def json) {
        def software = new Software()
        insertDataIntoDomain(software, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Software insertDataIntoDomain(def domain, def json) {
        if (!json.name.toString().equals("null"))
            domain.name = json.name
        else throw new WrongArgumentException("Software name cannot be null")
        if (!json.description.toString().equals("null"))
            domain.description = json.description
        if (!json.serviceName.toString().equals("null"))
            domain.serviceName = json.serviceName
        else throw new WrongArgumentException("Software service-name cannot be null")

        domain.resultName = json.resultName
        //try to loard service if exist
        def service
        try {
            service = grailsApplication.getMainContext().getBean(json.serviceName)
        } catch(Exception e) {
           throw new WrongArgumentException("Software service-name cannot be launch:"+e)
        }
        if(!service)  throw new WrongArgumentException("Software service-name cannot be found with name:"+json.serviceName)

        return domain;
    }
}
