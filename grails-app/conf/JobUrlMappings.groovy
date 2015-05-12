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

class JobUrlMappings {

    static mappings = {
        /* Job */
        "/api/job.$fomat"(controller:"restJob"){
            action = [GET: "list",POST:"add"]
        }
        "/api/job/$id.$format"(controller:"restJob"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/job/$id/alldata.$format"(controller:"restJob") {
            action = [DELETE: "deleteAllJobData", GET: "listAllJobData"]
        }
        "/api/job/$id/execute.$format" (controller : "restJob") {
            action = [POST : "execute"]
        }
        "/api/job/$id/preview_roi.$format" (controller : "restJob") {
            action = [GET : "getPreviewRoi"]
        }
        "/api/job/$id/preview.$format" (controller : "restJob") {
            action = [POST : "preview", GET : "getPreview"]
        }

        "/api/project/$id/job/purge.$format"(controller : "restJob") {
            action = [POST : "purgeJobNotReviewed", GET : "purgeJobNotReviewed"]
        }


        /* Job template */
        "/api/jobtemplate.$fomat"(controller:"restJobTemplate"){
            action = [POST:"add"]
        }
        "/api/jobtemplate/$id.$format"(controller:"restJobTemplate"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/project/$project/jobtemplate.$fomat"(controller:"restJobTemplate"){
            action = [GET: "list"]
        }

        /* Job template annotation */
        "/api/jobtemplateannotation.$fomat"(controller:"restJobTemplateAnnotation"){
            action = [POST:"add",GET: "list"]
        }
        "/api/jobtemplateannotation/$id.$format"(controller:"restJobTemplateAnnotation"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
    }
}
