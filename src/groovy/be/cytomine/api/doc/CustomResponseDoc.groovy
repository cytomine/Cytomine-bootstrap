package be.cytomine.api.doc

import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.annotation.ApiObjectField


class CustomResponseDoc {

    @ApiObjectFieldLight(description = "Response for sequence possibilities")
    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "slice", description = "Image slice index",allowedType = "list",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "zStack", description = "Image zstack index",allowedType = "list",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "time", description = "Image time index",allowedType = "list",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "channel", description = "Image channel index",allowedType = "list",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "s", description = "Range of possible slice index for image group",allowedType = "list",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "z", description = "Range of possible zstack index for image group",allowedType = "list",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "t", description = "Range of possible time index for image group",allowedType = "list",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "c", description = "Range of possible channel index for image group",allowedType = "list",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "imageGroup", description = "Image group id",allowedType = "list",useForCreation = false)
     ])
    static def sequence_possibilties

    //If true, send an array with item {imageinstanceId,layerId,layerName,projectId, projectName, admin}
    @ApiObjectFieldLight(description = "Response for project sharing the same image (list)")
    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "imageinstanceId", description = "Image id",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "layerId", description = "User id",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "layerName", description = "User name",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "projectId", description = "Project id",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "projectName", description = "Project name",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "admin", description = "User is admin or not",allowedType = "boolean",useForCreation = false)
    ])
    static def project_sharing_same_image


    //If true, send an array with item {imageinstanceId,layerId,layerName,projectId, projectName, admin}
    @ApiObjectFieldLight(description = "Response for annotation search")
    @ApiObjectFieldsLight(params=[
        @ApiObjectFieldLight(apiFieldName = "id", description = "Annotation id",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "class", description = "Annotation class name",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "countReviewedAnnotations", description = "(If params showMeta=true and reviewed=false) the number of reviewed annotation from this annotation",allowedType = "int",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "reviewed", description = "(If params showMeta=true) annotation is reviewed",allowedType = "int",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "image", description = "(If params showMeta=true), image annotation id)",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "project", description = "(If params showMeta=true) project annotation id",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "container", description = "(If params showMeta=true) project annotation id",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "created", description = "(If params showMeta=true) annotation create date",allowedType = "date",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "updated", description = "(If params showMeta=true) annotation update date",allowedType = "date",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "user", description = "(If params showMeta=true) user id that create annotation (if reveiwed annotation, user that create the annotation that has been validated)",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "countComments", description = "(If params showMeta=true) number of comments on this annotation",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "reviewUser", description = "(If params showMeta=true, only for reviewed annotation) the user id that review",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "geometryCompression", description = "(If params showMeta=true) Geometry compression rate used to simplify",allowedType = "double",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "cropURL", description = "(If params showMeta=true) URL to get the crop annotation (image view that frame the annotation)",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "smallCropURL", description = "(If params showMeta=true)  URL to get the small crop annotation (image view that frame the annotation)",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "url", description = "(If params showMeta=true) URL to go to the annotation on the webapp",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "imageURL", description = "(If params showMeta=true) URL to go to the image on the webapp",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "parentIdent", description = "(If params showMeta=true, only for reviewed) the annotation parent of the reviewed annotation",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "wkt", description = "(If params showWKT=true) the full polygon form in WKT",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "area", description = "(If params showGis=true) the area size of the annotation",allowedType = "double",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "areaUnit", description = "(If params showGis=true) the area unit (pixels²=1,micron²=3)",allowedType = "int",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "perimeter", description = "(If params showGis=true) the perimeter size of the annotation",allowedType = "double",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "perimeterUnit", description = "(If params showGis=true) the perimeter unit (pixels=0,mm=2,)",allowedType = "double",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "x", description = "(If params showGis=true) the annotation centroid x",allowedType = "double",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "y", description = "(If params showGis=true) the annotation centroid y",allowedType = "double",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "reviewUser", description = "(If params showGis=true) the user id thatreview",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "reviewUser", description = "(If params showGis=true) the user id thatreview",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "reviewUser", description = "(If params showGis=true) the user id thatreview",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "term", description = "(If params showTerm=true) the term list id",allowedType = "list",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "annotationTerms", description = "(If params showTerm=true) the annotationterms list id",allowedType = "list",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "userTerm", description = "(If params showTerm=true) the user id group by term id",allowedType = "map",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "rate", description = "(If params showTerm=true) the reliability of the prediction",allowedType = "double",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "originalfilename", description = "(If params showImage=true) the image filename",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "idTerm", description = "(If params showAlgo=true) the predicted term for the annotation",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "idExpectedTerm", description = "(If params showAlgo=true) the expected term (real term add by user previously)",allowedType = "long",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "creator", description = "(If params showUser=true) the username of the creator",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "lastname", description = "(If params showUser=true) the lastname of the creator",allowedType = "string",useForCreation = false),
        @ApiObjectFieldLight(apiFieldName = "firstname", description = "(If params showUser=true) the firstname of the creator",allowedType = "string",useForCreation = false)
    ])
    static def annotation_listing
}