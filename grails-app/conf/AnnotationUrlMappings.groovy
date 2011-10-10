/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class AnnotationUrlMappings {

    static mappings = {
        /* Annotation */
        "/api/annotation"(controller:"restAnnotation"){
            action = [GET: "list",POST:"add"]
        }
        "/api/annotation/$id"(controller:"restAnnotation"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
    }
}
