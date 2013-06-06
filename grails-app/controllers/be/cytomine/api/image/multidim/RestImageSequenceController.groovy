package be.cytomine.api.image.multidim

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.image.multidim.ImageGroup
import be.cytomine.image.multidim.ImageSequence
import be.cytomine.project.Project
import grails.converters.JSON
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 */
class RestImageSequenceController extends RestController {

    def imageSequenceService
    def imageGroupService
    def imageInstanceService
    def projectService

    def show = {
        ImageSequence image = imageSequenceService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("ImageGroup", params.id)
        }
    }

    def listByImageGroup = {
        ImageGroup imageGroup = imageGroupService.read(params.long('id'))
        if (imageGroup)  {
            responseSuccess(imageSequenceService.list(imageGroup))
        }
        else {
            responseNotFound("ImageSequence", "ImageGroup", params.id)
        }
    }

    def getByImageInstance = {
        ImageInstance imageInstance = imageInstanceService.read(params.long('id'))
        if (imageInstance)  {
            responseSuccess(imageSequenceService.get(imageInstance))
        }
        else {
            responseNotFound("ImageSequence", "ImageInstance", params.id)
        }
    }

    def getSequenceInfo = {
        ImageInstance imageInstance = imageInstanceService.read(params.long('id'))
        if (imageInstance)  {
            responseSuccess(imageSequenceService.getPossibilities(imageInstance))
        }
        else {
            responseNotFound("ImageSequence", "ImageInstance", params.id)
        }
    }

    def getByImageGroupAndIndex = {
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

    def add = {
        add(imageSequenceService, request.JSON)
    }

    def update = {
        update(imageSequenceService, request.JSON)
    }

    def delete = {
        delete(imageSequenceService, JSON.parse("{id : $params.id}"),null)
    }
}
