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
import be.cytomine.security.Group
import be.cytomine.security.User
import be.cytomine.security.UserGroup
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller to manage user in group
 */
@RestApi(name = "user group services", description = "Methods for managing a user in groups")
class RestUserGroupController extends RestController {

    def userGroupService
    def secUserService
    def groupService
    def cytomineService

    /**
     * List all user-group for a user
     */
    @RestApiMethod(description="List all user group for a user", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="user", type="long", paramType = RestApiParamType.PATH, description = "The user id")
    ])
    def list() {
        User user = secUserService.read(params.long('user'));
        responseSuccess(userGroupService.list(user))
    }

    /**
     * Get a user-group relation
     */
    @RestApiMethod(description="Get a user-group relation")
    @RestApiParams(params=[
        @RestApiParam(name="user", type="long", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="group", type="long", paramType = RestApiParamType.PATH, description = "The group id")
    ])
    def show() {
        User user = secUserService.read(params.long('user'));
        Group group = groupService.read(params.long('group'));
        UserGroup userGroup = userGroupService.get(user, group)
        if (!userGroup) {
            responseNotFound("UserGroup", params.user)
        } else {
            responseSuccess(userGroup)
        }
    }

    /**
     * Add a new user to a group
     */
    @RestApiMethod(description="Get a user-group relation")
    def add() {
        add(userGroupService, request.JSON)
    }

    /**
     * Remove a user from a group
     */
    @RestApiMethod(description="Remove a user from a group")
    @RestApiParams(params=[
        @RestApiParam(name="user", type="long", paramType = RestApiParamType.PATH, description = "The user id"),
        @RestApiParam(name="group", type="long", paramType = RestApiParamType.PATH, description = "The group id")
    ])
    def delete() {
        def json = JSON.parse("{user : $params.user, group: $params.group}")
        delete(userGroupService, json,null)
    }
}
