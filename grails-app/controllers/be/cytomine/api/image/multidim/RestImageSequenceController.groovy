package be.cytomine.api.image.multidim

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 */
@Api(name = "image sequence services", description = "Methods for managing image sequence that represent an image from a group in a given channel, zstack, slice, time...")
class RestImageSequenceController extends RestController {

    def imageSequenceService
    def imageGroupService
    def imageInstanceService
    def projectService

    @ApiMethodLight(description="Get an image sequence")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image sequence id")
    ])
    def show() {
        ImageSequence image = imageSequenceService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("ImageGroup", params.id)
        }
    }

    @ApiMethodLight(description="Get all image sequence from an image group", listing=true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The group id")
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

    @ApiMethodLight(description="List all image sequence from a specific image instance", listing=true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image instance id")
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

    @ApiMethodLight(description="Get the image dimension index (e.g. c=0, z=1, t=3,...) and the possible range for each dimension (e.g. image x has channel [0-2], zstack only 0, time [0-1],... ")
    @ApiResponseObject(objectIdentifier =  "[sequence_possibilties]")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image instance id")
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

    @ApiMethodLight(description="Get the image sequence in the given channel, zstack,... and image group", listing=true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The image group id"),
        @ApiParam(name="zstack", type="long", paramType = ApiParamType.PATH, description = "Zstack filter"),
        @ApiParam(name="time", type="long", paramType = ApiParamType.PATH, description = "Time filter"),
        @ApiParam(name="channel", type="long", paramType = ApiParamType.PATH, description = "Channel filter"),
        @ApiParam(name="slice", type="long", paramType = ApiParamType.PATH, description = "Slice filter")
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

    @ApiMethodLight(description="Add a new image sequence (index a new image instance at a given channel, zstack,... in an image group")
    def add () {
        add(imageSequenceService, request.JSON)
    }

    @ApiMethodLight(description="Update an image sequence (id must be defined in post data JSON)")
    def update () {
        update(imageSequenceService, request.JSON)
    }

    @ApiMethodLight(description="Delete an image sequence)")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The image sequence id")
    ])
    def delete () {
        delete(imageSequenceService, JSON.parse("{id : $params.id}"),null)
    }
}
