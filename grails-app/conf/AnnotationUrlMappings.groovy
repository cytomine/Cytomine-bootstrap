/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class AnnotationUrlMappings {

    static mappings = {
        /**
         * Annotation
         */
        "/api/annotation"(controller:"restAnnotation"){
            action = [GET: "list",POST:"add"]
        }
        "/api/annotation/$id"(controller:"restAnnotation"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/annotation/$id/copy"(controller:"restAnnotation"){
            action = [POST:"copy"]
        }
        "/api/project/$id/annotation"(controller: "restAnnotation"){
            action = [GET:"listByProject"]
        }
        "/api/imageinstance/$id/annotation"(controller:"restAnnotation"){
            action = [GET:"listByImage"]
        }
        "/api/user/$idUser/imageinstance/$idImage/annotation"(controller:"restAnnotation"){
            action = [GET:"listByImageAndUser"]
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

        /**
        * Download listing
        */
        "/api/project/$id/annotation/download"(controller: "restAnnotation"){
            action = [GET:"downloadDocumentByProject"]
        }

        /**
         * Comment annotation
         */
        "/api/annotation/$annotation/comment"(controller:"restAnnotation"){
            action = [POST: "addComment", GET:"listComments"]
        }
        "/api/annotation/$annotation/comment/$id"(controller:"restAnnotation"){
            action = [GET:"showComment"]
        }

        /**
         * Retrieval annotation suggestion
         */
        "/api/retrieval/missing/annotation"(controller: "restRetrieval"){
            action = [GET:"missingAnnotation"]
        }
        "/api/annotation/$idannotation/retrieval"(controller:"restRetrieval"){
            action = [GET:"listSimilarAnnotationAndBestTerm",POST:"index"]
        }
    }
}
