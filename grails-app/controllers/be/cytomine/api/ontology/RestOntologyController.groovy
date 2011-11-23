package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.security.User
import grails.converters.JSON

class RestOntologyController extends RestController {

    def springSecurityService
    def ontologyService
    def cytomineService
    def termService

    def list = {
        responseSuccess(ontologyService.list())
    }

    def listLight = {
        responseSuccess(ontologyService.listLight())
    }

    def listByTerm = {
        Term term = termService.read(params.id);
        if (term) responseSuccess(ontologyService.listByTerm(term))
        else responseNotFound("Ontology", "Term", params.id)
    }

    def listByUserLight = {
        User user = cytomineService.getCurrentUser()
        if (user) responseSuccess(ontologyService.listByUserLight(user))
        else responseNotFound("User", springSecurityService.principal.id)
    }
    def listByUser = {
        User user = cytomineService.getCurrentUser()
        if (user) responseSuccess(ontologyService.listByUser(user))
        else responseNotFound("User", springSecurityService.principal.id)
    }

    def show = {
        Ontology ontology = ontologyService.read(params.id)
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
