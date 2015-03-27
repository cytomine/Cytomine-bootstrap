package be.cytomine.api.security

import be.cytomine.api.RestController
import be.cytomine.security.Group
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

import static be.cytomine.security.Group.*

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
    @RestApiParams(params=[
            @RestApiParam(name="withUser", type="boolean", paramType = RestApiParamType.QUERY, description = "(Optional) If set, each group will have the array of its users."),
    ])
    def list() {
        if (Boolean.parseBoolean(params.withUser) == true) {
            responseSuccess(groupService.listWithUser())
        } else {
            responseSuccess(groupService.list())
        }
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

    @RestApiMethod(description="Check if a group is in the LDAP")
    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The group id "),
    ])
    def isInLDAP() {
        def result = groupService.isInLdap(params.long('id'))
        def returnArray = [:]
        returnArray["result"] = result
        responseSuccess(returnArray)
    }

    @RestApiMethod(description="Create a group from the LDAP")
    def createFromLDAP() {
        responseSuccess(groupService.createFromLDAP(request.JSON))
    }

    @RestApiParams(params=[
            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The group id "),
    ])
    @RestApiMethod(description="Reset a group with the informations in the LDAP")
    def resetFromLDAP() {
        responseSuccess(groupService.resetFromLDAP(params.long('id')))
    }
}
