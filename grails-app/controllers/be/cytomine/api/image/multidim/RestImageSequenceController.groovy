package be.cytomine.api.image.multidim

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

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import grails.converters.JSON
import org.restapidoc.annotation.*
import org.restapidoc.pojo.RestApiParamType

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 */
@RestApi(name = "image sequence services", description = "Methods for managing image sequence that represent an image from a group in a given channel, zstack, slice, time...")
class RestImageSequenceController extends RestController {

    def imageSequenceService
    def imageGroupService
    def imageInstanceService
    def projectService

    @RestApiMethod(description="Get an image sequence")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image sequence id")
    ])
    def show() {
        ImageSequence image = imageSequenceService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("ImageGroup", params.id)
        }
    }

    @RestApiMethod(description="Get all image sequence from an image group", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The group id")
    ])
    def listByImageGroup() {
        ImageGroup imageGroup = imageGroupService.read(params.long('id'))
        if (imageGroup)  {
            responseSuccess(imageSequenceService.list(imageGroup))
        }
        else {
            responseNotFound("ImageSequence", "ImageGroup", params.id)
        }
    }

    @RestApiMethod(description="List all image sequence from a specific image instance", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image instance id")
    ])
    def getByImageInstance () {
        ImageInstance imageInstance = imageInstanceService.read(params.long('id'))
        if (imageInstance)  {
            responseSuccess(imageSequenceService.get(imageInstance))
        }
        else {
            responseNotFound("ImageSequence", "ImageInstance", params.id)
        }
    }

    @RestApiMethod(description="Get the image dimension index (e.g. c=0, z=1, t=3,...) and the possible range for each dimension (e.g. image x has channel [0-2], zstack only 0, time [0-1],... ")
    @RestApiResponseObject(objectIdentifier =  "[sequence_possibilties]")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image instance id")
    ])
    def getSequenceInfo () {
        ImageInstance imageInstance = imageInstanceService.read(params.long('id'))
        if (imageInstance)  {
            responseSuccess(imageSequenceService.getPossibilities(imageInstance))
        }
        else {
            responseNotFound("ImageSequence", "ImageInstance", params.id)
        }
    }

    @RestApiMethod(description="Get the image sequence in the given channel, zstack,... and image group", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image group id"),
        @RestApiParam(name="zstack", type="long", paramType = RestApiParamType.PATH, description = "Zstack filter"),
        @RestApiParam(name="time", type="long", paramType = RestApiParamType.PATH, description = "Time filter"),
        @RestApiParam(name="channel", type="long", paramType = RestApiParamType.PATH, description = "Channel filter"),
        @RestApiParam(name="slice", type="long", paramType = RestApiParamType.PATH, description = "Slice filter")
    ])
    def getByImageGroupAndIndex () {
        try {
            ImageGroup imageGroup = imageGroupService.read(params.long('id'))
            if (imageGroup)  {
                Integer zStack = params.int("zstack")
                Integer time = params.int("time")
                Integer channel = params.int("channel")
                Integer slice = params.int("slice")
                responseSuccess(imageSequenceService.get(imageGroup,channel,zStack,slice,time))
            }
            else {
                responseNotFound("ImageSequence", "ImageInstance", params.id)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @RestApiMethod(description="Add a new image sequence (index a new image instance at a given channel, zstack,... in an image group")
    def add () {
        add(imageSequenceService, request.JSON)
    }

    @RestApiMethod(description="Update an image sequence (id must be defined in post data JSON)")
    def update () {
        update(imageSequenceService, request.JSON)
    }

    @RestApiMethod(description="Delete an image sequence)")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image sequence id")
    ])
    def delete () {
        delete(imageSequenceService, JSON.parse("{id : $params.id}"),null)
    }
}
