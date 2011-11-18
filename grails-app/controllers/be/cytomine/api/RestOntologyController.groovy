package be.cytomine.api

import be.cytomine.ontology.Term
import be.cytomine.ontology.Ontology
import be.cytomine.security.User
import be.cytomine.Exception.CytomineException


class RestOntologyController extends RestController {

    def springSecurityService
    def transactionService
    def ontologyService

    def list = {
        responseSuccess(ontologyService.list())
    }

    def listLight = {
        responseSuccess(ontologyService.listLight())
    }

    def listByTerm = {
        Term term = Term.read(params.id);
        if (term) responseSuccess(ontologyService.listByTerm(term))
        else responseNotFound("Ontology", "Term", params.id)
    }

    def listByUserLight = {
        User user = getCurrentUser(springSecurityService.principal.id)
        if (user) responseSuccess(ontologyService.listByUserLight(user))
        else responseNotFound("User", springSecurityService.principal.id)
    }
    def listByUser = {
        User user = getCurrentUser(springSecurityService.principal.id)
        if (user) responseSuccess(ontologyService.listByUserLight(user))
        else responseNotFound("User", springSecurityService.principal.id)
    }

    def show = {
        Ontology ontology = ontologyService.read(params.id)
        if (ontology) responseSuccess(ontology)
        else responseNotFound("Ontology", params.id)
    }


    def add = {
        try {
            def result = ontologyService.addOntology(request.JSON)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def update = {
        try {
            def result = ontologyService.updateOntology(request.JSON)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }

    def delete = {
        try {
            def result = ontologyService.deleteOntology(params.id)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }
}
