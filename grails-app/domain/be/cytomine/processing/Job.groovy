package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.ResponseService
import be.cytomine.project.Project
import be.cytomine.security.UserJob
import grails.converters.JSON
import org.apache.log4j.Logger

class Job extends CytomineDomain  {
    //enum type are too heavy with GORM
    public static int NOTLAUNCH = 0
    public static int INQUEUE = 1
    public static int RUNNING = 2
    public static int SUCCESS = 3
    public static int FAILED = 4
    public static int INDETERMINATE = 5
    public static int WAIT = 6

    int progress = 0
    int status = 0 //enum type are too heavy with GORM
    int number//nth job of the software within a project
    String statusComment

    Project project

    Double rate = null

    boolean dataDeleted

    static transients = ["url"]

    static belongsTo = [software: Software]

    SortedSet jobParameter
    static hasMany = [jobParameter: JobParameter]

    static constraints = {
        progress(min: 0, max: 100)
        project(nullable:true)
        statusComment(nullable:true)
        status(range: 0..6)
        rate(nullable: true)
        dataDeleted(nullable:true)
    }

    static mapping = {
        tablePerHierarchy(true)
        id(generator: 'assigned', unique: true)
    }

    public beforeInsert() {
        super.beforeInsert()
        List<Job> previousJob = Job.findAllBySoftwareAndProject(software,project,[sort: "number", order: "desc"])
        if(!previousJob.isEmpty()) number = previousJob.get(0).number+1
        else number = 1;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     * @param cytomineBaseUrl Cytomine base URL (from config file)
     */
    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + Job.class)
        JSON.registerObjectMarshaller(Job) {
            def job = [:]
            job.id = it.id
            job.algoType = ResponseService.getClassName(it).toLowerCase()
            job.progress = it.progress
            job.status = it.status
            job.number = it.number
            job.statusComment = it.statusComment

            try {
                UserJob user = UserJob.findByJob(it)
                job.username = user?.realUsername()
                job.userJob = user.id
            } catch (Exception e) {log.info e}

            job.project = it.project?.id
            job.software = it.software?.id
            job.created = it.created ? it.created.time.toString() : null
            job.updated = it.updated ? it.updated.time.toString() : null
            job.rate = it.rate
            try {
                job.jobParameter =  it.jobParameter
            } catch(Exception e) {log.info e}

            job.dataDeleted = it.dataDeleted

            return job
        }
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static Job createFromDataWithId(def json) {
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
    static Job createFromData(def json) {
        insertDataIntoDomain(new Job(), json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */    
    static def insertDataIntoDomain(def domain, def json) {
        try {
            if (!json.status.toString().equals("null"))
                domain.status = Integer.parseInt(json.status.toString())
            if (!json.progress.toString().equals("null"))
                domain.progress = Integer.parseInt(json.progress.toString())

            if (!json.statusComment.toString().equals("null"))
                domain.statusComment = json.statusComment.toString()
            else
                domain.statusComment = ""

            String projectId = json.project.toString()
            if (!projectId.equals("null")) {
                domain.project = Project.read(projectId)
                if (!domain.project) throw new WrongArgumentException("Project was not found with id:" + projectId)
            } else {
                domain.project = null
            }

            String softwareId = json.software.toString()
            if (!softwareId.equals("null")) {
                domain.software = Software.read(softwareId)
                if (!domain.software) throw new WrongArgumentException("Software was not found with id:" + softwareId)
            } else domain.software = null

            domain.rate = (!json.rate.toString().equals("null")) ? Double.parseDouble(json.rate.toString()) : -1

            if (!json.dataDeleted.toString().equals("null"))
                domain.dataDeleted = Boolean.parseBoolean(json.progress.toString())

            domain.created = (!json.created.toString().equals("null")) ? new Date(Long.parseLong(json.created.toString())) : null
            domain.updated = (!json.updated.toString().equals("null")) ? new Date(Long.parseLong(json.updated.toString())) : null
        }catch(Exception e) {
            throw new WrongArgumentException(e.toString())
        }
        return domain;
    }
}
