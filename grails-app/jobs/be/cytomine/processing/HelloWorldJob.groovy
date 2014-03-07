package be.cytomine.processing

import be.cytomine.security.UserJob
import grails.converters.JSON
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

/**
 * Created with IntelliJ IDEA.
 * User: stevben
 * Date: 24/11/13
 * Time: 23:20
 * To change this template use File | Settings | File Templates.
 */
class HelloWorldJob {
    def sessionRequired = true
    def concurrent = true

    def jobParameterService

    static triggers = {
        //simple repeatInterval: 5000l // execute job once in 5 seconds
    }

    def execute(context) {
        log.info "Start HellWorld"

        Boolean preview = (Boolean) context.mergedJobDataMap.get('preview')
        Job job = (Job) context.mergedJobDataMap.get('job')
        UserJob userJob = (UserJob) context.mergedJobDataMap.get('userJob')

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

        rabbitSend('helloWorldQueue', (jobParameters as JSON).toString())
    }
}
