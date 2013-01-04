package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Term
import grails.converters.JSON

/**
 * Controller for relation between terms in ontology (ex: parent)
 */
class RestRelationTermController extends RestController {

    def relationService
    def termService
    def relationTermService

    /**
     * List all full relation (term 1, term 2 and relation)
     */
    def list = {
        responseSuccess(relationTermService.list())
    }

    /**
     * List all relation terms filter by relation
     */
    def listByRelation = {
        Relation relation = relationService.read(params.long('id'))
        if(!relation) {
            relation = relationService.getRelationParent()
        }
        responseSuccess(relationTermService.list(relation))
    }

    /**
     * List all relation for a specific term and position
     */
    def listByTerm = {
        Term term = termService.read(params.long('id'))
        String position = params.i

        if (term && (position == "1" || position == "2")){
            responseSuccess(relationTermService.list(term, position))
        } else {
            responseNotFound("RelationTerm", "Term" + position, params.id)
        }
    }

    /**
     * List all relation for a specific term
     */
    def listByTermAll = {
        Term term = termService.read(params.long('id'))
        if (term) {
            responseSuccess(relationTermService.list(term))
        }
        else {
            responseNotFound("RelationTerm", "Term", params.id)
        }
    }

    /**
     * Check if a relation exist with term1 and term2
     */
    def show = {
        Relation relation
        if (params.idrelation != null) {
            relation = relationService.read(params.long('idrelation'))
        } else {
            relation = relationService.getRelationParent()
        }

        Term term1 = termService.read(params.long('idterm1'))
        Term term2 = termService.read(params.long('idterm2'))

        RelationTerm relationTerm = relationTermService.get(relation, term1, term2)

        if (relation && term1 && term2 && relationTerm) {
            responseSuccess(relationTerm)
        }
        else {
            responseNotFound("RelationTerm", "Relation", relation, "Term1", term1, "Term2", term2)
        }

    }

    /**
     * Add a new relation with two terms
     */
    def add = {
        def json = request.JSON
        Relation relation
        if (json.relation != null && json.relation.toString()!="null") {
            String strRel = json.relation
            relation = relationService.read(Long.parseLong(strRel))
        } else {
            relation = relationService.getRelationParent()
        }

        json.relation = relation ? relation.id : -1

        add(relationTermService, json)
    }

    /**
     * Delete a relation between two terms
     */
    def delete = {

        Relation relation
        if (params.idrelation != null && params.idrelation!="null")
            relation = relationService.read(params.long('idrelation'))
        else
            relation = relationService.getRelationParent()

        def json = JSON.parse("{relation: ${relation ? relation.id : -1}, term1: $params.idterm1, term2: $params.idterm2}")
        delete(relationTermService, json)
    }
}
