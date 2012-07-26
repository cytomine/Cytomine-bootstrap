package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.ResponseService
import be.cytomine.project.Project
import grails.converters.JSON

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

    double rate

    static transients = ["url"]

    static belongsTo = [software: Software]

    SortedSet jobParameter
    static hasMany = [jobParameter: JobParameter]

    static constraints = {
        progress(min: 0, max: 100)
        project(nullable:true)
        statusComment(nullable:true)
        status(range: 0..6)
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
    

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + Job.class
        JSON.registerObjectMarshaller(Job) {
            def job = [:]
            job.id = it.id
            job.algoType = ResponseService.getClassName(it).toLowerCase()
            job.progress = it.progress
            job.status = it.status
            job.number = it.number
            job.statusComment = it.statusComment
            job.project = it.project?.id
            job.software = it.software?.id
            job.created = it.created ? it.created.time.toString() : null
            job.updated = it.updated ? it.updated.time.toString() : null
            job.rate = (it.rate == -1) ? "N.A." : it.rate
            try {

            } catch(Exception e) {e.printStackTrace()}
            
            try {
                job.jobParameter =  it.jobParameter

            } catch(Exception e) {println e}

            return job
        }
    }

    static Job createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static Job createFromData(jsonJob) {
        getFromData(new Job(), jsonJob)
    }

    static def getFromData(Job job, jsonJob) {
        try {
            if (!jsonJob.status.toString().equals("null"))
                job.status = Integer.parseInt(jsonJob.status.toString())
            if (!jsonJob.progress.toString().equals("null"))
                job.progress = Integer.parseInt(jsonJob.progress.toString())

            if (!jsonJob.statusComment.toString().equals("null"))
                job.statusComment = jsonJob.statusComment.toString()
            else
                job.statusComment = ""

            String projectId = jsonJob.project.toString()
            if (!projectId.equals("null")) {
                job.project = Project.read(projectId)
                if (!job.project) throw new WrongArgumentException("Project was not found with id:" + projectId)
            } else {
                job.project = null
            }

            String softwareId = jsonJob.software.toString()
            if (!softwareId.equals("null")) {
                job.software = Software.read(softwareId)
                if (!job.software) throw new WrongArgumentException("Software was not found with id:" + softwareId)
            } else job.software = null

            job.rate = (!jsonJob.rate.toString().equals("null")) ? Double.parseDouble(jsonJob.rate.toString()) : -1

            job.created = (!jsonJob.created.toString().equals("null")) ? new Date(Long.parseLong(jsonJob.created.toString())) : null
            job.updated = (!jsonJob.updated.toString().equals("null")) ? new Date(Long.parseLong(jsonJob.updated.toString())) : null
        }catch(Exception e) {
            throw new WrongArgumentException(e.toString())
        }
        return job;
    }
}
