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
import be.cytomine.utils.Task
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Created by julien 
 * Date : 25/02/15
 * Time : 15:13
 *
 * Controller for the AMQP Queues
 */

@RestApi(name = "AMQP Queue services", description = "Methods useful for managing AMQP queues")
class RestAmqpQueueController extends RestController{

    def amqpQueueService
    def taskService

    /**
     * List all the queues currently active. Can also list all the active queues based on a name pattern.
     */
    @RestApiMethod(description="Get active queues (either all of them or based on their name)", listing = true)
    @RestApiParams(params=[
            @RestApiParam(name="name", type="string", paramType = RestApiParamType.PATH,description = "The name (or a part) of the queue")
    ])
    def list() {
        if(params.containsKey("name")) {
            responseSuccess(amqpQueueService.list(params.name.toString()))
        }
        else {
            responseSuccess(amqpQueueService.list())
        }
    }

    /**
     * Retrieve a single queue
     */
    @RestApiMethod(description="Get a queue based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The queue id")
    ])
    def show () {
        AmqpQueue amqpQueue

        if(params.containsKey("name")) {
            amqpQueue = amqpQueueService.read(params.name.toString())
        }
        else {
            amqpQueue = amqpQueueService.read(params.long('id'))
        }


        if (amqpQueue) {
            responseSuccess(amqpQueue)
        } else {
            responseNotFound("AmqpQueue", params.id)
        }
    }

    /**
     * Add a new queue
     */
    @RestApiMethod(description="Add a queue")
    def add () {
        add(amqpQueueService, request.JSON)
    }

    /**
     * Update an already existing queue
     */
    @RestApiMethod(description="Update a queue based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The queue id")
    ])
    def update () {
        update(amqpQueueService, request.JSON)
    }

    /**
     * Delete a queue
     */
    @RestApiMethod(description="Delete a queue based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The queue id")
    ])
    def delete() {
        Task task = taskService.read(params.getLong("task"))
        delete(amqpQueueService, JSON.parse("{id : $params.id}"),task)
    }
}
