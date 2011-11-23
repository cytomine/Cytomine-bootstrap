package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import grails.plugins.springsecurity.Secured

class RestJobController extends RestController {

    def jobService

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list = {
        jobService.list()
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def show = {
        Job job = jobService.read(params.id)
        if (job) responseSuccess(job)
        else responseNotFound("Job", params.id)
    }

    @Secured(['ROLE_ADMIN'])
    def save = {
        def json = request.JSON
        response(jobService.add(json))
    }

}
