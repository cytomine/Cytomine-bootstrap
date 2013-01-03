package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Relation

/**
 * Controller for relation between terms (parent, synonym,...)
 * We only use "parent" now, but we could later implement CRUD to support new type of relation
 */
class RestRelationController extends RestController {

    def springSecurityService
    def relationService

    /**
     * List all relation available
     */
    def list = {
        responseSuccess(relationService.list())
    }

    /**
     * Get a single relation with its id
     */
    def show = {
        Relation relation = relationService.read(params.long('id'))
        if (relation) responseSuccess(relation)
        else responseNotFound("Relation", params.id)
    }
}
