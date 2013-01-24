package be.cytomine.api.image

import be.cytomine.AnnotationDomain
import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import grails.converters.JSON

/**
 * Controller for abstract image
 * An abstract image can be add in n projects
 */
class RestImageController extends RestController {

    def imagePropertiesService
    def abstractImageService
    def cytomineService
    def projectService

    /**
     * List all abstract image available on cytomine
     */
    def list = {
        SecUser user = cytomineService.getCurrentUser()
        if(params.getBoolean('datatable')) {
            responseSuccess(abstractImageService.list(user, params.page, params.rows, params.sidx, params.sord, params.filename, params.createdstart, params.createdstop))
        } else {
            response(abstractImageService.list(user))
        }
    }

    /**
     * List all abstract images for a project
     */
    def listByProject = {
        Project project = Project.read(params.id)
        if (project) {
            responseSuccess(abstractImageService.list(project))
        } else {
            responseNotFound("Image", "Project", params.id)
        }
    }

    /**
     * Get a single image
     */
    def show = {
        AbstractImage image = abstractImageService.read(params.long('id'))
        if (image) {
            responseSuccess(image)
        } else {
            responseNotFound("Image", params.id)
        }
    }

    /**
     * Add a new image
     * TODO:: how to manage security here?
     */
    def add = {
        add(abstractImageService, request.JSON)
    }

    /**
     * Update a new image
     * TODO:: how to manage security here?
     */
    def update = {
        update(abstractImageService, request.JSON)
    }

    /**
     * Delete a new image
     * TODO:: how to manage security here?
     */
    def delete = {
        delete(abstractImageService, JSON.parse("{id : $params.id}"))
    }

    /**
     * Get metadata URL for an images
     * If extract, populate data from metadata table into image object
     */
    def metadata = {
        def idImage = params.long('id')
        def extract = params.boolean('extract')
        if (extract) {
            AbstractImage image = abstractImageService.read(idImage)
            imagePropertiesService.extractUseful(image)
            image.save(flush : true)
        }
        def responseData = [:]
        responseData.metadata = abstractImageService.metadata(idImage)
        response(responseData)
    }

    /**
     * Extract image properties from file
     */
    def imageProperties = {
        response(abstractImageService.imageProperties(params.long('id')))
    }

    /**
     * Get an image property
     */
    def imageProperty = {
        def imageProperty = abstractImageService.imageProperty(params.long('imageproperty'))
        if (imageProperty) {
            responseSuccess(imageProperty)
        } else {
            responseNotFound("ImageProperty", params.imageproperty)
        }
    }

    /**
     * Get all image servers URL for an image
     */
    def imageservers = {
        responseSuccess(abstractImageService.imageservers(params.long('id')))
    }

    /**
     * Get image thumb URL
     */
    def thumb = {
        def url = abstractImageService.thumb(params.long('id'))
        println "controller.url=$url"
        responseImage(url)
    }

    /**
     * Get image preview URL
     */
    def preview = {
        responseImage(abstractImageService.preview(params.long('id')))
    }

    /**
     * Get annotation crop (image area that frame annotation)
     * This work for all kinds of annotations
     */
    def cropAnnotation = {
        try {
            def annotation = AnnotationDomain.getAnnotationDomain(params.id)
            def cropURL = getCropAnnotationURL(annotation,params)
            if(cropURL!=null) responseImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    /**
     * Get annotation crop (image area that frame annotation)
     * Force the size for crop annotation
     * This work for all kinds of annotations
     */
    def cropAnnotationMin = {
        try {
            params.max_size = "256"
            def annotation = AnnotationDomain.getAnnotationDomain(params.id)
            def cropURL = getCropAnnotationURL(annotation,params)
            if(cropURL!=null) responseImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    /**
     * Get annotation user crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    def cropUserAnnotation = {
        try {
            def annotation = UserAnnotation.read(params.id)
            def cropURL = getCropAnnotationURL(annotation,params)
            if(cropURL!=null) responseImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    /**
     * Get annotation algo crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    def cropAlgoAnnotation = {
        try {
            def annotation = AlgoAnnotation.read(params.id)
            def cropURL = getCropAnnotationURL(annotation,params)
            responseImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    /**
     * Get annotation review crop (image area that frame annotation)
     * (Use this service if you know the annotation type)
     */
    def cropReviewedAnnotation = {
        try {
            def annotation = ReviewedAnnotation.read(params.id)
            def cropURL = getCropAnnotationURL(annotation,params)
            responseImage(cropURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    /**
     * Get crop annotation URL
     * @param annotation Annotation
     * @param params Params
     * @return Crop Annotation URL
     */
    private def getCropAnnotationURL(AnnotationDomain annotation, def params) {
        Integer zoom = 0
        Integer maxSize = -1
        if (params.max_size != null) {
            maxSize =  Integer.parseInt(params.max_size)
        }
        if (params.zoom != null) {
            zoom = Integer.parseInt(params.zoom)
        }

        if (annotation == null) {
            responseNotFound("Crop", "Annotation", params.id)
        } else if ((params.zoom != null) && (zoom < annotation.getImage().getBaseImage().getZoomLevels().min || zoom > annotation.getImage().getBaseImage().getZoomLevels().max)) {
            responseNotFound("Crop", "Zoom", zoom)
        } else {
            try {
                String cropURL
                if (maxSize != -1) {
                    cropURL = abstractImageService.cropWithMaxSize(annotation, maxSize)
                } else {
                    cropURL = abstractImageService.crop(annotation, zoom)
                }

                if (cropURL == null) {
                    //no crop available, add lambda image
                    cropURL = grailsApplication.config.grails.serverURL + "/images/cytomine.jpg"
                }
                return cropURL
            } catch (Exception e) {
                e.printStackTrace()
                log.error("GetCrop:" + e)
                return null
            }
        }
    }

    /**
     * TODOSTEVBEN: doc
     */
    def slidingWindow = {
        int width = params.width != null ? Integer.parseInt(params.width) : 1000
        int height = params.height != null ? Integer.parseInt(params.width) : 1000
        float overlapX = params.overlapX != null ? Float.parseFloat(params.overlapX) : 0
        float overlapY = params.overlapY != null ? Float.parseFloat(params.overlapY) : 0
        AbstractImage image = abstractImageService.read(params.long('id'))
        if (image) {
            responseSuccess(abstractImageService.slidingWindow(image, [ width : width, height : height, overlapX : overlapX, overlapY : overlapY]))
        }
    }
}



