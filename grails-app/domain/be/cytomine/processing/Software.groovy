package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Software is an application that can read/add/update/delete data from cytomine
 * When a software is launch, we add a
 */
class Software extends CytomineDomain {

    /**
     * Application name
     */
    String name

    /**
     * Service that will be call when we launch the software
     * This server will, for example, launch a binary file with ssh
     */
    def service

    /**
     * Service name used to load service
     */
    String serviceName

    /**
     * Type of result page
     * For UI client, we load a specific page for each software to print data (charts, listing,...)
     */
    String resultName

    /**
     * Software info
     */
    String description

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
         //load service thanks to serviceName from DB
        if (!service) {
            service = grailsApplication.getMainContext().getBean(serviceName)
        }
     }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        Software.withNewSession {
            if(name) {
                Software softwareSameName = Software.findByName(name)
                if(softwareSameName && (softwareSameName.id!=id))  {
                    throw new AlreadyExistException("Software "+softwareSameName.name + " already exist!")
                }
            }

        }
    }

    String toString() {
        name
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Software insertDataIntoDomain(def json,def domain=new Software()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name')
        domain.description = JSONUtils.getJSONAttrStr(json, 'description')
        domain.serviceName = JSONUtils.getJSONAttrStr(json, 'serviceName',true)
        domain.resultName = JSONUtils.getJSONAttrStr(json, 'resultName')

        def service
        try {
            service = grailsApplication.getMainContext().getBean(json.serviceName)
        } catch(Exception e) {
           throw new WrongArgumentException("Software service-name cannot be launch:"+e)
        }
        if(!service)  {
            throw new WrongArgumentException("Software service-name cannot be found with name:"+json.serviceName)
        }

        return domain;
    }


    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
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

    /**
     * Return domain software (parameter software, job software...)
     * By default, a domain has no software linked.
     * You need to override softwareDomain() in domain class
     * @return Domain software
     */
    public Software softwareDomain() {
        return this;
    }

}
