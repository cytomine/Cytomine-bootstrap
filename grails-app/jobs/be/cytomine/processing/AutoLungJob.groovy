package be.cytomine.processing

import be.cytomine.processing.job.AutoLungJobService
import be.cytomine.security.UserJob
import grails.converters.JSON
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class AutoLungJob  {

    def sessionRequired = true
    def concurrent = true

    def jobParameterService

    static triggers = {
        //simple repeatInterval: 5000l // execute job once in 5 seconds
    }

    def execute(context) {
        println "Start AutoLungJob"

        Boolean preview = (Boolean) context.mergedJobDataMap.get('preview')
        Job job = (Job) context.mergedJobDataMap.get('job')
        UserJob userJob = (UserJob) context.mergedJobDataMap.get('userJob')

        println "job ? : $job"
        println "userJob ? : $userJob"
        println "preview ? : $preview"
        SpringSecurityUtils.reauthenticate userJob.getUser().getUsername(), null

        def jobParameters = []
        jobParameterService.list(job).each {
            jobParameters << [ name : "--"+it.getSoftwareParameter().getName(), value : it.getValue()]
        }

        if (preview) {
            jobParameters << [ name : "--preview", value : ""]
        }
        // execute job
        log.info "execute $job with $jobParameters"

        rabbitSend('cytomineQueue', (jobParameters as JSON).toString())
    }
}
