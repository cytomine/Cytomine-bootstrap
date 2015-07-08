package be.cytomine.api.image.server

import be.cytomine.Exception.CytomineException

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
import be.cytomine.utils.Task
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

@RestApi(name = "storage abstract image services", description = "Methods for managing the link between an image and its storage list")
class RestStorageAbstractImageController extends RestController {

    def storageAbstractImageService
    def taskService

    /**
     * Add a new storage to an abstract image
     */
    @RestApiMethod(description="Add a new storage to an abstract image")
    def add() {
        add(storageAbstractImageService, request.JSON)
    }

    /**
     * Remove a group from an abstract image
     */
    @RestApiMethod(description="Delete a storage from an abstract image list)")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The abstractimage-storage id")
    ])
    def delete() {
        try {
            Task task = taskService.read(params.getLong("task"))
            log.info "task ${task} is find for id = ${params.getLong("task")}"
            def domain = storageAbstractImageService.retrieve(JSON.parse("{id : $params.id}"))
            def result = storageAbstractImageService.delete(domain,transactionService.start(),task)
            //delete container in retrieval
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }
}
