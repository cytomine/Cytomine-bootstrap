package be.cytomine.api.image

import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.project.Project
import be.cytomine.security.Group
import be.cytomine.security.SecUser
import be.cytomine.security.User
import grails.converters.JSON
import be.cytomine.ontology.UserAnnotation
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.CytomineDomain
import be.cytomine.AnnotationDomain

class RestImageController extends RestController {

    def imagePropertiesService
    def storageService
    def abstractImageService
    def cytomineService

    def index = {
        redirect(controller: "image")
    }
    def list = {
        response(abstractImageService.list())
    }

    def listByUser = {
        SecUser user = null
        if (params.id != null) user = User.read(params.id)
        else user = cytomineService.getCurrentUser()
        if (user != null) responseSuccess(abstractImageService.list(user, params.page, params.rows, params.sidx, params.sord, params.filename, params.createdstart, params.createdstop))
        else responseNotFound("User", params.id)
    }

    def listByGroup = {
        Group group = Group.read(params.idgroup)
        if (group) responseSuccess(abstractImageService.list(group))
        else responseNotFound("AbstractImageGroup", "Group", params.idgroup)
    }


    def show = {
        AbstractImage image = abstractImageService.read(params.long('id'))
        if (image) responseSuccess(image)
        else responseNotFound("Image", params.id)
    }


    def listByProject = {
        Project project = Project.read(params.id)
        if (project) responseSuccess(abstractImageService.list(project))
        else responseNotFound("Image", "Project", params.id)
    }

    def add = {
        add(abstractImageService, request.JSON)
    }

    def update = {
        update(abstractImageService, request.JSON)
    }

    def delete = {
        delete(abstractImageService, JSON.parse("{id : $params.id}"))
    }

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

    def imageProperties = {
        response(abstractImageService.imageProperties(params.long('id')))
    }

    def imageProperty = {
        def imageProperty = abstractImageService.imageProperty(params.long('id'))
        if (imageProperty) responseSuccess(imageProperty)
        else responseNotFound("ImageProperty", params.imageproperty)
    }

    def imageservers = {
        AbstractImage image = abstractImageService.read(params.long('id'))
        def urls = image.getImageServers().collect { it.getZoomifyUrl() + image.getPath() + "/" }
        def result = [:]
        result.imageServersURLs = urls
        response(result)
    }

    def thumb = {
        AbstractImage image = AbstractImage.read(params.long('id'))
        try {
            String thumbURL = image.getThumbURL()
            if (thumbURL == null) thumbURL = grailsApplication.config.grails.serverURL + "/images/cytomine.jpg"
            responseImage(thumbURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    def preview = {
        AbstractImage image = AbstractImage.read(params.long('id'))
        try {
            String previewURL = image.getPreviewURL()
            if (previewURL == null) previewURL = grailsApplication.config.grails.serverURL + "/images/cytomine.jpg"
            responseImage(previewURL)
        } catch (Exception e) {
            log.error("GetThumb:" + e)
        }
    }

    def cropUserAnnotation = {
        def annotation = UserAnnotation.read(params.id)
        def cropURL = cropAnnotation(annotation,params)
        responseImage(cropURL)
    }
    def cropAlgoAnnotation = {
        def annotation = AlgoAnnotation.read(params.id)
        def cropURL = cropAnnotation(annotation,params)
        responseImage(cropURL)
    }

    private def cropAnnotation(AnnotationDomain annotation, def params) {
        println "cropAnnotation:"+annotation
        Integer zoom = 0
        Integer maxSize = -1
        if (params.max_size != null) maxSize =  Integer.parseInt(params.max_size)
        if (params.zoom != null) zoom = Integer.parseInt(params.zoom)
        if (annotation == null)
            responseNotFound("Crop", "Annotation", params.id)
        else if ((params.zoom != null) && (zoom < annotation.getImage().getBaseImage().getZoomLevels().min || zoom > annotation.getImage().getBaseImage().getZoomLevels().max))
            responseNotFound("Crop", "Zoom", zoom)
        else {
            try {
                String cropURL = null
                if (maxSize != -1) {
                    cropURL = abstractImageService.cropWithMaxSize(annotation, maxSize)
                } else {
                    cropURL = abstractImageService.crop(annotation, zoom)
                }

                if (cropURL == null) cropURL = grailsApplication.config.grails.serverURL + "/images/cytomine.jpg"
                return cropURL
            } catch (Exception e) {
                e.printStackTrace()
                log.error("GetCrop:" + e)
            }
        }
    }


    def slidingWindow = {
        int width = params.width != null ? Integer.parseInt(params.width) : 1000
        int height = params.height != null ? Integer.parseInt(params.width) : 1000
        float overlapX = params.overlapX != null ? Float.parseFloat(params.overlapX) : 0
        float overlapY = params.overlapY != null ? Float.parseFloat(params.overlapY) : 0
        AbstractImage image = abstractImageService.read(params.long('id'))
        if (image) responseSuccess(abstractImageService.slidingWindow(image, [ width : width, height : height, overlapX : overlapX, overlapY : overlapY]))
    }

    def retrieval = {
        UserAnnotation annotation = UserAnnotation.read(params.idannotation)
        int zoom = (params.zoom != null) ? Integer.parseInt(params.zoom) : annotation.getImage().getZoomLevels().middle

        if (annotation == null)
            responseNotFound("Crop", "Annotation", params.id)
        else if (zoom < annotation.getImage().getBaseImage().getZoomLevels().min || zoom > annotation.getImage().getBaseImage().getZoomLevels().max)
            responseNotFound("Crop", "Zoom", zoom)
        else {

            int maxSimilarPictures = Integer.parseInt(params.maxsimilarpictures)
            responseSuccess(abstractImageService.retrieval(annotation, zoom, maxSimilarPictures))
        }
    }

}



