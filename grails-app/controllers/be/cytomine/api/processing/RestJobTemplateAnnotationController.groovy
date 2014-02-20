package be.cytomine.api.processing

import be.cytomine.api.RestController
import be.cytomine.processing.JobTemplate
import be.cytomine.processing.JobTemplateAnnotation
import be.cytomine.project.Project
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for software project link
 * A software may be used by some project
 */
@Api(name = "job template annotation services", description = "Methods for managing a link between a job template and an annotation (roi or other type)")
class RestJobTemplateAnnotationController extends RestController{

    def jobTemplateAnnotationService
    def jobTemplateService
    def userAnnotationService

    /**
     * List all software by project
     */
    @ApiMethodLight(description="List all link beetween a job template and an annotation", listing = true)
    @ApiParams(params=[
        @ApiParam(name="jobtemplate", type="long", paramType = ApiParamType.QUERY, description = "(Optional) The job template id"),
        @ApiParam(name="annotation", type="long", paramType = ApiParamType.QUERY, description = "(Optional) The annotation ROI id")
    ])
    def list() {
        JobTemplate template = jobTemplateService.read(params.long('jobtemplate'))
        if(template || params.long('annotation')) {
            responseSuccess(jobTemplateAnnotationService.list(template,params.long('annotation')))
        } else {
            responseNotFound("JobTemplateAnnotation","JobTemplate",params.jobtemplate)
        }

    }

    /**
     * Get a software project link
     */
    @ApiMethodLight(description="Get a link between a job and an annotation")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The link id")
    ])
    def show() {
        JobTemplateAnnotation parameter = jobTemplateAnnotationService.read(params.long('id'))
        if (parameter) responseSuccess(parameter)
        else responseNotFound("JobTemplateAnnotation", params.id)
    }

    /**
     * Add an existing software to a project
     */
    @ApiMethodLight(description="Add a link between a job and an annotation")
    def add () {
        add(jobTemplateAnnotationService, request.JSON)
    }

    /**
     * Delete the software for the project
     */
    @ApiMethodLight(description="Remove the link beween the job and the annotation")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The link id")
    ])
    def delete() {
        delete(jobTemplateAnnotationService, JSON.parse("{id : $params.id}"),null)
    }
}
