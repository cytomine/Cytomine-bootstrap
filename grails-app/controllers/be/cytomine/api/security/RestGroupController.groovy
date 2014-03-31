package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.security.Group
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for group of users
 */
@RestApi(name = "group services", description = "Methods for managing user groups")
class RestGroupController extends RestController {

    def abstractImageService
    def groupService

    /**
     * List all group
     */
    @RestApiMethod(description="List all group", listing=true)
    def list() {
        responseSuccess(groupService.list())
    }

    /**
     * Get a group info
     */
    @RestApiMethod(description="Get a group")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The group id")
    ])
    def show() {
        Group group = groupService.read(params.long('id'))
        if (group) {
            responseSuccess(group)
        } else {
            responseNotFound("Group", params.id)
        }
    }

    /**
     * Add a new group
     */
    @RestApiMethod(description="Add a new group")
    def add() {
        add(groupService, request.JSON)
    }

    /**
     * Update a group
     */
    @RestApiMethod(description="Edit a group")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The group id")
    ])
    def update() {
        update(groupService, request.JSON)
    }

    /**
     * Delete a group
     */
    @RestApiMethod(description="Delete a group")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The group id")
    ])
    def delete() {
        delete(groupService, JSON.parse("{id : $params.id}"),null)
    }
}
