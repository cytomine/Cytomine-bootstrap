/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 1/12/11
 * Time: 15:39
 */
class AnnotationFilterUrlMappings {
    static mappings = {
        /* User */
        "/api/annotationfilter"(controller:"restAnnotationFilter"){
            action = [GET:"listByProject", POST:"add"]
        }
        "/api/annotationfilter/$id"(controller:"restAnnotationFilter"){
            action = [GET:"show", PUT:"update", DELETE:"delete"]
        }

        "/api/ontology/$idOntology/annotationfilter"(controller: "restAnnotationFilter"){
            action = [GET:"listByOntology"]
        }
    }
}
