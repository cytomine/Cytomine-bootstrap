package be.cytomine.api.image

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Controller that handle request for project images.
 */
@RestApi(name = "nested image services", description = "Methods for managing a nested image, a sub-image of an existing image instance")
class RestNestedImageInstanceController extends RestController {

    def segmentationService
    def imageProcessingService
    def nestedImageInstanceService
    def imageInstanceService
    def projectService
    def abstractImageService
    def userAnnotationService
    def algoAnnotationService
    def reviewedAnnotationService
    def secUserService
    def termService
    def cytomineService
    def taskService

    @RestApiMethod(description="Get a nested image")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The nested image id")
    ])
    def show() {
        ImageInstance image = nestedImageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("NestedImageInstance", params.id)
        }
    }

    @RestApiMethod(description="List all nested image for an image instance", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="idImage", type="long", paramType = RestApiParamType.PATH, description = "The image instance id")
    ])
    def listByImageInstance() {
        ImageInstance image = imageInstanceService.read(params.long('idImage'))
        if (image)  {
            responseSuccess(nestedImageInstanceService.list(image))
        }
        else {
            responseNotFound("NestedImageInstance", "Image", params.idImage)
        }
    }

    @RestApiMethod(description="Add a new nested image (from an image instance)")
    def add() {
        try {
            responseResult(nestedImageInstanceService.add(request.JSON))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @RestApiMethod(description="Update a nested image instance")
    def update() {
        update(nestedImageInstanceService, request.JSON)
    }

    @RestApiMethod(description="Delete a nested image instance)")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The nested image id")
    ])
    def delete() {
        delete(nestedImageInstanceService, JSON.parse("{id : $params.id}"),null)
    }
}
