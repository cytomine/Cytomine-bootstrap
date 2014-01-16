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


}