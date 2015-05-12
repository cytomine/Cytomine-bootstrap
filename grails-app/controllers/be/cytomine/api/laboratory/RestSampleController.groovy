package be.cytomine.api.laboratory

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
import be.cytomine.laboratory.Sample
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for sample (part of 'source' that has been scan to image)
 */
@RestApi(name = "sample services", description = "Methods for managing a sample, part of 'source' that has been scan to image")
class RestSampleController extends RestController {

    def sampleService
    def cytomineService

    /**
     * List all available sample for the current user
     */
    @RestApiMethod(description="Get all sample available for the current user", listing = true)
    def list() {
        responseSuccess(sampleService.list(cytomineService.getCurrentUser()))
    }

    /**
     * Get a sample
     */
    @RestApiMethod(description="Get a sample")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The sample id")
    ])
    def show() {
        Sample sample = sampleService.read(params.long('id'))
        if (sample) {
            responseSuccess(sample)
        } else {
            responseNotFound("Sample", params.id)
        }
    }

    /**
     * Add a new sample
     */
    @RestApiMethod(description="Add a new sample")
    def add() {
        add(sampleService, request.JSON)
    }

    /**
     * Update a existing sample
     */
    @RestApiMethod(description="Update a sample")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The sample id")
    ])
    def update() {
        update(sampleService, request.JSON)
    }

    /**
     * Delete sample
     */
    @RestApiMethod(description="Delete a sample")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The sample id")
    ])
    def delete() {
        delete(sampleService, JSON.parse("{id : $params.id}"),null)
    }
}
