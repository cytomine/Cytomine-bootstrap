package be.cytomine.processing

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

        Job job = (Job) context.mergedJobDataMap.get('job')
        UserJob userJob = UserJob.findByJob(job)
        SpringSecurityUtils.reauthenticate userJob.getUser().getUsername(), null

        def jobParameters = []
        jobParameterService.list(job).each {
            jobParameters << [ name : "--"+it.getSoftwareParameter().getName(), value : it.getValue()]
        }
        // execute job
        log.info "execute $job with $jobParameters"
        rabbitSend('cytomineQueue', (jobParameters as JSON).toString())

    }
}
