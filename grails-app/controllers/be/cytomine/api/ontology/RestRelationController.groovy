package be.cytomine.api.ontology

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
import be.cytomine.ontology.Relation
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for relation between terms (parent, synonym,...)
 * We only use "parent" now, but we could later implement CRUD to support new type of relation
 */
@RestApi(name = "relation services", description = "Methods for managing relations")
class RestRelationController extends RestController {


    def springSecurityService
    def relationService

    /**
     * List all relation available
     */
    @RestApiMethod(description="List all relation available", listing = true)
    def list () {
        responseSuccess(relationService.list())
    }

    /**
     * Get a single relation with its id
     */
    @RestApiMethod(description="Get a relation")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The relation id")
    ])
    def show () {
        Relation relation = relationService.read(params.long('id'))
        if (relation) {
            responseSuccess(relation)
        } else {
            responseNotFound("Relation", params.id)
        }
    }
}
