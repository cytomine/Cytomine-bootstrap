package be.cytomine.api.image

import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Controller that handle request for project images.
 */
@Api(name = "nested image services", description = "Methods for managing a nested image, a sub-image of an existing image instance")
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

    @ApiMethodLight(description="Get a nested image")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH, description = "The nested image id")
    ])
    def show() {
        ImageInstance image = nestedImageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("NestedImageInstance", params.id)
        }
    }

    @ApiMethodLight(description="List all nested image for an image instance", listing = true)
    @ApiParams(params=[
        @ApiParam(name="idImage", type="long", paramType = ApiParamType.PATH, description = "The image instance id")
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

    @ApiMethodLight(description="Add a new nested image (from an image instance)")
    def add() {
        try {
            responseResult(nestedImageInstanceService.add(request.JSON))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @ApiMethodLight(description="Update a nested image instance")
    def update() {
        update(nestedImageInstanceService, request.JSON)
    }

    @ApiMethodLight(description="Delete a nested image instance)")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The nested image id")
    ])
    def delete() {
        delete(nestedImageInstanceService, JSON.parse("{id : $params.id}"),null)
    }
}
