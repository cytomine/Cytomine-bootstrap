package be.cytomine.api.processing

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.processing.*
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for software project link
 * A software may be used by some project
 */
@RestApi(name = "job template annotation services", description = "Methods for managing a link between a job template and an annotation (roi or other type)")
class RestJobTemplateAnnotationController extends RestController{

    def jobTemplateAnnotationService
    def jobTemplateService
    def userAnnotationService
    def jobParameterService
    def jobService
    def cytomineService

    /**
     * List all software by project
     */
    @RestApiMethod(description="List all link beetween a job template and an annotation", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="jobtemplate", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) The job template id"),
        @RestApiParam(name="annotation", type="long", paramType = RestApiParamType.QUERY, description = "(Optional) The annotation ROI id")
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
    @RestApiMethod(description="Get a link between a job and an annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The link id")
    ])
    def show() {
        JobTemplateAnnotation parameter = jobTemplateAnnotationService.read(params.long('id'))
        if (parameter) responseSuccess(parameter)
        else responseNotFound("JobTemplateAnnotation", params.id)
    }

    /**
     * Add an existing software to a project
     */
    @RestApiMethod(description="Add a link between a job and an annotation")
    def add () {
        try {
            def result = jobTemplateAnnotationService.add(request.JSON)
            JobTemplateAnnotation jobTemplateAnnotation = result.object

            //init a job with the template
            Job jobToAdd = new Job(project:jobTemplateAnnotation.jobTemplate.project, software:jobTemplateAnnotation.jobTemplate.software)
            jobToAdd = jobService.add(JSON.parse(jobToAdd.encodeAsJSON())).object

            //copy parameters
            jobTemplateAnnotation.jobTemplate.parameters().each { paramTemplate ->
                jobParameterService.add(JSON.parse(new JobParameter(softwareParameter: paramTemplate.softwareParameter,value: paramTemplate.value, job:jobToAdd).encodeAsJSON()))
            }

            //add the annotation parameters
            SoftwareParameter annotationParam = SoftwareParameter.findBySoftwareAndName(jobTemplateAnnotation.jobTemplate.software,"annotation")
            if(!annotationParam) {
                throw new WrongArgumentException("Software must have a parameter called 'annotation'!")
            }
            jobParameterService.add(JSON.parse(new JobParameter(softwareParameter: annotationParam,value: jobTemplateAnnotation.annotationIdent, job:jobToAdd).encodeAsJSON()))

            //create user job
            jobService.createUserJob(cytomineService.currentUser, jobToAdd)

            result.data.job = jobToAdd
            responseResult(result)
        } catch (CytomineException e) {
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }


    }

    /**
     * Delete the software for the project
     */
    @RestApiMethod(description="Remove the link beween the job and the annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The link id")
    ])
    def delete() {
        delete(jobTemplateAnnotationService, JSON.parse("{id : $params.id}"),null)
    }
}
