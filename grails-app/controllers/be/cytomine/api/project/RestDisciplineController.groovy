package be.cytomine.api.project

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
import be.cytomine.project.Discipline
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for discipline
 * A discipline can be link with a project
 */
@RestApi(name = "discipline services", description = "Methods for managing discipline")
class RestDisciplineController extends RestController {

    def disciplineService
    /**
     * List all discipline
     */
    @RestApiMethod(description="Get discipline listing, according to your access", listing = true)
    def list () {
        responseSuccess(disciplineService.list())
    }

    /**
     * Get a single discipline
     */
    @RestApiMethod(description="Get a discipline")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The discipline id")
    ])
    def show () {
        Discipline discipline = disciplineService.read(params.long('id'))
        if (discipline) {
            responseSuccess(discipline)
        } else {
            responseNotFound("Discipline", params.id)
        }
    }

    /**
     * Add a new discipline
     */
    @RestApiMethod(description="Add a new discipline")
    def add () {
        add(disciplineService, request.JSON)
    }

    /**
     * Update a existing discipline
     */
    @RestApiMethod(description="Update a discipline")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="int", paramType = RestApiParamType.PATH)
    ])
    def update () {
        update(disciplineService, request.JSON)
    }

    /**
     * Delete discipline
     */
    @RestApiMethod(description="Delete a discipline")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="int", paramType = RestApiParamType.PATH)
    ])
    def delete () {
        delete(disciplineService, JSON.parse("{id : $params.id}"),null)
    }

}
