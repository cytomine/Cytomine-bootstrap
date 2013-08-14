package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance

class RestAnnotationIndexController extends RestController {

    def annotationIndexService
    def imageInstanceService

    def listByImage = {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(annotationIndexService.list(image))
        }
        else {
            responseNotFound("Project", params.id)
        }
    }
}
