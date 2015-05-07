package be.cytomine.api.utils

import be.cytomine.api.RestController
import be.cytomine.utils.Config
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

class RestConfigController extends RestController {

    def configService

    @RestApiMethod(description="Get all global configs")
    def list() {
        def data = configService.list()
        responseSuccess(data)
    }

    @RestApiMethod(description="Get a config with its id or its key")
    @RestApiParams(params=[
            @RestApiParam(name="key", type="String", paramType = RestApiParamType.PATH,description = "The config key")
    ])
    def show() {
        Config config = configService.readByKey(params.key)

        if (config) {
            responseSuccess(config)
        } else {
            responseNotFound("Config", params.key)
        }
    }

    @RestApiMethod(description="Add a global config")
    def add() {
        add(configService, request.JSON)
    }


    @RestApiMethod(description="Edit a config")
    def update() {
        update(configService, request.JSON)
    }

    /**
     * Delete a Property (Method from RestController)
     */
    @RestApiMethod(description="Delete a config")
    @RestApiParams(params=[
            @RestApiParam(name="key", type="String", paramType = RestApiParamType.PATH,description = "The config key")
    ])
    def delete()  {
        delete(configService, JSON.parse("{id : $params.id}"),null)
    }
}
