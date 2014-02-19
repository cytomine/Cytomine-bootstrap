package be.cytomine.api.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Property
import be.cytomine.project.Project
import be.cytomine.utils.GeometryUtils
import com.vividsolutions.jts.geom.Geometry
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

@Api(name = "property services", description = "Methods for managing properties")
class RestPropertyController extends RestController {

    def propertyService
    def cytomineService
    def projectService
    def imageInstanceService
    def secUserService

    /**
     * List all Property visible for the current user by Project, AnnotationDomain and ImageInstance
     */
    @ApiMethodLight(description="Get all properties for a project", listing=true)
    @ApiParams(params=[
        @ApiParam(name="idProject", type="long", paramType = ApiParamType.PATH,description = "The project id")
    ])
    def listByProject() {
        def projectId = params.long('idProject')
        Project project = projectService.read(projectId)
        if(project) {
            responseSuccess(propertyService.list(project))
        } else {
            responseNotFound("Project",params.idProject)
        }
    }

    @ApiMethodLight(description="Get all properties for an annotation (algo,user, or reviewed)", listing=true)
    @ApiParams(params=[
        @ApiParam(name="idAnnotation", type="long", paramType = ApiParamType.PATH,description = "The annotation id")
    ])
    def listByAnnotation() {
        try {
            def annotationId = params.long('idAnnotation')
            AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(annotationId)
            if(annotation) {
                responseSuccess(propertyService.list(annotation))
            } else {
                responseNotFound("Annotation",params.idAnnotation)
            }
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    @ApiMethodLight(description="Get all properties for an image instance", listing=true)
    @ApiParams(params=[
        @ApiParam(name="idImageInstance", type="long", paramType = ApiParamType.PATH,description = "The image instance id")
    ])
    def listByImageInstance() {
        def imageInstanceId = params.long('idImageInstance')
        ImageInstance imageInstance = imageInstanceService.read(imageInstanceId)
        if(imageInstance) {
            responseSuccess(propertyService.list(imageInstance))
        } else {
            responseNotFound("ImageInstance",params.idImageInstance)
        }
    }

    @ApiMethodLight(description="Get all keys of annotation properties in a project or image", listing=true)
    @ApiParams(params=[
        @ApiParam(name="idProject", type="long", paramType = ApiParamType.QUERY,description = "(Optional, if null idImage must be set) The project id"),
        @ApiParam(name="idImage", type="long", paramType = ApiParamType.QUERY,description = "(Optional, if null idProject must be set) The image instance id")
    ])
    def listKeyForAnnotation() {
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

    @ApiMethodLight(description="Get all keys of images properties in a project", listing=true)
    @ApiParams(params=[
        @ApiParam(name="idProject", type="long", paramType = ApiParamType.QUERY,description = "(Optional, if null idImage must be set) The project id"),
    ])
    def listKeyForImageInstance() {
        Project project = projectService.read(params.long('idProject'))

        if(project) {
            responseSuccess(propertyService.listKeysForImageInstance(project))
        } else {
            responseNotFound("Property","Project", params.idProject)
        }
    }

    @ApiMethodLight(description="For a specific key, Get all annotation centroid (x,y) and the corresponding value for an image and a layer (user)", listing=true)
    @ApiParams(params=[
        @ApiParam(name="idImage", type="long", paramType = ApiParamType.PATH,description = "The image id"),
        @ApiParam(name="idUser", type="long", paramType = ApiParamType.PATH,description = "The layer id"),
        @ApiParam(name="key", type="long", paramType = ApiParamType.QUERY,description = "The properties key"),
        @ApiParam(name="bbox", type="string", paramType = ApiParamType.QUERY,description = "(Optional) Form of the restricted area"),
    ])
    def listAnnotationPosition() {
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

    @ApiMethodLight(description="Get a project property with tis id or its key")
    @ApiParams(params=[
        @ApiParam(name="idProject", type="long", paramType = ApiParamType.PATH, description = "The project id"),
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "(Optional, if null key must be set) The property id"),
        @ApiParam(name="key", type="long", paramType = ApiParamType.PATH,description = "(Optional, if null id must be set) The property key")
    ])
    def showProject() {
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

    @ApiMethodLight(description="Get a project property with its id or its key")
    @ApiParams(params=[
        @ApiParam(name="idAnnotation", type="long", paramType = ApiParamType.PATH, description = "The annotation id"),
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "(Optional, if null key must be set) The property id"),
        @ApiParam(name="key", type="long", paramType = ApiParamType.PATH,description = "(Optional, if null id must be set) The property key")
    ])
    def showAnnotation() {
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

    @ApiMethodLight(description="Get an image instance property with its id or its key")
    @ApiParams(params=[
        @ApiParam(name="idImageInstance", type="long", paramType = ApiParamType.PATH, description = "The image instance id"),
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "(Optional, if null key must be set) The property id"),
        @ApiParam(name="key", type="long", paramType = ApiParamType.PATH,description = "(Optional, if null id must be set) The property key")
    ])
    def showImageInstance() {
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
    @ApiMethodLight(description="Add a property to a project")
    @ApiParams(params=[
        @ApiParam(name="idProject", type="long", paramType = ApiParamType.PATH, description = "The project id"),
    ])
    def addPropertyProject() {
        def json = request.JSON
        json.domainClassName = Project.getName()
        add(propertyService, request.JSON)
    }

    @ApiMethodLight(description="Add a property to an annotation")
    @ApiParams(params=[
        @ApiParam(name="idAnnotation", type="long", paramType = ApiParamType.PATH, description = "The annotation id"),
    ])
    def addPropertyAnnotation()  {
        def json = request.JSON
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(json.domainIdent)
        json.domainClassName = annotation.class.getName()
        add(propertyService, request.JSON)
    }

    @ApiMethodLight(description="Add a property to a image instance")
    @ApiParams(params=[
        @ApiParam(name="idImageInstance", type="long", paramType = ApiParamType.PATH, description = "The image instance id"),
    ])
    def addPropertyImageInstance()  {
        def json = request.JSON
        json.domainClassName = ImageInstance.getName()
        add(propertyService, request.JSON)
    }


    /**
     * Update a Property (Method from RestController)
     */
    @ApiMethodLight(description="Edit a property")
    @ApiParams(params=[
        @ApiParam(name="idAnnotation", type="long", paramType = ApiParamType.PATH,description = "(Optional) The annotation id"),
        @ApiParam(name="idImageInstance", type="long", paramType = ApiParamType.PATH,description = "(Optional) The image instance id"),
        @ApiParam(name="idProject", type="long", paramType = ApiParamType.PATH,description = "(Optional) The project id")
    ])
    def update() {
        update(propertyService, request.JSON)
    }

    /**
     * Delete a Property (Method from RestController)
     */
    @ApiMethodLight(description="Delete a property")
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The property id")
    ])
    def delete()  {
        def json = JSON.parse("{id : $params.id}")
        delete(propertyService,json,null)
    }
}
