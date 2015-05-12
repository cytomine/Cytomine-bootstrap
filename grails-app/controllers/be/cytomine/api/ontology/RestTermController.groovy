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
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType

/**
 * Controller for term request (word in ontology)
 */

@RestApi(name = "term services", description = "Methods for managing terms")
class RestTermController extends RestController {

    def termService

    /**
     * List all term available
     * @return All term available for the current user
     */
    @RestApiMethod(description="List all term available", listing = true)
    def list () {
        responseSuccess(termService.list())
    }

    /**
     * Get a specific term
     *
     * @param  id The term id
     * @return A Term
     */
    @RestApiMethod(description="Get a term")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The term id")
    ])
    def show() {
        Term term = termService.read(params.long('id'))
        if (term) {
            responseSuccess(term)
        } else {
            responseNotFound("Term", params.id)
        }
    }

    /**
     * Add a new term
     * Use next add relation-term to add relation with another term
     * @param data JSON with Term data
     * @return Response map with .code = http response code and .data.term = new created Term
     */
    @RestApiMethod(description="Add a term in an ontology")
    def add () {
        add(termService, request.JSON)
    }

    /**
     * Update a term
     * @param id Term id
     * @param data JSON with the new Term data
     * @return Response map with .code = http response code and .data.newTerm = new created Term and  .data.oldTerm = old term value
     */
    @RestApiMethod(description="Update a term")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The term id")
    ])
    def update () {
        update(termService, request.JSON)
    }

    /**
     * Delete a term
     * @param id Term id
     * @return Response map with .code = http response code and .data.term = deleted term value
     */
    @RestApiMethod(description="Delete a term")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The term id")
    ])
    def delete () {
        delete(termService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Get terms from an ontology
     * @param idontology Ontology filter
     * @return List of term
     */
    @RestApiMethod(description="Get all term from an ontology", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The ontology id")
    ])
    def listByOntology() {
        Ontology ontology = Ontology.read(params.idontology)
        if (ontology) {
            responseSuccess(termService.list(ontology))
        } else {
            responseNotFound("Term", "Ontology", params.idontology)
        }
    }

    /**
     * Get all project terms
     * @param idProject Project filter
     * @return List of term
     */
    @RestApiMethod(description="Get all term for a project", listing=true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH,description = "The project id")
    ])
    def listAllByProject() {
        Project project = Project.read(params.idProject)
        if (project && project.ontology) {
            responseSuccess(termService.list(project))
        }
        else {
            responseNotFound("Term", "Project", params.idProject)
        }
    }

    /**
     * Get the stats info for a term
     * @Param id Term id
     * @return For each project with this Term, get a the term count
     */
    //TODO:APIDOC
    def statProject() {
        Term term = Term.read(params.id)
        if (term) responseSuccess(termService.statProject(term))
        else responseNotFound("Project", params.id)
    }

}
