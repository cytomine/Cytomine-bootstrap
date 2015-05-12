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
class RelationTermUrlMappings {

    static mappings = {
        "/api/relation/$id/term.$format"(controller:"restRelationTerm"){
            action = [POST:"add"]
        }
        "/api/relation/$idrelation/term1/$idterm1/term2/$idterm2.$format"(controller:"restRelationTerm"){
            action = [GET: "show",DELETE:"delete"]
        }
        "/api/relation/parent/term.$format"(controller:"restRelationTerm"){
            action = [POST:"add"]
        }
        "/api/relation/parent/term1/$idterm1/term2/$idterm2.$format"(controller:"restRelationTerm"){
            action = [GET: "show",DELETE:"delete"]
        }
        "/api/relation/term/$id.$format"(controller:"restRelationTerm"){
            action = [GET: "listByTermAll"]
        }
        //i = 1 or 2 (term 1 or term 2), id = id term
        "/api/relation/term/$i/$id.$format"(controller:"restRelationTerm"){
            action = [GET: "listByTerm"]
        }
    }
}

