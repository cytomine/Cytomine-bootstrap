package be.cytomine.api

import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Term
import be.cytomine.Exception.CytomineException

class RestRelationTermController extends RestController {

    def springSecurityService
    def relationService
    def termService
    def relationTermService
    def transactionService

    def list = {
        responseSuccess(relationTermService.list())
    }

    def listByRelation = {
        Relation relation
        if (params.id != null) relation = relationService.read(params.id)
        else relation = relationService.getRelationParent()
        if (relation) responseSuccess(relationTermService.list(relation))
        else responseNotFound("RelationTerm", "Relation", params.id)
    }

    def listByTerm = {
        Term term = termService.read(params.id)
        String position = params.i

        if (term && (position == "1" || position == "2"))
            responseSuccess(relationTermService.list(term, position))
        else responseNotFound("RelationTerm", "Term" + position, params.id)
    }

    def listByTermAll = {
        Term term = Term.read(params.id)
        if (term) responseSuccess(relationTermService.list(term))
        else responseNotFound("RelationTerm", "Term", params.id)
    }

    def show = {
        Relation relation
        if (params.idrelation != null) relation = relationService.read(params.idrelation)
        else relation = relationService.getRelationParent()

        Term term1 = termService.read(params.idterm1)
        Term term2 = termService.read(params.idterm2)

        RelationTerm relationTerm = relationTermService.get(relation, term1, term2)

        if (relation && term1 && term2 && relationTerm) responseSuccess(relationTerm)
        else responseNotFound("RelationTerm", "Relation", relation, "Term1", term1, "Term2", term2)

    }

    def add = {
        try {
            def json = request.JSON
            Relation relation
            if (json.relation != null)
                relation = relationService.read(params.id)
            else
                relation = relationService.getRelationParent()

            json.relation = relation ? relation.id : -1

            def result = relationTermService.add(json)
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
            Relation relation
            if (params.idrelation != null)
                relation = relationService.read(params.idrelation)
            else
                relation = relationService.getRelationParent()
            def result = relationTermService.delete(relation, params.idterm1, params.idterm2)
            responseResult(result)
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.message], e.code)
        } finally {
            transactionService.stopIfTransactionInProgress()
        }
    }
}
