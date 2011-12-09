package be.cytomine.api.image

import be.cytomine.api.RestController
import be.cytomine.image.AbstractImage
import be.cytomine.ontology.Annotation
import be.cytomine.project.Project
import be.cytomine.security.Group
import be.cytomine.security.User
import grails.converters.JSON

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
        User user = null
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
        def url = abstractImageService.metadata(params.long('id'))

        withFormat {
            json {
                render(contentType: "application/json", text: "${url.text}")
            }
        }
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
            responseImage(image.getThumbURL())
        } catch (Exception e) {
            log.error("GetThumb:" + e);
        }
    }

    def crop = {
        Annotation annotation = Annotation.read(params.id)
        def zoom
        if (params.zoom != null) zoom = Integer.parseInt(params.zoom)
        if (annotation == null)
            responseNotFound("Crop", "Annotation", params.id)
        else if ((params.zoom != null) && (zoom < annotation.getImage().getBaseImage().getZoomLevels().min || zoom > annotation.getImage().getBaseImage().getZoomLevels().max))
            responseNotFound("Crop", "Zoom", zoom)
        else {
            try {
                responseImage(abstractImageService.crop(annotation, zoom))
            } catch (Exception e) {
                log.error("GetThumb:" + e);
            }
        }
    }

    def retrieval = {
        Annotation annotation = Annotation.read(params.idannotation)
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



