package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON

/**
 * Controller for term request (word in ontology)
 */


class RestTermController extends RestController {

    def termService

    /**
     * List all term available
     * @return All term available for the current user
     */
    def list = {
        responseSuccess(termService.list())
    }

    /**
     * Get a specific term
     *
     * @param  id The term id
     * @return A Term
     */
      def show() {
        Term term = termService.read(params.long('id'))
        if (term) {
            responseSuccess(term)
        } else {
            responseNotFound("Term", params.id)
        }
      }

    /**
     * Get terms from an ontology
     * @param idontology Ontology filter
     * @return List of term
     */
    def listByOntology = {
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
    def listAllByProject = {
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
    def statProject = {
        Term term = Term.read(params.id)
        if (term) responseSuccess(termService.statProject(term))
        else responseNotFound("Project", params.id)
    }

    /**
     * Add a new term
     * Use next add relation-term to add relation with another term
     * @param data JSON with Term data
     * @return Response map with .code = http response code and .data.term = new created Term
     */
    def add = {
        add(termService, request.JSON)
    }

    /**
     * Update a term
     * @param id Term id
     * @param data JSON with the new Term data
     * @return Response map with .code = http response code and .data.newTerm = new created Term and  .data.oldTerm = old term value
     */
    def update = {
        update(termService, request.JSON)
    }

    /**
     * Delete a term
     * @param id Term id
     * @return Response map with .code = http response code and .data.term = deleted term value
     */
    def delete = {
        delete(termService, JSON.parse("{id : $params.id}"),null)
    }

}
