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
 * Time: 13:52
 */
class AnnotationTermUrlMappings {

    static mappings = {
        "/api/annotation/$idannotation/term.$format"(controller:"restAnnotationTerm"){
            action = [GET: "listTermByAnnotation"]
        }
        "/api/annotation/$idannotation/term/$idterm.$format"(controller:"restAnnotationTerm"){
            action = [POST:"add",DELETE:"delete", GET:"show"]
        }
        "/api/annotation/$idannotation/term/$idterm/clearBefore.$format"(controller:"restAnnotationTerm"){
            action = [POST:"addWithDeletingOldTerm"]
        }
        "/api/annotation/$idannotation/term/$idterm/user/$idUser.$format"(controller:"restAnnotationTerm"){
            action = [GET:"show",DELETE:"delete"]
        }
        "/api/annotation/$idannotation/notuser/$idNotUser/term.$format"(controller:"restAnnotationTerm"){
            action = [GET: "listAnnotationTermByUserNot"]
        }
    }
}


