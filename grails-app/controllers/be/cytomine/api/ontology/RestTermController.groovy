package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import grails.converters.JSON

/**
 * Controller for term request (word in ontology)
 */
class RestTermController extends RestController {

    def termService

    /**
     * List all term available
     */
    def list = {
        responseSuccess(termService.list())
    }

    /**
     * Get a single term
     */
    def show = {
        Term term = termService.read(params.long('id'))
        if (term) {
            responseSuccess(term)
        } else {
            responseNotFound("Term", params.id)
        }
    }

    /**
     * Get all term in the ontology
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
     * Get all term for the project ontology
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
     */
    def statProject = {
        Term term = Term.read(params.id)
        if (term) responseSuccess(termService.statProject(term))
        else responseNotFound("Project", params.id)
    }

    /**
     * Add a new term
     * Use next add relation-term to add relation with another term
     */
    def add = {
        add(termService, request.JSON)
    }

    /**
     * Update a term
     */
    def update = {
        update(termService, request.JSON)
    }

    /**
     * Delete a term
     */
    def delete = {
        delete(termService, JSON.parse("{id : $params.id}"))
    }

}
