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
class ProjectUrlMappings {


    static mappings = {
        "/api/project.$format"(controller: "restProject"){  //?.$format
            action = [GET:"list", POST:"add"]
        }
        "/api/project/$id.$format"(controller: "restProject"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
        "/api/software/$id/project.$format"(controller:"restProject"){
            action = [GET: "listBySoftware"]
        }
        "/api/user/$id/project.$format"(controller:"restProject"){
            action = [GET:"listByUser"]
        }
        "/api/project/$id/last/$max.$format"(controller:"restProject"){
            action = [GET:"lastAction"]
        }
        "/api/ontology/$id/project.$format"(controller:"restProject"){
            action = [GET:"listByOntology"]
        }

        "/api/retrieval/$id/project.$format"(controller:"restProject"){
            action = [GET:"listRetrieval"]
        }

        "/api/user/$id/project/light.$format"(controller:"restProject"){
            action = [GET:"listLightByUser"]
        }

        "/api/project/method/lastopened.$format" (controller: "restProject") {
            action = [GET:"listLastOpened"]
        }

        "/api/project/$id/invitation.$format" (controller: "restProject") {
            action = [POST:"inviteNewUser"]
        }
    }
}
