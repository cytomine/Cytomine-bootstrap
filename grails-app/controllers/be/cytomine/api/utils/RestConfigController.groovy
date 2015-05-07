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
    def cytomineService
    def projectService
    def imageInstanceService
    def secUserService

    @RestApiMethod(description="Get all global configs")
    def list() {
        def data = configService.list()
        responseSuccess(data)
    }

    @RestApiMethod(description="Get a config with its id or its key")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "(Optional, if null key must be set) The config id"),
            @RestApiParam(name="key", type="long", paramType = RestApiParamType.PATH,description = "(Optional, if null id must be set) The config key")
    ])
    def show() {
        Config config
        if(params.id != null) {
            config = configService.read(params.id)
        } else if (params.key != null) {
            config = configService.readByKey(params.key)
        }

        if (config) {
            responseSuccess(config)
        } else {
            responseNotFound("Config", params.id)
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
    @RestApiMethod(description="Delete a property")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The config id")
    ])
    def delete()  {
        def json = JSON.parse("{id : $params.id}")
        delete(configService,json,null)
    }
}
