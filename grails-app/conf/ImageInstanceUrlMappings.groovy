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

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ImageInstanceUrlMappings {

    static mappings = {
        /* Image Instance */
        "/api/imageinstance.$format"(controller: "restImageInstance"){
            action = [POST:"add"]
        }
        "/api/imageinstance/$id.$format"(controller: "restImageInstance"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/imageinstance/$id/next.$format"(controller: "restImageInstance"){
            action = [GET:"next"]
        }
        "/api/user/$user/imageinstance/light.$format"(controller: "restImageInstance"){
            action = [GET:"listByUser"]
        }
        "/api/imageinstance/$id/previous.$format"(controller: "restImageInstance"){
            action = [GET:"previous"]
        }
        "/api/imageinstance/$id/window-$x-$y-$w-$h.$format"(controller: "restImageInstance"){
            action = [GET:"window"]
        }
        "/api/imageinstance/$id/window_url-$x-$y-$w-$h.$format"(controller: "restImageInstance"){
            action = [GET:"windowUrl"]
        }
        "/api/imageinstance/$id/download"(controller: "restImageInstance"){
            action = [GET:"download"]
        }
        "/api/imageinstance/$id/associated.$format"(controller: "restImageInstance"){
            action = [GET:"associated"]
        }
        "/api/imageinstance/$id/associated/$label.$format"(controller: "restImageInstance"){
            action = [GET:"label"]
        }
//        "/api/imageinstance/$id/imageproperty.$format"(controller: "restImageInstance"){
//            action = [GET:"imageProperties"]
//        }
        "/api/project/$id/imageinstance.$format"(controller: "restImageInstance"){
            action = [GET:"listByProject"]
        }
        "/api/imageinstance/$id/cropgeometry.$format"(controller :"restImageInstance") {
            action = [GET:"cropGeometry"]
        }
        "/api/imageinstance/$id/crop.$format"(controller :"restImageInstance") {
            action = [GET:"crop"]
        }
        "/api/imageinstance/method/lastopened.$format"(controller :"restImageInstance") {
            action = [GET:"listLastOpenImage"]
        }
        "/api/imageinstance/$id/sameimagedata.$format"(controller :"restImageInstance") {
            action = [GET:"retrieveSameImageOtherProject"]
        }
        "/api/imageinstance/$id/copyimagedata.$format"(controller :"restImageInstance") {
            action = [POST:"copyAnnotationFromSameAbstractImage"]
        }

        "/api/imageinstance/$id/copymetadata.$format"(controller :"restImageInstance") {
            action = [POST:"copyMetadata"]
        }

        "/api/imageinstance/$idImage/nested.$format"(controller: "restNestedImageInstance"){
            action = [POST:"add", GET : "listByImageInstance"]
        }
        "/api/imageinstance/$idImage/nested/$id.$format"(controller: "restNestedImageInstance"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
    }
}
