package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Property
import be.cytomine.project.Project
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.geom.Geometry
import grails.converters.JSON

class RestPropertyController extends RestController {

    def propertyService
    def cytomineService
    def projectService
    def imageInstanceService
    def secUserService

    /**
     * List all annotationProperty visible for the current user
     */
    def list = {
        responseSuccess(propertyService.list())
    }

    /**
     * List all Property visible for the current user by Project, AnnotationDomain and ImageInstance
     */
    def listByProject = {
        def projectId = params.long('idProject')
        Project project = projectService.read(projectId)
        if(project) {
            responseSuccess(propertyService.list(project))
        } else {
            responseNotFound("Project",params.idProject)
        }
    }
    def listByAnnotation = {
        def annotationId = params.long('idAnnotation')
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(annotationId)
        if(annotation) {
            responseSuccess(propertyService.list(annotation))
        } else {
            responseNotFound("Annotation",params.idAnnotation)
        }
    }
    def listByImageInstance = {
        def imageInstanceId = params.long('idImageInstance')
        ImageInstance imageInstance = imageInstanceService.read(imageInstanceId)
        if(imageInstance) {
            responseSuccess(propertyService.list(imageInstance))
        } else {
            responseNotFound("ImageInstance",params.idImageInstance)
        }
    }


    def listKeyForAnnotation = {
        Project project = projectService.read(params.long('idProject'))
        ImageInstance image = imageInstanceService.read(params.long('idImage'))

        if(image) {
            responseSuccess(propertyService.listKeysForAnnotation(null, image))
        } else if(project) {
            responseSuccess(propertyService.listKeysForAnnotation(project, null))
        } else {
            responseNotFound("Property","Image/Project", params.idImage+"/"+params.idProject)
        }
    }
    def listKeyForImageInstance = {
        Project project = projectService.read(params.long('idProject'))

        if(project) {
            responseSuccess(propertyService.listKeysForImageInstance(project))
        } else {
            responseNotFound("Property","Project", params.idProject)
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

            def data = propertyService.listAnnotationCenterPosition(user, image, boundingbox, params.key)
            responseSuccess(data)
        } else if (!user) {
            responseNotFound("User", params.idUser)
        } else if (!image) {
            responseNotFound("Image", params.idImage)
        }
    }


    def showProject = {
        def projectId = params.long('idProject')
        Project project = projectService.read(projectId)

        Property property
        if(params.id != null) {
            property = propertyService.read(params.id)
        } else if (params.key != null) {
            property = propertyService.read(project, params.key)
        }

        if (property) {
            responseSuccess(property)
        } else {
            responseNotFound("Property", params.id)
        }
    }
    def showAnnotation = {
        def annotationId = params.long('idAnnotation')
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(annotationId)

        Property property
        if(params.id != null) {
            property = propertyService.read(params.id)
        } else if (params.key != null) {
            property = propertyService.read(annotation, params.key)
        }

        if (property) {
            responseSuccess(property)
        } else {
            responseNotFound("Property", params.id)
        }
    }
    def showImageInstance = {
        def imageInstanceId = params.long('idImageInstance')
        ImageInstance imageInstance = imageInstanceService.read(imageInstanceId)

        Property property
        if(params.id != null) {
            property = propertyService.read(params.id)
        } else if (params.key != null) {
            property = propertyService.read(imageInstance, params.key)
        }

        if (property) {
            responseSuccess(property)
        } else {
            responseNotFound("Property", params.id)
        }
    }


    /**
     * Add a new Property (Method from RestController)
     */
    def addPropertyProject = {
        def json = request.JSON
        json.domainClassName = Project.getName()
        add(propertyService, request.JSON)
    }
    def addPropertyAnnotation = {
        def json = request.JSON

        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(json.domainIdent)

        json.domainClassName = annotation.class.getName()
        add(propertyService, request.JSON)
    }
    def addPropertyImageInstance = {
        def json = request.JSON
        json.domainClassName = ImageInstance.getName()
        add(propertyService, request.JSON)
    }


    /**
     * Update a Property (Method from RestController)
     */
    def update = {
        update(propertyService, request.JSON)
    }

    /**
     * Delete a Property (Method from RestController)
     */
    def delete = {
        def json = JSON.parse("{id : $params.id}")
        delete(propertyService,json,null)
    }
}
