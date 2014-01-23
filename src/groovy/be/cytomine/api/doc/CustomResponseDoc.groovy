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
        @ApiObjectFieldLight(apiFieldName = "admin", description = "User is admin or not",allowedType = "boolean",useForCreation = false),
    ])
    static def project_sharing_same_image
}