package be.cytomine.api.processing

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.processing.RoiAnnotation
import grails.converters.JSON
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.annotation.RestApiResponseObject
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for annotation created by user
 */
@RestApi(name = "roi annotation services", description = "Methods for managing an region of interest annotation")
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
    @RestApiMethod(description="Get a roi annotation")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
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
    @RestApiMethod(description="Add an annotation created by user")
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
    @RestApiMethod(description="Update an annotation")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
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
    @RestApiMethod(description="Delete an annotation")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
    ])
    def delete() {
        def json = JSON.parse("{id : $params.id}")
        delete(roiAnnotationService, json,null)
    }


    /**
     * Get annotation user crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    @RestApiMethod(description="Get annotation user crop (image area that frame annotation)")
    @RestApiResponseObject(objectIdentifier = "file")
    @RestApiParams(params=[
    @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
    @RestApiParam(name="max_size", type="int", paramType = RestApiParamType.PATH,description = "Maximum size of the crop image (w and h)"),
    @RestApiParam(name="zoom", type="int", paramType = RestApiParamType.PATH,description = "Zoom level"),
    @RestApiParam(name="draw", type="boolean", paramType = RestApiParamType.PATH,description = "Draw annotation form border on the image")
    ])
    def crop() {
        RoiAnnotation annotation = RoiAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("Annotation", params.id)
        } else {
            redirect (url : annotation.toCropURL(params))
        }

    }

    //TODO:APIDOC
    def cropMask () {
        RoiAnnotation annotation = RoiAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("RoiAnnotation", params.id)
        } else {
            params.mask = true
            redirect (url : annotation.toCropURL(params))
        }

    }

    //TODO:APIDOC
    def cropAlphaMask () {
        RoiAnnotation annotation = RoiAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("RoiAnnotation", params.id)
        } else {
            params.alphaMask = true
            redirect (url : annotation.toCropURL(params))
        }

    }


}
