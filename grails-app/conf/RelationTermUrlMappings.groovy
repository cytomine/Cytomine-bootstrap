/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class RelationTermUrlMappings {

    static mappings = {
        "/api/relation/$id/term.$format"(controller:"restRelationTerm"){
            action = [POST:"add"]
        }
        "/api/relation/$idrelation/term1/$idterm1/term2/$idterm2.$format"(controller:"restRelationTerm"){
            action = [GET: "show",DELETE:"delete"]
        }
        "/api/relation/parent/term.$format"(controller:"restRelationTerm"){
            action = [POST:"add"]
        }
        "/api/relation/parent/term1/$idterm1/term2/$idterm2.$format"(controller:"restRelationTerm"){
            action = [GET: "show",DELETE:"delete"]
        }
        "/api/relation/term/$id.$format"(controller:"restRelationTerm"){
            action = [GET: "listByTermAll"]
        }
        //i = 1 or 2 (term 1 or term 2), id = id term
        "/api/relation/term/$i/$id.$format"(controller:"restRelationTerm"){
            action = [GET: "listByTerm"]
        }
    }
}

