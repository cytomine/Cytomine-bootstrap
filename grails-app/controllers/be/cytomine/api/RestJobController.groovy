package be.cytomine.api

import grails.plugins.springsecurity.Secured
import be.cytomine.processing.Job
import be.cytomine.security.User
import be.cytomine.command.Command
import be.cytomine.command.job.AddJobCommand

class RestJobController extends RestController {

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        responseSuccess(Job.list())
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        Job job = Job.read(params.id)
        if (job) responseSuccess(job)
        else responseNotFound("Job", params.id)
    }

    @Secured(['ROLE_ADMIN'])
    def save = {
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        Command addJobCommand = new AddJobCommand(postData: request.JSON.toString(), user: currentUser)
        def result = processCommand(addJobCommand, currentUser)
        response(result)
    }

}
