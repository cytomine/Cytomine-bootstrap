package be.cytomine.api.middleware

import be.cytomine.api.RestController
import be.cytomine.middleware.AmqpQueue
import be.cytomine.middleware.AmqpQueueConfig
import be.cytomine.utils.Task
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Created by julien 
 * Date : 26/02/15
 * Time : 15:16
 *
 * Controller for the possible configurations for an AMQP queue
 */
@RestApi(name = "AMQP Queue Configuration services", description = "Methods useful for managing AMQP configurations")
class RestAmqpQueueConfigController extends RestController {

    def amqpQueueConfigService
    def taskService

    /**
     * List all the possible parameters for a queue.
     */
    @RestApiMethod(description="Get all possible parameters for an AMQP queue", listing = true)
    def list() {
        responseSuccess(amqpQueueConfigService.list())
    }

    /**
     * Retrieve a single configuration based on an id or a name.
     */
    @RestApiMethod(description="Get a queue based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The parameter id"),
            @RestApiParam(name="name", type="string", paramType = RestApiParamType.PATH, description = "The parameter name")
    ])
    def show () {
        AmqpQueueConfig amqpQueueConfig


        if(params.containsKey("name")) {
            amqpQueueConfig = amqpQueueConfigService.read(params.name.toString())
        }
        else {
            amqpQueueConfig = amqpQueueConfigService.read(params.long('id'))
        }

        if (amqpQueueConfig) {
            responseSuccess(amqpQueueConfig)
        } else {
            responseNotFound("AmqpQueueConfig", params.id)
        }
    }

    /**
     * Add a new configuration for a queue
     */
    @RestApiMethod(description="Add a configuration for a queue")
    def add () {
        add(amqpQueueConfigService, request.JSON)
    }

    /**
     * Update an already existing configuration
     */
    @RestApiMethod(description="Update a configuration based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The configuration id")
    ])
    def update () {
        update(amqpQueueConfigService, request.JSON)
    }

    /**
     * Delete a configuration
     */
    @RestApiMethod(description="Delete a configuration based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The configuration id")
    ])
    def delete() {
        Task task = taskService.read(params.getLong("task"))
        delete(amqpQueueConfigService, JSON.parse("{id : $params.id}"),task)
    }
}

