/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class OntologyUrlMappings {

    static mappings = {
        /* Ontology */
        "/api/ontology"(controller:"restOntology"){
            action = [GET: "list",POST:"add"]
        }
        "/api/ontology/light"(controller:"restOntology"){  //TODO: merge with previous block
            action = [GET: "listLight"]
        }
        "/api/ontology/$id"(controller:"restOntology"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/ontology/$id/tree"(controller:"restOntology"){
            action = [GET:"tree"]
        }

    }
}
