package be.cytomine.api.middleware

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
import be.cytomine.middleware.AmqpQueue
import be.cytomine.middleware.AmqpQueueConfigInstance
import be.cytomine.utils.Task
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Created by julien 
 * Date : 03/03/15
 * Time : 14:09
 */
class RestAmqpQueueConfigInstanceController extends RestController {

    def amqpQueueConfigInstanceService
    def amqpQueueService
    def taskService

    /**
     * List all the parameters for the queues in the system.
     */
    @RestApiMethod(description="Get all the parameters for the queues in the system", listing = true)
    def list() {
        responseSuccess(amqpQueueConfigInstanceService.list())
    }

    /**
     * List all parameters/configurations by queue
     */
    @RestApiMethod(description="List all parameters/configurations by queue", listing = true)
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The queue id")
    ])
    def listByQueue() {
        AmqpQueue amqpQueue = amqpQueueService.read(params.long('id'))
        if (amqpQueue) {
            responseSuccess(amqpQueueConfigInstanceService.list(amqpQueue))
        } else {
            responseNotFound("AmqpQueue", params.id)
        }
    }

    /**
     * Retrieve a single configuration for a specific queue based on an id or a name.
     */
    @RestApiMethod(description="Get the value for a parameter for a specific queue based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The instance id")
    ])
    def show () {

        AmqpQueueConfigInstance amqpQueueConfigInstance = amqpQueueConfigInstanceService.read(params.long('id'))

        if (amqpQueueConfigInstance) {
            responseSuccess(amqpQueueConfigInstance)
        } else {
            responseNotFound("AmqpQueueConfigInstance", params.id)
        }
    }

    /**
     * Add a new value for a configuration and a specific queue
     */
    @RestApiMethod(description="Add a new value for a configuration and a specific queue")
    def add () {
        add(amqpQueueConfigInstanceService, request.JSON)
    }

    /**
     * Update an already existing configuration for a specific queue
     */
    @RestApiMethod(description="Update an already existing configuration for a specific queue based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The instance id")
    ])
    def update () {
        update(amqpQueueConfigInstanceService, request.JSON)
    }

    /**
     * Delete a configuration for a specific queue
     */
    @RestApiMethod(description="Delete a configuration for a specific queue based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The instance id")
    ])
    def delete() {
        Task task = taskService.read(params.getLong("task"))
        delete(amqpQueueConfigInstanceService, JSON.parse("{id : $params.id}"),task)
    }

}
