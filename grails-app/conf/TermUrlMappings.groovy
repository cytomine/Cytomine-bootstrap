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
class TermUrlMappings {

    static mappings = {
        /* Term */
        "/api/term.$format"(controller:"restTerm"){
            action = [GET: "list",POST:"add"]
        }
        "/api/term/$id.$format"(controller:"restTerm"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/project/$idProject/term.$format"(controller:"restTerm"){
            action = [GET:"listAllByProject"]
        }
        "/api/term/$id/project/stat.$format"(controller:"restTerm"){
            action = [GET:"statProject"]
        }
        "/api/ontology/$idontology/term.$format"(controller:"restTerm"){
            action = [GET:"listByOntology"]
        }
        "/api/ontology/$idontology/term.$format"(controller:"restTerm"){
            action = [GET:"listAllByOntology"]
        }
        "/api/userannotation/$idannotation/user/$idUser/term.$format"(controller:"restAnnotationTerm"){
            action = [GET: "listTermByAnnotation"]
        }
    }
}

