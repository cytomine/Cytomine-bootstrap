/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class OntologyUrlMappings {

    static mappings = {
        "/api/ontology.$format"(controller:"restOntology"){
            action = [GET: "list",POST:"add"]
        }
//        "/api/ontology/light.$format"(controller:"restOntology"){
//            action = [GET: "listLight"]
//        }
        "/api/ontology/$id.$format"(controller:"restOntology"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/ontology/$id/tree.$format"(controller:"restOntology"){
            action = [GET:"tree"]
        }
    }
}
