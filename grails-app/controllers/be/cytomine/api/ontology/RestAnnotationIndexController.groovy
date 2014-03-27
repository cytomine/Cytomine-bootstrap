package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiParamLight
import org.jsondoc.core.annotation.Api

import jsondoc.annotation.ApiParamsLight
import org.jsondoc.core.pojo.ApiParamType

@Api(name = "annotation index service", description = "Methods for managing annotation index. Its auto index that store entries <image,user,nbreAnnotation,nbreReviewed")
class RestAnnotationIndexController extends RestController {

    def annotationIndexService
    def imageInstanceService

    @ApiMethodLight(description="Get all index entries for an image", listing=true)
    @ApiParamsLight(params=[
        @ApiParamLight(name="id", type="long", paramType = ApiParamType.PATH,description = "The image id")
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
