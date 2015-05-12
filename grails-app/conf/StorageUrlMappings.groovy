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
 * Date: 7/02/13
 * Time: 11:06
 */
class StorageUrlMappings {

    static mappings = {
        /* Storage */
        "/api/storage.$format"(controller: "restStorage"){
            action = [GET:"list", POST:"add"]
        }
        "/api/storage/$id.$format"(controller: "restStorage"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/storage/create/$user.$format"(controller : "restStorage") {
            action = [POST:"create"]
        }



        "/api/imageserver.$format"(controller : "restStorage") {
            action = [GET:"listByMime"]
        }
    }
}