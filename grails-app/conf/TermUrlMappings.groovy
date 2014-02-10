/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class TermUrlMappings {

    static mappings = {
        /* Term */
        "/api/term.$format"(controller:"restTerm"){
            action = [GET: "list",POST:"add"]
        }
        "/api/term/$id.$format"(controller:"restTerm"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/project/$idProject/term.$format"(controller:"restTerm"){
            action = [GET:"listAllByProject"]
        }
        "/api/term/$id/project/stat.$format"(controller:"restTerm"){
            action = [GET:"statProject"]
        }
        "/api/ontology/$idontology/term.$format"(controller:"restTerm"){
            action = [GET:"listByOntology"]
        }
        "/api/ontology/$idontology/term.$format"(controller:"restTerm"){
            action = [GET:"listAllByOntology"]
        }
        "/api/userannotation/$idannotation/user/$idUser/term.$format"(controller:"restAnnotationTerm"){
            action = [GET: "listTermByAnnotation"]
        }
    }
}

