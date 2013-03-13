package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationProperty
import be.cytomine.project.Project
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.geom.Geometry
import grails.converters.JSON

class RestAnnotationPropertyController extends RestController {

    def annotationPropertyService
    def cytomineService
    def projectService
    def imageInstanceService
    def secUserService

    /**
     * List all annotationProperty visible for the current user
     */
    def list = {
        responseSuccess(annotationPropertyService.list())
    }

    /**
     * List all annotationProperty visible for the current user by AnnotationDomain
     */
    def listByAnnotation = {
        def annotationId = params.long('idAnnotation')
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(annotationId)
        if(annotation) {
            responseSuccess(annotationPropertyService.list(annotation))
        } else {
            responseNotFound("Annotation",params.idAnnotation)
        }

    }

    def listKey = {
        Project project = projectService.read(params.long('idProject'))
        ImageInstance image = imageInstanceService.read(params.long('idImage'))

        if(image) {
            responseSuccess(annotationPropertyService.listKeys(null, image))
        } else if(project) {
            responseSuccess(annotationPropertyService.listKeys(project, null))
        } else {
            responseNotFound("AnnotationProperty","Image/Project", params.idImage+"/"+params.idProject)
        }
    }

    def listAnnotationPosition = {
        def image = imageInstanceService.read(params.long('idImage'))
        def user = secUserService.read(params.idUser)
        if (image && user && params.key) {

            Geometry boundingbox = null
            if(params.bbox!=null) {
                boundingbox = GeometryUtils.createBoundingBox(params.bbox)
            }

            def data = annotationPropertyService.listAnnotationCenterPosition(user, image, boundingbox, params.key)
            responseSuccess(data)
        } else if (!user) {
            responseNotFound("User", params.idUser)
        } else if (!image) {
            responseNotFound("Image", params.idImage)
        }
    }

    def show = {

        AnnotationProperty annotationProperty
        if(params.id != null) {
            annotationProperty = annotationPropertyService.read(params.id)
        } else {
            annotationProperty = annotationPropertyService.read(params.idAnnotation, params.key)
        }

        if (annotationProperty) {
            responseSuccess(annotationProperty)
        } else {
            responseNotFound("AnnotationProperty", params.id)
        }
    }

    /**
     * Add a new AnnotationProperty (Method from RestController)
     */
    def add = {
        add(annotationPropertyService, request.JSON)
    }

    /**
     * Update a AnnotationProperty (Method from RestController)
     */
    def update = {
        update(annotationPropertyService, request.JSON)
    }

    /**
     * Delete a AnnotationProperty (Method from RestController)
     */
    def delete = {
        def json = JSON.parse("{id : $params.id}")
        delete(annotationPropertyService,json,null)
    }
}
