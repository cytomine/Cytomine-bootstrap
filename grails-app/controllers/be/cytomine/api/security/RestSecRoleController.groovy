package be.cytomine.api.security

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
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for user roles
 * A user may have some roles (user, admin,...)
 */
@RestApi(name = "sec role services", description = "Methods for managing user role")
class RestSecRoleController extends RestController {

    def secRoleService

    /**
     * List all roles available on cytomine
     */
    @RestApiMethod(description="List all roles available on cytomine", listing=true)
    def list() {
        responseSuccess(secRoleService.list())
    }

    @RestApiMethod(description="Get a role")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The role id")
    ])
    def show() {
        responseSuccess(secRoleService.read(params.id))
    }
}
