package be.cytomine.api.ontology

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
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.Term
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.restapidoc.annotation.*
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller that handle request on annotation created by software (job)
 * Annotation my be created by humain (RestUserAnnotationController).
 */
@RestApi(name = "algo annotation services", description = "Methods for managing an annotation created by a software")
class  RestAlgoAnnotationController extends RestController {

    def exportService
    def algoAnnotationService
    def termService
    def imageInstanceService
    def secUserService
    def projectService
    def cytomineService
    def dataSource
    def algoAnnotationTermService
    def paramsService
    def unionGeometryService
    def annotationIndexService
    def reportService
    def imageProcessingService

    /**
     * List all annotation (created by algo) visible for the current user
     */
    @RestApiMethod(description="List all software annotation visible for the current user. See 'annotation domain' data for parameters (only show/hide parameters are available for this service). ", listing = true)
    @RestApiResponseObject(objectIdentifier =  "[annotation listing]")
    def list() {
        def annotations = []
        //get all user's project and list all algo annotation
        def projects = projectService.list()
        projects.each {
            annotations.addAll(algoAnnotationService.list(it,paramsService.getPropertyGroupToShow(params)))
        }
        responseSuccess(annotations)
    }

    /**
     * Read a single algo annotation
     */
    @RestApiMethod(description="Get an algo annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The annotation id")
    ])
    def show() {
        AlgoAnnotation annotation = algoAnnotationService.read(params.long('id'))
        if (annotation) {
            responseSuccess(annotation)
        }
        else {
            responseNotFound("Annotation", params.id)
        }
    }

    /**
     * Add an annotation created by an algo
     * If JSON request params is an object, create a new annotation
     * If its a json array, create multiple annotation
     */
    @RestApiMethod(description="Add an algo annotation")
    def add(){
        def json = request.JSON
        try {
            if (json instanceof JSONArray) {
                responseResult(addMultiple(algoAnnotationService, json))
            } else {
                responseResult(addOne(algoAnnotationService, json))
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @Override
    public Object addOne(def service, def json) {
        if ((!json.project || json.isNull('project'))) {
            //fill project id thanks to image info
            ImageInstance image = ImageInstance.read(json.image)
            if (image) {
                json.project = image.project.id
            }
        }
        if (json.isNull('project')) {
            throw new WrongArgumentException("Annotation must have a valid project:" + json.project)
        }
        if (json.isNull('location')) {
            throw new WrongArgumentException("Annotation must have a valid geometry:" + json.location)
        }
        def minPoint = params.getLong('minPoint')
        def maxPoint = params.getLong('maxPoint')

        def result = algoAnnotationService.add(json,minPoint,maxPoint)
        return result
    }

    /**
     * Update a single annotation created by algo
     */
    @RestApiMethod(description="Update an algo annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
    ])
    def update() {
        def json = request.JSON
        try {
            //get annotation from DB
            def domain = algoAnnotationService.retrieve(json)
            //update it thanks to JSON in request
            def result = algoAnnotationService.update(domain,json)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Delete a single annotation created by algo
     */
    @RestApiMethod(description="Delete an algo annotation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id")
    ])
    def delete() {
        def json = JSON.parse("{id : $params.id}")
        delete(algoAnnotationService, json,null)
    }

    @RestApiMethod(description="Download a report (pdf, xls,...) with software annotation data from a specific project")
    @RestApiResponseObject(objectIdentifier =  "file")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id"),
        @RestApiParam(name="terms", type="list", paramType = RestApiParamType.QUERY,description = "The annotation terms id (if empty: all terms)"),
        @RestApiParam(name="users", type="list", paramType = RestApiParamType.QUERY,description = "The annotation users id (if empty: all users)"),
        @RestApiParam(name="images", type="list", paramType = RestApiParamType.QUERY,description = "The annotation images id (if empty: all images)"),
        @RestApiParam(name="format", type="string", paramType = RestApiParamType.QUERY,description = "The report format (pdf, xls,...)")
    ])
    def downloadDocumentByProject() {
        reportService.createAnnotationDocuments(params.long('id'),params.terms,params.users,params.images,params.format,response,"ALGOANNOTATION")
    }


    /**
     * Get annotation algo crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    @RestApiMethod(description="Get annotation algo crop (image area that frame annotation)")
    @RestApiResponseObject(objectIdentifier =  "file")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The annotation id"),
        @RestApiParam(name="maxSize", type="int", paramType = RestApiParamType.PATH,description = "Maximum size of the crop image (w and h)"),
        @RestApiParam(name="zoom", type="int", paramType = RestApiParamType.PATH,description = "Zoom level"),
        @RestApiParam(name="draw", type="boolean", paramType = RestApiParamType.PATH,description = "Draw annotation form border on the image"),
    ])
    def crop() {
        AlgoAnnotation annotation = AlgoAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("AlgoAnnotation", params.id)
        } else {
            redirect (url : annotation.toCropURL(params))
        }

    }

    //TODO:APIDOC
    def cropMask () {
        AlgoAnnotation annotation = AlgoAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("AlgoAnnotation", params.id)
        } else {
            params.mask = true
            redirect (url : annotation.toCropURL(params))
        }

    }

    //TODO:APIDOC
    def cropAlphaMask () {
        AlgoAnnotation annotation = AlgoAnnotation.read(params.long("id"))
        if (!annotation) {
            responseNotFound("AlgoAnnotation", params.id)
        } else {
            params.alphaMask = true
            redirect (url : annotation.toCropURL(params))
        }

    }

    /**
     * Do union operation between annotation from the same image, user and term.
     * Params are:
     * -minIntersectionLength: size of the intersection geometry between two annotation to merge them
     * -bufferLength: tolerance threshold for two annotation (if they are very close but not intersect)
     */
    @Deprecated
    def union() {
        ImageInstance image = ImageInstance.read(params.getLong('idImage'))
        SecUser user = SecUser.read(params.getLong('idUser'))
        Term term = Term.read(params.getLong('idTerm'))
        Integer minIntersectLength = params.getInt('minIntersectionLength')
        Integer bufferLength = params.getInt('bufferLength')
        Integer area = params.getInt('area')

        if (!image) {
            responseNotFound("ImageInstance", params.getLong('idImage'))
        }
        else if (!term) {
            responseNotFound("Term", params.getLong('idTerm'))
        }
        else if (!user) {
            responseNotFound("User", params.getLong('idUser'))
        }
        else {
            if(!area) {
                //compute a good "windows area" (depend of number of annotation and image size)
                //little image with a lot of annotataion must be very short window size
                def annotationNumber = annotationIndexService.count(image,user)
                def imageSize = image.baseImage.width*image.baseImage.height
                area = (Math.sqrt(imageSize)/(annotationNumber/1000))/4
                area = Math.max(area,500)
            }
            unionAnnotations(image, user, term, minIntersectLength, bufferLength,area)
            def data = [:]
            data.annotationunion = [:]
            data.annotationunion.status = "ok"
            responseSuccess(data)
        }
    }

    /**
     * Merge all annotation from the image, user and term that touch with min minIntersectLength size and with a tolerance threshold bufferLength
     * @param image Image
     * @param user User
     * @param term Term
     * @param minIntersectLength  size of the intersection geometry between two annotation to merge them
     * @param bufferLength tolerance threshold for two annotation (if they are very close but not intersect)
     */
    private def unionAnnotations(ImageInstance image, SecUser user, Term term, Integer minIntersectLength, Integer bufferLength, Integer area) {
        unionGeometryService.unionPicture(image,user,term,area,area,bufferLength,minIntersectLength)
    }
}
