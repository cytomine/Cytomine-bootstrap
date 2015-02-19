package be.cytomine.api.middleware

import be.cytomine.api.RestController
import be.cytomine.middleware.MessageBrokerServer
import be.cytomine.utils.Task
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Created by julien 
 * Date : 05/02/15
 * Time : 11:54
 *
 * Controller for MessageBrokerServer
 */
@RestApi(name = "message broker server services", description = "Methods for managing message broker servers")
class RestMessageBrokerServerController extends RestController{

    def messageBrokerServerService
    def taskService

    /**
     * Can either list all message broker servers visible for the current user or
     * list all message broker servers that contains a specific string in their name
     */
    @RestApiMethod(description="Get message broker servers available that contains a specific name", listing = true)
    @RestApiParams(params=[
            @RestApiParam(name="name", type="string", paramType = RestApiParamType.PATH,description = "The name of the message broker server")
    ])
    def list() {
        if(params.containsKey("name")) {
            /*
            MessageBrokerServer mbs = messageBrokerServerService.read(params.name)
            if(mbs) {
                responseSuccess(messageBrokerServerService.list(mbs.name))
            } else {
                responseNotFound("MessageBrokerServer", params.name)
            }*/
            responseSuccess(messageBrokerServerService.list(params.name.toString()))
        }
        else {
            responseSuccess(messageBrokerServerService.list())
        }
    }

      /**
       * List all message broker servers available for the current user with a specific name
       */
//    @RestApiMethod(description="Get message broker servers available that contains a specific name", listing = true)
//    @RestApiParams(params=[
//            @RestApiParam(name="name", type="string", paramType = RestApiParamType.PATH,description = "The name of the message broker server")
//    ])
//    def listByNameILike() {
//        MessageBrokerServer msb = messageBrokerServerService.read(params.toString("name"))
//        if(msb) {
//            responseSuccess(messageBrokerServerService.list(msb.name))
//        } else {
//            responseNotFound("MessageBrokerServer", params.name)
//        }
//    }

    /**
     * Retrieve a single message broker server
     */
    @RestApiMethod(description="Get a message broker server based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The message broker server id")
    ])
    def show () {
        MessageBrokerServer msb = messageBrokerServerService.read(params.long('id'))
        if (msb) {
            responseSuccess(msb)
        } else {
            responseNotFound("MessageBrokerServer", params.id)
        }
    }

    /**
     * Add a new message broker server
     */
    @RestApiMethod(description="Add a message broker server")
    def add () {
        add(messageBrokerServerService, request.JSON)
    }

    /**
     * Update an already existing message broker server
     */
    @RestApiMethod(description="Update a message broker server based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The message broker server id")
    ])
    def update () {
        update(messageBrokerServerService, request.JSON)
    }

    /**
     * Delete a message broker server
     */
    @RestApiMethod(description="Delete a message broker server based on an id")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The message broker server id")
    ])
    def delete() {
        Task task = taskService.read(params.getLong("task"))
        delete(messageBrokerServerService, JSON.parse("{id : $params.id}"),task)
    }

}
