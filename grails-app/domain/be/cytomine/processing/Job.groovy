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
    int number


    Project project

    double rate = -1d

    static transients = ["url"]

    static belongsTo = [software: Software]

    SortedSet jobParameter
    static hasMany = [jobParameter: JobParameter]

    static constraints = {
        progress(min: 0, max: 100)
        project(nullable:true)
        status(range: 0..6)
        rate(nullable:true)
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

            job.project = it.project?.id
            job.software = it.software?.id

            job.created = it.created ? it.created.time.toString() : null
            job.updated = it.updated ? it.updated.time.toString() : null

            try {
                job.rate = it.rate
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
        def job = new Job()
        getFromData(job, jsonJob)
    }

    static def getFromData(job, jsonJob) {
        try {
            if (!jsonJob.status.toString().equals("null"))
                job.status = Integer.parseInt(jsonJob.status.toString())
            if (!jsonJob.progress.toString().equals("null"))
                job.progress = Integer.parseInt(jsonJob.progress.toString())

            String projectId = jsonJob.project.toString()
            if (!projectId.equals("null")) {
                job.project = Project.read(projectId)
                if (!job.project) throw new WrongArgumentException("Project was not found with id:" + projectId)
            } else job.project = null

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

//    static Job createFromData(data) {
    //
    //        Job job = null
    //
    //        /*if (new MyDetectionLearnJob().getClass().getName().contains(data.className.toString())) {
    //            println "MyDetectionLearnJob"
    //            // Should be in  MyDetectionLearnJob.createFromData but doesn't work ? Instance is null when passed in argument... (*)
    //            job = new MyDetectionLearnJob()
    //            if (data.doRGB) job.doRGB = Boolean.parseBoolean(data.doRGB)
    //            if (data.doEDGE) job.doEDGE = Boolean.parseBoolean(data.doEDGE)
    //            if (data.doHSV) job.doHSV = Boolean.parseBoolean(data.doHSV)
    //            if (data.doGRAY) job.doGRAY = Boolean.parseBoolean(data.doGRAY)
    //            if (data.doLBP) job.doLBP = Boolean.parseBoolean(data.doLBP)
    //            if (data.ratio) job.ratio = Integer.parseInt(data.ratio)
    //            if (data.split) job.split = Integer.parseInt(data.split)
    //            if (data.bound) job.bound = Integer.parseInt(data.bound)
    //            if (data.tree) job.tree = Integer.parseInt(job.tree)
    //            if (data.subwindowWidth) job.subwindowWidth = Integer.parseInt(job.subwindowWidth)
    //            if (data.subwindowHeight) job.subwindowHeight = Integer.parseInt(job.subwindowHeight)
    //            job.user = User.read(data.user)
    //            job.software = Software.read(data.software)
    //            job.project = Project.read(data.project)
    //        }
    //
    //        if (new MyDetectionPredictJob().getClass().getName().contains(data.className.toString())) {
    //            println "MyDetectionPredictJob"
    //            // Same than (*)
    //            job = new MyDetectionPredictJob()
    //            job.myDetectionLearnJob = MyDetectionLearnJob.read(data.myDetectionPredictJob)
    //        }*/
    //        return job
    //    }

}
