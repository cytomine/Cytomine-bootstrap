/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class TermUrlMappings {

    static mappings = {
        /* Term */
        "/api/term"(controller:"restTerm"){
            action = [GET: "list",POST:"add"]
        }
        "/api/term/$id"(controller:"restTerm"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/imageinstance/$id/term"(controller:"restTerm"){
            action = [GET:"listByImageInstance"]
        }
        "/api/project/$idProject/term"(controller:"restTerm"){
            action = [GET:"listAllByProject"]
        }
        "/api/term/$id/project/stat"(controller:"restTerm"){
            action = [GET:"statProject"]
        }
        "/api/ontology/$idontology/term"(controller:"restTerm"){
            action = [GET:"listByOntology"]
        }
        "/api/ontology/$idontology/term"(controller:"restTerm"){
            action = [GET:"listAllByOntology"]
        }
    }
}

