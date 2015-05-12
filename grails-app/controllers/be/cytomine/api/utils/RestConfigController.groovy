package be.cytomine.api.utils

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
import be.cytomine.utils.Config
import grails.converters.JSON
import grails.converters.XML
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
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The config id")
    ])
    def delete()  {
        delete(configService, JSON.parse("{id : $params.id}"),null)
    }

    @RestApiMethod(description="Get the boolean LDAP enabled")
    def ldap() {

        def data = [:]
        data['enabled'] = grailsApplication.config.grails.plugin.springsecurity.ldap.active

        withFormat {
            json { render data as JSON }
            xml { render data as XML}
        }
    }

}
