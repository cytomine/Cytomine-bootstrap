package be.cytomine.api.processing

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.processing.JobTemplate
import be.cytomine.processing.Software
import be.cytomine.project.Project
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import org.jsondoc.core.pojo.ApiParamType

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Controller that handle request for project images.
 */
@Api(name = "job template services", description = "Methods for managing job template, a pre-filled job to quickly run")
class RestJobTemplateController extends RestController {

    def imageProcessingService
    def jobTemplateService
    def imageInstanceService
    def projectService
    def userAnnotationService
    def algoAnnotationService
    def reviewedAnnotationService
    def secUserService
    def termService
    def cytomineService
    def taskService
    def softwareService

    @ApiMethodLight(description="Get a job template")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH, description = "The template id")
    ])
    def show() {
        JobTemplate job = jobTemplateService.read(params.long('id'))
        if (job) {
            responseSuccess(job)
        } else {
            responseNotFound("JobTemplate", params.id)
        }
    }

    @ApiMethodLight(description="List template for the specific filter", listing = true)
    @ApiParamsLight(params=[
        @ApiParamLight(name="project", type="long", paramType = ApiParamType.PATH, description = "The projecte id"),
        @ApiParamLight(name="software", type="long", paramType = ApiParamType.QUERY, description = "(Optional) The software id"),
    ])
    def list() {
        Project project = projectService.read(params.long('project'))
        Software software = softwareService.read(params.long('software'))
        if (params.long('software') && !software)  {
            responseNotFound("JobTemplate", "Software", params.software)
        } else if (project)  {
            responseSuccess(jobTemplateService.list(project,software))
        } else {
            responseNotFound("JobTemplate", "Project", params.idImage)
        }
    }

    @ApiMethodLight(description="Add a new job template")
    def add() {
        try {
            responseResult(jobTemplateService.add(request.JSON))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @ApiMethodLight(description="Update a job template")
    def update() {
        update(jobTemplateService, request.JSON)
    }

    @ApiMethodLight(description="Delete a job template")
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The template id")
    ])
    def delete() {
        delete(jobTemplateService, JSON.parse("{id : $params.id}"),null)
    }
}
