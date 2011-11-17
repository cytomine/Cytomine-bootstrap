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
        def json = request.JSON
        User currentUser = getCurrentUser(springSecurityService.principal.id)
        def result = processCommand(new AddJobCommand(user: currentUser), json)
        response(result)
    }

}
