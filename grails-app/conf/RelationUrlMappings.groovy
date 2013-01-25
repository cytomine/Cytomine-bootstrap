/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class RelationUrlMappings {

    static mappings = {
        /* Relation (term)*/
        "/api/relation"(controller: "restRelation"){
            action = [GET:"list", POST:"add"]
        }
        "/api/relation/$id"(controller: "restRelation"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }
    }
}
