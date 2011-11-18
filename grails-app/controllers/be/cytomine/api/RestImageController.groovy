package be.cytomine.api

import be.cytomine.image.AbstractImage
import be.cytomine.ontology.Annotation
import be.cytomine.security.User
import be.cytomine.project.Project
import be.cytomine.Exception.CytomineException

class RestImageController extends RestController {

    def springSecurityService
    def transactionService
    def imagePropertiesService
    def storageService
    def abstractImageService

    def index = {
        redirect(controller: "image")
    }
    def list = {
        response(abstractImageService.list())
    }

    def listByUser = {
        User user = null
        if (params.id != null)
            user = User.read(params.id)
        else user = getCurrentUser(springSecurityService.principal.id)
        if (user != null) responseSuccess(abstractImageService.list(user, params.page, params.rows, params.sidx, params.sord, params.filename, params.createdstart, params.createdstop))
        else responseNotFound("User", params.id)
    }

    def show = {
        AbstractImage image = abstractImageService.read(params.id)
        if (image) responseSuccess(image)
        else responseNotFound("Image", params.id)
    }


    def listByProject = {
        Project project = Project.read(params.id)
        if (project) responseSuccess(abstractImageService.list(project))
        else responseNotFound("Image", "Project", params.id)
    }

    def add = {
        try {
            def result = abstractImageService.addImage(request.JSON)
            responseOK(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def update = {
        try {
            def result = abstractImageService.updateImage(request.JSON)
            responseOK(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def delete = {
        try {
            def result = abstractImageService.deleteImage(params.id)
            responseOK(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def metadata = {
        def url = abstractImageService.metadata(params.id)

        withFormat {
            json {
                render(contentType: "application/json", text: "${url.text}")
            }
        }
    }

    def imageProperties = {
        response(abstractImageService.imageProperties(params.id))
    }

    def imageProperty = {
        def imageProperty = abstractImageService.imageProperty(params.imageproperty)
        if (imageProperty) responseSuccess(imageProperty)
        else responseNotFound("ImageProperty", params.imageproperty)
    }


    def imageservers = {
        AbstractImage image = AbstractImage.read(params.id)
        def urls = image.getImageServers().collect { it.getZoomifyUrl() + image.getPath() + "/" }
        def result = [:]
        result.imageServersURLs = urls
        response(result)
    }

    def thumb = {
        AbstractImage image = AbstractImage.read(params.id)
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
                responseImage(abstractImageService.crop(annotation,zoom))
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
            responseSuccess(abstractImageService.retrieval(annotation,zoom,maxSimilarPictures))
        }
    }
}



