package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Relation
import be.cytomine.ontology.Term
import grails.converters.JSON
import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiParam
import org.jsondoc.core.annotation.ApiParams
import org.jsondoc.core.pojo.ApiParamType

/**
 * Controller for relation between terms in ontology (ex: parent)
 */
@Api(name = "relation term services", description = "Methods for managing relation between terms in ontology (ex: t1 parent t2)")
class RestRelationTermController extends RestController {

    def relationService
    def termService
    def relationTermService


    /**
     * List all relation for a specific term and position
     */
    @ApiMethodLight(description="List all relation for a specific term and position (1 or 2)", listing=true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The term id"),
        @ApiParam(name="i", type="int", paramType = ApiParamType.PATH,description = "The position index (1 or 2)")
    ])
    def listByTerm() {
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
    @ApiMethodLight(description="List all relation for a specific term", listing=true)
    @ApiParams(params=[
        @ApiParam(name="id", type="long", paramType = ApiParamType.PATH,description = "The term id"),
        @ApiParam(name="i", type="int", paramType = ApiParamType.PATH,description = "The position index (1 or 2)")
    ])
    def listByTermAll() {
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
    @ApiMethodLight(description="Get a project property with its id or its key")
    @ApiParams(params=[
        @ApiParam(name="idrelation", type="long", paramType = ApiParamType.PATH, description = "The relation id"),
        @ApiParam(name="idterm1", type="long", paramType = ApiParamType.PATH,description = "The term 1 id"),
        @ApiParam(name="idterm2", type="long", paramType = ApiParamType.PATH,description = "The term 2 id")
    ])
    def show() {
        Relation relation
        if (params.idrelation != null) {
            relation = relationService.read(params.long('idrelation'))
        } else {
            relation = relationService.getRelationParent()
        }

        Term term1 = termService.read(params.long('idterm1'))
        Term term2 = termService.read(params.long('idterm2'))

        if (relation && term1 && term2) {
            def relationTerm = relationTermService.get(relation, term1, term2)
            if(relationTerm) {
                responseSuccess(relationTerm)
            } else {
                responseNotFound("RelationTerm", "Relation", relation, "Term1", term1, "Term2", term2)
            }
        }
        else {
            responseNotFound("RelationTerm", "Relation", relation, "Term1", term1, "Term2", term2)
        }

    }

    /**
     * Add a new relation with two terms
     */
    @ApiMethodLight(description="Add a relation between two terms. If not set, relation is PARENT")
    def add() {
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
    @ApiMethodLight(description="Delete a relation between two terms")
    @ApiParams(params=[
        @ApiParam(name="idrelation", type="long", paramType = ApiParamType.PATH,description = "The relation id"),
        @ApiParam(name="idterm1", type="long", paramType = ApiParamType.PATH,description = "The term 1"),
        @ApiParam(name="idterm2", type="long", paramType = ApiParamType.PATH,description = "The term 2")
    ])
    def delete() {

        Relation relation
        if (params.idrelation != null && params.idrelation!="null")
            relation = relationService.read(params.long('idrelation'))
        else
            relation = relationService.getRelationParent()

        def json = JSON.parse("{relation: ${relation ? relation.id : -1}, term1: $params.idterm1, term2: $params.idterm2}")
        delete(relationTermService, json,null)
    }
}
