package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.security.SecUser
import grails.converters.JSON

/**
 * Controller for ontology (terms tree)
 */
class RestOntologyController extends RestController {

    def springSecurityService
    def ontologyService
    def cytomineService
    def termService

    /**
     * List all ontology visible for the current user
     * For each ontology, print the terms tree
     */
    def list = {
        responseSuccess(ontologyService.list())
    }

    /**
     * List all ontology visible for the current user
     * List only id and name
     */
    def listLight = {
        responseSuccess(ontologyService.listLight())
    }

    /**
     *
     */
    def show = {
        Ontology ontology = ontologyService.read(params.long('id'))
        if (ontology) responseSuccess(ontology)
        else responseNotFound("Ontology", params.id)
    }

    def add = {
        add(ontologyService, request.JSON)
    }

    def update = {
        update(ontologyService, request.JSON)
    }

    def delete = {
        delete(ontologyService, JSON.parse("{id : $params.id}"))
    }
}
