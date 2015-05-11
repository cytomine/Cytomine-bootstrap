package be.cytomine.api.processing

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.processing.JobTemplate
import be.cytomine.processing.Software
import be.cytomine.project.Project
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Controller that handle request for project images.
 */
@RestApi(name = "job template services", description = "Methods for managing job template, a pre-filled job to quickly run")
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

    @RestApiMethod(description="Get a job template")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The template id")
    ])
    def show() {
        JobTemplate job = jobTemplateService.read(params.long('id'))
        if (job) {
            responseSuccess(job)
        } else {
            responseNotFound("JobTemplate", params.id)
        }
    }

    @RestApiMethod(description="List template for the specific filter", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="project", type="long", paramType = RestApiParamType.PATH, description = "The projecte id"),
        @RestApiParam(name="software", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) The software id"),
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

    @RestApiMethod(description="Add a new job template")
    def add() {
        try {
            responseResult(jobTemplateService.add(request.JSON))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @RestApiMethod(description="Update a job template")
    def update() {
        update(jobTemplateService, request.JSON)
    }

    @RestApiMethod(description="Delete a job template")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The template id")
    ])
    def delete() {
        delete(jobTemplateService, JSON.parse("{id : $params.id}"),null)
    }
}
