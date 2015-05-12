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
class AttachedFileUrlMappings {

    static mappings = {

        "/api/attachedfile.$format"(controller: "restAttachedFile") {
            action = [GET:"list", POST:"upload",PUT:"upload"]
        }

        "/api/domain/$domainClassName/$domainIdent/attachedfile.$format"(controller: "restAttachedFile") {
            action = [GET:"listByDomain"]
        }

        "/api/attachedfile/$id.$format"(controller: "restAttachedFile") {
            action = [GET:"show"]
        }

        "/api/attachedfile/$id/download"(controller: "restAttachedFile") {
            action = [GET:"download"]
        }


        // FOR JS
        "/api/attachedfileCKEditor.$format"(controller: "restAttachedFile") {
            action = [POST:"uploadFromCKEditor"]
        }
    }
}
