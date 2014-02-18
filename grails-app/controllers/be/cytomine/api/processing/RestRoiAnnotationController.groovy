package be.cytomine.api.processing

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.SecurityACL
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.RoiAnnotation
import be.cytomine.security.ForgotPasswordToken
import be.cytomine.security.SecRole
import be.cytomine.security.User
import be.cytomine.social.SharedAnnotation
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.annotation.ApiResponseObject
import org.jsondoc.core.pojo.ApiParamType

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Controller for annotation created by user
 */
@Api(name = "roi annotation services", description = "Methods for managing an region of interest annotation")
class RestRoiAnnotationController extends RestController {

    def roiAnnotationService
    def termService
    def imageInstanceService
    def secUserService
    def projectService
    def cytomineService
    def annotationListingService
    def imageProcessingService

    /**
     * Get a single annotation
     */
    @ApiMethodLight(description="Get a roi annotation")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
    ])
    def show() {
        RoiAnnotation annotation = roiAnnotationService.read(params.long('id'))
        if (annotation) {
            responseSuccess(annotation)
        }
        else responseNotFound("Annotation", params.id)
    }

    /**
     * Add annotation created by user
     */
    @ApiMethodLight(description="Add an annotation created by user")
    def add(){
        add(roiAnnotationService, request.JSON)
    }


    /**
     * Update annotation created by user
     */
    @ApiMethodLight(description="Update an annotation")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
    ])
    def update() {
        def json = request.JSON
        try {
            def domain = roiAnnotationService.retrieve(json)
            def result = roiAnnotationService.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Delete annotation created by user
     */
    @ApiMethodLight(description="Delete an annotation")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
    ])
    def delete() {
        def json = JSON.parse("{id : $params.id}")
        delete(roiAnnotationService, json,null)
    }


    /**
     * Get annotation user crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    @ApiMethodLight(description="Get annotation user crop (image area that frame annotation)")
    @ApiResponseObject(objectIdentifier = "file")
    @ApiParams(params=[
    @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id"),
    @ApiParam(name="max_size", type="int", paramType = ApiParamType.PATH,description = "Maximum size of the crop image (w and h)"),
    @ApiParam(name="zoom", type="int", paramType = ApiParamType.PATH,description = "Zoom level"),
    @ApiParam(name="draw", type="boolean", paramType = ApiParamType.PATH,description = "Draw annotation form border on the image")
    ])
    def crop() {
        RoiAnnotation annotation = RoiAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("Annotation", params.id)
        } else {
            responseBufferedImage(imageProcessingService.crop(annotation, params))
        }

    }

    //TODO:APIDOC
    def cropMask () {
        RoiAnnotation annotation = RoiAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("RoiAnnotation", params.id)
        } else {
            responseBufferedImage(imageProcessingService.getMaskImage(annotation, params, false))
        }

    }

    //TODO:APIDOC
    def cropAlphaMask () {
        RoiAnnotation annotation = RoiAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("RoiAnnotation", params.id)
        } else {
            responseBufferedImage(imageProcessingService.getMaskImage(annotation, params, true))
        }

    }


}
