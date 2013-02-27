package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationProperty
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import grails.converters.JSON


class RestAnnotationPropertyController extends RestController {

    def annotationPropertyService
    def cytomineService
    def projectService

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
        def idProject = params.long('idProject')
        def idImage = params.long('idImage')

        println params
        println idProject
        println idImage

        if (idProject != null) {
            Project project = projectService.read(idProject)
            if (project) {
                responseSuccess(annotationPropertyService.listKeys(project, null))
            } else {
                responseNotFound("AnnotationProperty - Project", params.idProject)
            }
        } else if (idImage != null) {
            ImageInstance image = ImageInstance.findById(idImage)
            if (image) {
                responseSuccess(annotationPropertyService.listKeys(null, image))
            } else {
                responseNotFound("AnnotationProperty - Image", params.idImage)
            }
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
