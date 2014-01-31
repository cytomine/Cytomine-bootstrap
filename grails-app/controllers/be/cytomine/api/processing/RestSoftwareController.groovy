package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.project.Project
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for software: application that can be launch (job)
 */
@Api(name = "software services", description = "Methods for managing software, application that can be launch (job)")
class RestSoftwareController extends RestController {

    def softwareService

    /**
     * List all software available in cytomine
     */
    @ApiMethodLight(description="Get all software available in cytomine", listing = true)
    def list() {
        responseSuccess(softwareService.list())
    }

    /**
     * List all software by project
     */
    @ApiMethodLight(description="Get all software available in a project", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The project id")
    ])
    def listByProject() {
        Project project = Project.read(params.long('id'))
        if(project) responseSuccess(softwareService.list(project))
        else responseNotFound("Project", params.id)
    }

    /**
     * Get a specific software
     */
    @ApiMethodLight(description="Get a specific software")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The software id")
    ])
    def show() {
        Software software = softwareService.read(params.long('id'))
        if (software) {
            responseSuccess(software)
        } else {
            responseNotFound("Software", params.id)
        }
    }

    /**
     * Add a new software to cytomine
     * We must add in other request: parameters, software-project link,...
     */
    @ApiMethodLight(description="Add a new software to cytomine. We must add in other request: software parameters, software project link,...")
    def add() {
        add(softwareService, request.JSON)
    }

    /**
     * Update a software info
     */
    @ApiMethodLight(description="Update a software.", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The software id")
    ])
    def update() {
        update(softwareService, request.JSON)
    }

    /**
     * Delete software
     */
    @ApiMethodLight(description="Delete a software.", listing = true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The software id")
    ])
    def delete() {
        delete(softwareService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * List software
     * TODO:: could be improved with a single SQL request
     *
     */
    @ApiMethodLight(description="For a software and a project, get the stats (number of job, succes,...)", listing = true)
    @ApiParams(params=[
        @ApiParam(name="idProject", type="long", paramType = ApiParamType.PATH, description = "The project id"),
        @ApiParam(name="idSoftware", type="long", paramType = ApiParamType.PATH, description = "The software id"),
    ])
    @ApiResponseObject(objectIdentifier = "[numberOfJob:x,numberOfNotLaunch:x,numberOfInQueue:x,numberOfRunning:x,numberOfSuccess:x,numberOfFailed:x,numberOfIndeterminate:x,numberOfWait:x]")
    def softwareInfoForProject() {
        Project project = Project.read(params.long('idProject'))
        Software software = Software.read(params.long('idSoftware'))
        if(!project) {
            responseNotFound("Project", params.idProject)
        } else if(!software) {
            responseNotFound("Software", params.idSoftware)
        } else {
            def result = [:]
            List<Job> jobs = Job.findAllByProjectAndSoftware(project,software)
            
            //Number of job for this software and this project
            result['numberOfJob'] = jobs.size()
            
            //Number of job by state
            result['numberOfNotLaunch'] = 0
            result['numberOfInQueue'] = 0
            result['numberOfRunning'] = 0
            result['numberOfSuccess'] = 0
            result['numberOfFailed'] = 0
            result['numberOfIndeterminate'] = 0
            result['numberOfWait'] = 0
            
            jobs.each { job ->
                if(job.status==Job.NOTLAUNCH) result['numberOfNotLaunch']++
                if(job.status==Job.INQUEUE) result['numberOfInQueue']++
                if(job.status==Job.RUNNING) result['numberOfRunning']++
                if(job.status==Job.SUCCESS) result['numberOfSuccess']++
                if(job.status==Job.FAILED) result['numberOfFailed']++
                if(job.status==Job.INDETERMINATE) result['numberOfIndeterminate']++
                if(job.status==Job.WAIT) result['numberOfWait']++
            }

            responseSuccess(result)
        }
    }
}
