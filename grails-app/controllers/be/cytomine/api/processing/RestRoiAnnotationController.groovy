package be.cytomine.api.processing

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.processing.RoiAnnotation
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import jsondoc.annotation.ApiResponseObjectLight
import org.jsondoc.core.pojo.ApiParamType

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
    @ApiParamsLight(params=[
    @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
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

    @Override
    public Object addOne(def service, def json) {
        if (!json.project || json.isNull('project')) {
            ImageInstance image = ImageInstance.read(json.image)
            if (image) json.project = image.project.id
        }
        if (json.isNull('project')) {
            throw new WrongArgumentException("Annotation must have a valide project:" + json.project)
        }
        if (json.isNull('location')) {
            throw new WrongArgumentException("Annotation must have a valide geometry:" + json.location)
        }
        def minPoint = params.getLong('minPoint')
        def maxPoint = params.getLong('maxPoint')

        def result = roiAnnotationService.add(json,minPoint,maxPoint)
        return result
    }


    /**
     * Update annotation created by user
     */
    @ApiMethodLight(description="Update an annotation")
    @ApiParamsLight(params=[
    @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
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
    @ApiParamsLight(params=[
    @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
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
    @ApiResponseObjectLight(objectIdentifier = "file")
    @ApiParamsLight(params=[
    @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The annotation id"),
    @ApiParamLight(name="max_size", type="int", paramType = ApiParamType.PATH,description = "Maximum size of the crop image (w and h)"),
    @ApiParamLight(name="zoom", type="int", paramType = ApiParamType.PATH,description = "Zoom level"),
    @ApiParamLight(name="draw", type="boolean", paramType = ApiParamType.PATH,description = "Draw annotation form border on the image")
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
