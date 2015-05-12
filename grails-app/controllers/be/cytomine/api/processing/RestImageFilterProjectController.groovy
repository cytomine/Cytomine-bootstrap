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

import be.cytomine.api.RestController
import be.cytomine.project.Project
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller that handle the link between a project and a image filter
 */
@RestApi(name = "image filter project services", description = "Methods for managing image filter project, a link between an image filter and a project")
class RestImageFilterProjectController extends RestController {

    def imageFilterProjectService
    def projectService
    def cytomineService

    /**
     * List all image filter project
     */
    @RestApiMethod(description="List all image filter project", listing = true)
    def list() {
 		responseSuccess(imageFilterProjectService.list())
    }

    /**
     * List all image filter for a project
     */
    @RestApiMethod(description="List all image filter project for a specific project", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="project", type="long", paramType = RestApiParamType.PATH, description = "The project id")
    ])
    def listByProject() {
        def project = Project.read(params.project)
		if (!project) {
            responseNotFound("Project", "Project", params.project)
            return
        }
        def imagesFiltersProject = imageFilterProjectService.list(project)
 		responseSuccess(imagesFiltersProject)
    }

    /**
     * Add an image filter to a project
     */
    @RestApiMethod(description="Add an image filter to a project")
    def add () {
        add(imageFilterProjectService, request.JSON)
    }

    /**
     * Delete an image filter from a project
     */
    @RestApiMethod(description="Delete an image filter from a project")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image filter id")
    ])
    def delete() {
        delete(imageFilterProjectService, JSON.parse("{id : $params.id}"),null)
    }

}
