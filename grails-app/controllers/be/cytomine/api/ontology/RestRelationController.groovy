package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.ontology.Relation

class RestRelationController extends RestController {

    def springSecurityService
    def relationService

    def list = {
        responseSuccess(relationService.list())
    }

    def show = {
        Relation relation = relationService.read(params.id)
        if (relation) responseSuccess(relation)
        else responseNotFound("Relation", params.id)
    }
}
