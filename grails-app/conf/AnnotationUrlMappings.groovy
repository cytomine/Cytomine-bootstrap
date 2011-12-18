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
        "/api/user/$id/annotation"(controller:"restAnnotation"){
            action = [GET:"listByUser"]
        }
        "/api/user/$idUser/imageinstance/$idImage/annotation"(controller:"restAnnotation"){
            action = [GET:"listByImageAndUser"]
        }
        "/api/project/$id/annotation"(controller: "restAnnotation"){
            action = [GET:"listByProject"]
        }
        "/api/imageinstance/$id/annotation"(controller:"restAnnotation"){
            action = [GET:"listByImage"]
        }
        "/api/term/$idterm/annotation"(controller:"restAnnotation"){
            action = [GET: "listAnnotationByTerm"]
        }
        "/api/term/$idterm/project/$idproject/annotation"(controller:"restAnnotation"){
            action = [GET: "listAnnotationByProjectAndTerm"]
        }
        "/api/term/$idterm/imageinstance/$idimageinstance/annotation"(controller:"restAnnotationTerm"){
            action = [GET: "listAnnotationByProjectAndImageInstance"]
        }
        "/api/project/$id/annotation/download"(controller: "restAnnotation"){
            action = [GET:"downloadDocumentByProject"]
        }
        "/api/annotation/share"(controller: "restAnnotation"){
            action = [POST:"share"]
        }
    }
}
