package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.command.ResponseService
import be.cytomine.project.Project
import be.cytomine.security.UserJob
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

/**
 * A job is a software instance
 * This is the execution of software with some parameters
 */
@ApiObject(name = "job", description = "A job is a software instance. This is the execution of software with some parameters")
class Job extends CytomineDomain  {
    /**
     * Job status (enum type are too heavy with GORM)
     */
    public static int NOTLAUNCH = 0
    public static int INQUEUE = 1
    public static int RUNNING = 2
    public static int SUCCESS = 3
    public static int FAILED = 4
    public static int INDETERMINATE = 5
    public static int WAIT = 6
    public static int PREVIEWED = 7

    /**
     * Job progression
     */
    @ApiObjectFieldLight(description = "The algo progression (from 0 to 100)",mandatory = false)
    int progress = 0

    /**
     * Job status (see static int)
     */
    @ApiObjectFieldLight(description = "The algo status (NOTLAUNCH = 0, INQUEUE = 1, RUNNING = 2,SUCCESS = 3,FAILED = 4,INDETERMINATE = 5,WAIT = 6,PREVIEWED = 7)",mandatory = false)
    int status = 0

    /**
     * Job Indice for this software in this project
     */
    @ApiObjectFieldLight(description = "Job Indice for this software in this project",useForCreation = false)
    int number

    /**
     * Text comment for the job status
     */
    @ApiObjectFieldLight(description = "Text comment for the job status", mandatory = false)
    String statusComment

    /**
     * Job project
     */
    @ApiObjectFieldLight(description = "The project of the job")
    Project project

    /**
     * Generic field for job rate info
     * The rate is a quality value about the job works
     */
    @ApiObjectFieldLight(description = "Generic field for job rate info. The rate is a quality value about the job works",mandatory = false)
    Double rate = null

    /**
     * Flag to see if data generate by this job are deleted
     */
    @ApiObjectFieldLight(description = "Flag to see if data generate by this job are deleted",mandatory = false)
    boolean dataDeleted = false

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "algoType", description = "The algo type based on the class name",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "softwareName", description = "The software name of the job",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "username", description = "The username of the job",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "userJob", description = "The user of the job",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "jobParameters", description = "List of job parameters for this job",allowedType = "list",useForCreation = false)
    ])
    static transients = ["url"]

    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "software", description = "The software of the job",allowedType = "long",useForCreation = true)
    ])
    static belongsTo = [software: Software]

    static constraints = {
        progress(min: 0, max: 100)
        project(nullable:true)
        statusComment(nullable:true)
        status(range: 0..7)
        rate(nullable: true)
    }

    static mapping = {
        tablePerHierarchy(true)
        id(generator: 'assigned', unique: true)
        sort "id"
        software fetch: 'join'
    }

    public beforeInsert() {
        super.beforeInsert()
        //Get the last job with the same software and same project to incr job number
        List<Job> previousJob = Job.findAllBySoftwareAndProject(software,project,[sort: "number", order: "desc",max: 1])
        if(!previousJob.isEmpty()) {
            number = previousJob.get(0).number+1
        } else {
            number = 1
        };
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */    
    static def insertDataIntoDomain(def json,def domain = new Job()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.status = JSONUtils.getJSONAttrInteger(json, 'status', 0)
        domain.progress = JSONUtils.getJSONAttrInteger(json, 'progress', 0)
        domain.statusComment = JSONUtils.getJSONAttrStr(json, 'statusComment')
        domain.project = JSONUtils.getJSONAttrDomain(json, "project", new Project(), true)
        domain.software = JSONUtils.getJSONAttrDomain(json, "software", new Software(), true)
        domain.rate = JSONUtils.getJSONAttrDouble(json, 'rate', -1)
        domain.dataDeleted =  JSONUtils.getJSONAttrBoolean(json,'dataDeleted', false)
        domain.created = JSONUtils.getJSONAttrDate(json, 'created')
        domain.updated = JSONUtils.getJSONAttrDate(json, 'updated')
        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['algoType'] = ResponseService?.getClassName(domain)?.toLowerCase()
        returnArray['progress'] = domain?.progress
        returnArray['status'] = domain?.status
        returnArray['number'] = domain?.number
        returnArray['statusComment'] = domain?.statusComment
        returnArray['project'] = domain?.project?.id
        returnArray['software'] = domain?.software?.id
        returnArray['softwareName'] = domain?.software?.name
        returnArray['rate'] = domain?.rate
        returnArray['dataDeleted'] = domain?.dataDeleted
        try {
            UserJob user = UserJob.findByJob(domain)
            returnArray['username'] = user?.humanUsername()
            returnArray['userJob'] = user?.id
            returnArray['jobParameters'] = domain?.parameters()
        } catch (Exception e) {
        }
        return returnArray
    }


    public List<JobParameter> parameters() {
        if(this.version!=null) {
            return JobParameter.findAllByJob(this,[sort: 'created'])
        } else {
            return []
        }
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return project.container()
    }

    public String toString() {
        software.getName() + "[" + software.getId() + "]"
    }

    public isNotLaunch () {
        return status == NOTLAUNCH
    }

    public isInQueue () {
        return status == INQUEUE
    }

    public isRunning () {
        return status == RUNNING
    }

    public isSuccess () {
        return status == SUCCESS
    }

    public isFailed () {
        return status == FAILED
    }

    public isIndeterminate () {
        return status == INDETERMINATE
    }

    public isWait () {
        return status == WAIT
    }

    public isPreviewed () {
        return status == PREVIEWED
    }

}
