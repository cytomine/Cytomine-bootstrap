package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.security.SecUser
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
        Term term = termService.read(params.long('id'));
        if (term) responseSuccess(ontologyService.listByTerm(term))
        else responseNotFound("Ontology", "Term", params.id)
    }

    def listByUserLight = {
        SecUser user = cytomineService.getCurrentUser()
        if (user) responseSuccess(ontologyService.listByUserLight(user))
        else responseNotFound("User", springSecurityService.principal.id)
    }
    def listByUser = {
        SecUser user = cytomineService.getCurrentUser()
        if (user) responseSuccess(ontologyService.listByUser(user))
        else responseNotFound("User", springSecurityService.principal.id)
    }

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
