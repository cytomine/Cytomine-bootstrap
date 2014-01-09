package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.RestController
import be.cytomine.api.UrlApi
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

import java.text.SimpleDateFormat

/**
 * Controller that handle request on annotation created by software (job)
 * Annotation my be created by humain (RestUserAnnotationController).
 */
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

    /**
     * List all annotation (created by algo) visible for the current user
     */
    def list = {
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
    def show = {
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
    def add = {
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
    def update = {
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
    def delete = {
        def json = JSON.parse("{id : $params.id}")
        delete(algoAnnotationService, json,null)
    }

    def downloadDocumentByProject = {
        reportService.createAnnotationDocuments(params.long('id'),params.terms,params.users,params.images,params.format,response,"ALGOANNOTATION")
    }

    /**
     * Do union operation between annotation from the same image, user and term.
     * Params are:
     * -minIntersectionLength: size of the intersection geometry between two annotation to merge them
     * -bufferLength: tolerance threshold for two annotation (if they are very close but not intersect)
     */
    def union = {
        ImageInstance image = ImageInstance.read(params.getLong('idImage'))
        SecUser user = SecUser.read(params.getLong('idUser'))
        Term term = Term.read(params.getLong('idTerm'))
        Integer minIntersectLength = params.getInt('minIntersectionLength')
        Integer bufferLength = params.getInt('bufferLength')
        Integer area = params.getInt('area')

        println "area=$area"
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
                println "area=$area"
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
        println "area=$area"
        unionGeometryService.unionPicture(image,user,term,area,area,bufferLength,minIntersectLength)
    }
}
