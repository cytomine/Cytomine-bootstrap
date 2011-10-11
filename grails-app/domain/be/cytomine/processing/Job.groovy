package be.cytomine.processing

import be.cytomine.security.User
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import be.cytomine.SequenceDomain
import be.cytomine.processing.algorithms.myDetection.MyDetectionLearnJob
import be.cytomine.processing.algorithms.myDetection.MyDetectionPredictJob
import be.cytomine.project.Project

class Job extends SequenceDomain implements CytomineJob {   //TODO : SHOULD BE ABSTRACT with GRAILS2.0

    User user
    Boolean running = false
    Boolean indeterminate = true
    int progress = 0
    Boolean successful = false

    static transients = ["url"]

    static belongsTo = [ software : Software]

    static hasMany = [ jobData : JobData]


    static constraints = {
        user nullable : false
        progress(min : 0, max : 100)
    }

    public afterInsert() {
        print "afterInsert"
        execute()
    }

    static mapping = {
        tablePerHierarchy(true)
    }

    def getUrl() {
        ConfigurationHolder.config.grails.serverURL
    }

    def execute() {
        //throw "Method job.execute() not overridden"
    }

    static Job createFromData(data) {

        Job job = null

        if (new MyDetectionLearnJob().getClass().getName().contains(data.className.toString())) {
            println "MyDetectionLearnJob"
            /* Should be in  MyDetectionLearnJob.createFromData but doesn't work ? Instance is null when passed in argument... (*) */
            job = new MyDetectionLearnJob()
            if (data.doRGB) job.doRGB = Boolean.parseBoolean(data.doRGB)
            if (data.doEDGE) job.doEDGE = Boolean.parseBoolean(data.doEDGE)
            if (data.doHSV) job.doHSV = Boolean.parseBoolean(data.doHSV)
                if (data.doGRAY) job.doGRAY = Boolean.parseBoolean(data.doGRAY)
            if (data.doLBP) job.doLBP = Boolean.parseBoolean(data.doLBP)
            if (data.ratio) job.ratio = Integer.parseInt(data.ratio)
            if (data.split) job.split = Integer.parseInt(data.split)
            if (data.bound) job.bound = Integer.parseInt(data.bound)
            if (data.tree) job.tree = Integer.parseInt(job.tree)
            if (data.subwindowWidth) job.subwindowWidth = Integer.parseInt(job.subwindowWidth)
            if (data.subwindowHeight) job.subwindowHeight = Integer.parseInt(job.subwindowHeight)
            job.user = User.read(data.user)
            job.software = Software.read(data.software)
            job.project = Project.read(data.project)
        }

        if (new MyDetectionPredictJob().getClass().getName().contains(data.className.toString())) {
            println "MyDetectionPredictJob"
            /* Same than (*) */
            job = new MyDetectionPredictJob()
            job.myDetectionLearnJob = MyDetectionLearnJob.read(data.myDetectionPredictJob)
        }
        return job
    }

}
