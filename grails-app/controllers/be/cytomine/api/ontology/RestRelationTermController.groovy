package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Term
import grails.converters.JSON

class RestRelationTermController extends RestController {

    def relationService
    def termService
    def relationTermService

    def list = {
        responseSuccess(relationTermService.list())
    }

    def listByRelation = {
        Relation relation
        if (params.id != null) relation = relationService.read(params.long('id'))
        else relation = relationService.getRelationParent()
        if (relation) responseSuccess(relationTermService.list(relation))
        else responseNotFound("RelationTerm", "Relation", params.id)
    }

    def listByTerm = {
        Term term = termService.read(params.long('id'))
        String position = params.i

        if (term && (position == "1" || position == "2"))
            responseSuccess(relationTermService.list(term, position))
        else responseNotFound("RelationTerm", "Term" + position, params.id)
    }

    def listByTermAll = {
        Term term = termService.read(params.long('id'))
        if (term) responseSuccess(relationTermService.list(term))
        else responseNotFound("RelationTerm", "Term", params.id)
    }

    def show = {
        Relation relation
        if (params.idrelation != null) relation = relationService.read(params.long('idrelation'))
        else relation = relationService.getRelationParent()

        Term term1 = termService.read(params.long('idterm1'))
        Term term2 = termService.read(params.long('idterm2'))

        RelationTerm relationTerm = relationTermService.get(relation, term1, term2)

        if (relation && term1 && term2 && relationTerm) responseSuccess(relationTerm)
        else responseNotFound("RelationTerm", "Relation", relation, "Term1", term1, "Term2", term2)

    }

    def add = {
        def json = request.JSON
        Relation relation
        if (json.relation != null)
            relation = relationService.read(params.long('id'))
        else
            relation = relationService.getRelationParent()

        json.relation = relation ? relation.id : -1

        add(relationTermService, json)
    }

    def delete = {

        Relation relation
        if (params.idrelation != null)
            relation = relationService.read(params.long('idrelation'))
        else
            relation = relationService.getRelationParent()

        def json = JSON.parse("{relation: ${relation ? relation.id : -1}, term1: $params.idterm1, term2: $params.idterm2}")
        delete(relationTermService, json)
    }
}
