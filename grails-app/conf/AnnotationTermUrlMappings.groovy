/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:52
 */
class AnnotationTermUrlMappings {

    static mappings = {
        "/api/annotation/$idannotation/ontology/$idontology/term"(controller:"restAnnotationTerm"){
            action = [GET: "listTermByAnnotationAndOntology"]
        }
        "/api/annotation/$idannotation/term/$idterm"(controller:"restAnnotationTerm"){
            action = [GET:"show",DELETE:"delete",POST:"add"]
        }
    }
}


