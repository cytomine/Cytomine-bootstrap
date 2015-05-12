package be.cytomine.api.processing

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.api.RestController
import be.cytomine.processing.ImageFilter
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for image filter, filter that can be apply to a picture
 */
@RestApi(name = "image filter services", description = "Methods for managing image filter, filter that can be apply to a picture")
class RestImageFilterController extends RestController {

    def imageFilterService
    def projectService

    /**
     * List all image filter
     */
    @RestApiMethod(description="List all image filter", listing = true)
    def list() {
        responseSuccess(imageFilterService.list())
    }

    /**
     * Get an image filter
     */
    @RestApiMethod(description="Get an image filter")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The image filter id")
    ])
    def show() {
        ImageFilter imageFilter = imageFilterService.read(params.long('id'))
        if (imageFilter) {
            responseSuccess(imageFilter)
        } else {
            responseNotFound("ImageFilter", params.id)
        }
    }


}
