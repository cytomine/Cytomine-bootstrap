package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApi

import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

@RestApi(name = "annotation index service", description = "Methods for managing annotation index. Its auto index that store entries <image,user,nbreAnnotation,nbreReviewed")
class RestAnnotationIndexController extends RestController {

    def annotationIndexService
    def imageInstanceService

    @RestApiMethod(description="Get all index entries for an image", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The image id")
    ])
    def listByImage() {
        ImageInstance image = imageInstanceService.read(params.long('id'))
        if (image) {
            responseSuccess(annotationIndexService.list(image))
        }
        else {
            responseNotFound("Project", params.id)
        }
    }
}
