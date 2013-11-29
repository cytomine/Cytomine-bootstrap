/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class AnnotationIndexUrlMappings {

    static mappings = {

        "/api/imageinstance/$id/annotationindex.$format"(controller:"restAnnotationIndex"){
            action = [GET:"listByImage"]
        }

    }
}
