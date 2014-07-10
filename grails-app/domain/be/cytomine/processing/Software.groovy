package be.cytomine.processing
import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields
import org.restapidoc.annotation.RestApiObject

/**
 * Software is an application that can read/add/update/delete data from cytomine
 * Each time a software is launch, we create a job instance
 */
@RestApiObject(name = "software", description = "Software is an application that can read/add/update/delete data from cytomine. Each time a software is launch, we create a job instance")
class Software extends CytomineDomain {


    def softwareParameterService

    /**
     * Application name
     */
    @RestApiObjectField(description = "The software name")
    String name

    /**
     * Service that will be call when we launch the software
     * This server will, for example, launch a binary file with ssh
     */
    def service

    /**
     * Service name used to load service
     */
    @RestApiObjectField(description = "Service name used to load software and create job", mandatory = false)
    String serviceName

    /**
     * Type of result page
     * For UI client, we load a specific page for each software to print data (charts, listing,...)
     */
    @RestApiObjectField(description = "For UI client: Type of result page. We load a specific page for each software to print data (charts, listing,...)", mandatory = false)
    String resultName

    /**
     * Software info
     */
    @RestApiObjectField(description = "Software info", mandatory = false)
    String description

    /**
     * Result sample (image, report, ...). Still used?????
     */
    byte[] resultSample

    /**
     * Command to execute software
     */
    String executeCommand

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "parameters", description = "List of 'software parameter' for this software (sort by index asc)",allowedType = "list",useForCreation = false),
        @RestApiObjectField(apiFieldName = "numberOfJob", description = "The number of job for this software",allowedType = "long",useForCreation = false),
        @RestApiObjectField(apiFieldName = "numberOfNotLaunch", description = "The number of job not launch for this software",allowedType = "long",useForCreation = false),
        @RestApiObjectField(apiFieldName = "numberOfNotLaunch", description = "The number of job not launch for this software",allowedType = "long",useForCreation = false),
        @RestApiObjectField(apiFieldName = "numberOfInQueue", description = "The number of job in queue for this software",allowedType = "long",useForCreation = false),
        @RestApiObjectField(apiFieldName = "numberOfRunning", description = "The number of job currently running for this software",allowedType = "long",useForCreation = false),
        @RestApiObjectField(apiFieldName = "numberOfSuccess", description = "The number of job finished with success for this software",allowedType = "long",useForCreation = false),
        @RestApiObjectField(apiFieldName = "numberOfFailed", description = "The number of job failed for this software",allowedType = "long",useForCreation = false),
        @RestApiObjectField(apiFieldName = "numberOfIndeterminate", description = "The number of job in indeterminate status for this software",allowedType = "long",useForCreation = false),
        @RestApiObjectField(apiFieldName = "numberOfWait", description = "The number of job waiting for this software",allowedType = "long",useForCreation = false),
    ])
    static transients = []


    static constraints = {
        name(nullable: false, unique: true)
        resultName(nullable:true)
        description(nullable:true, blank : false, maxSize: 65560)
        resultSample(nullable:true)
        executeCommand(nullable: true)
    }

    static mapping = {
        id generator: "assigned"
        description type: 'text'
        sort "id"
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
        domain.serviceName = JSONUtils.getJSONAttrStr(json, 'serviceName')
        domain.resultName = JSONUtils.getJSONAttrStr(json, 'resultName')
        domain.executeCommand = JSONUtils.getJSONAttrStr(json, 'executeCommand')

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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray['serviceName'] = domain?.serviceName
        returnArray['resultName'] = domain?.resultName
        returnArray['description'] = domain?.description
        returnArray['executeCommand'] = domain?.executeCommand
        try {
            returnArray['parameters'] = SoftwareParameter.findAllBySoftwareAndSetByServer(domain, false, [sort : "index", order : "asc"])
            returnArray['numberOfJob'] = Job.countBySoftware(domain)
            returnArray['numberOfNotLaunch'] = Job.countBySoftwareAndStatus(domain,Job.NOTLAUNCH)
            returnArray['numberOfInQueue'] = Job.countBySoftwareAndStatus(domain,Job.INQUEUE)
            returnArray['numberOfRunning'] = Job.countBySoftwareAndStatus(domain,Job.RUNNING)
            returnArray['numberOfSuccess'] = Job.countBySoftwareAndStatus(domain,Job.SUCCESS)
            returnArray['numberOfFailed'] = Job.countBySoftwareAndStatus(domain,Job.FAILED)
            returnArray['numberOfIndeterminate'] = Job.countBySoftwareAndStatus(domain,Job.INDETERMINATE)
            returnArray['numberOfWait'] = Job.countBySoftwareAndStatus(domain,Job.WAIT)
        } catch(Exception e) { }
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return this;
    }

}
