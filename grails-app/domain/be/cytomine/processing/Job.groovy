package be.cytomine.processing

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.security.User
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import grails.converters.JSON
import be.cytomine.Exception.WrongArgumentException

class Job extends CytomineDomain implements CytomineJob {   //TODO : SHOULD BE ABSTRACT with GRAILS2.0

    Boolean running = false
    Boolean indeterminate = true
    int progress = 0
    Boolean successful = false
    Project project

    static transients = ["url"]

    static belongsTo = [software: Software]

    static hasMany = [jobData: JobData, jobParameter : JobParameter]

    static constraints = {
        progress(min: 0, max: 100)
        project(nullable:true)
    }

    static mapping = {
        tablePerHierarchy(false)
    }

    def getUrl() {
        ConfigurationHolder.config.grails.serverURL
    }

    def execute() {
        //throw "Method job.execute() not overridden"
    }

    static void registerMarshaller() {
        println "Register custom JSON renderer for " + Job.class
        JSON.registerObjectMarshaller(Job) {
            def job = [:]
            job.id = it.id
            job.running = it.running
            job.indeterminate = it.indeterminate
            job.progress = it.progress
            job.successful = it.successful
            
            job.project = it.project?.id
            job.software = it.software?.id
            
            job.jobParameter = it.jobParameter
            job.jobData = it.jobData
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

    static Job getFromData(job, jsonJob) {
        try {
            if (!jsonJob.running.toString().equals("null"))
                job.running = Boolean.parseBoolean(jsonJob.running.toString())
            if (!jsonJob.indeterminate.toString().equals("null"))
                job.indeterminate = Boolean.parseBoolean(jsonJob.indeterminate.toString())
            if (!jsonJob.progress.toString().equals("null"))
                job.progress = Integer.parseInt(jsonJob.progress.toString())
            if (!jsonJob.successful.toString().equals("null"))
                job.successful = Boolean.parseBoolean(jsonJob.successful.toString())
            
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
