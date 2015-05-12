package be.cytomine.api.image.multidim

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
import be.cytomine.image.multidim.ImageGroup
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
 */
@RestApi(name = "image group services", description = "Methods for managing image group, a group of image from the same sample in different dimension (channel, zstack,...)")
class RestImageGroupController extends RestController {

    def imageGroupService
    def projectService

    @RestApiMethod(description="Get an image group")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image group id")
    ])
    def show() {
        ImageGroup image = imageGroupService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("ImageGroup", params.id)
        }
    }

    @RestApiMethod(description="Get image group listing by project", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The project id")
    ])
    def listByProject() {
        Project project = projectService.read(params.long('id'))

        if (project)  {
            responseSuccess(imageGroupService.list(project))
        }
        else {
            responseNotFound("ImageGroup", "Project", params.id)
        }
    }

    @RestApiMethod(description="Add a new image group")
    def add () {
        add(imageGroupService, request.JSON)
    }

    @RestApiMethod(description="Update an image group")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="int", paramType = RestApiParamType.PATH, description = "The image group id")
    ])
    def update() {
        update(imageGroupService, request.JSON)
    }

    @RestApiMethod(description="Delete an image group")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image group")
    ])
    def delete() {
        delete(imageGroupService, JSON.parse("{id : $params.id}"),null)
    }
}
