/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class AnnotationUrlMappings {

    static mappings = {
        /**
         * Annotation generic
         */
        "/api/annotation/union"(controller:"restAnnotationDomain"){
            action = [PUT:"union", GET:"union"]
        }
        "/api/annotation"(controller:"restAnnotationDomain"){
            action = [GET: "list",POST:"add"]
        }
        "/api/annotation/$id"(controller:"restAnnotationDomain"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/imageinstance/$id/annotation"(controller:"restAnnotationDomain"){
            action = [GET:"listByImage"]
        }
        "/api/project/$id/annotation"(controller: "restAnnotationDomain"){
             action = [GET:"listByProject"]
         }
        "/api/imageinstance/$id/annotation"(controller:"restAnnotationDomain"){
            action = [GET:"listByImage"]
        }
        "/api/user/$idUser/imageinstance/$idImage/annotation"(controller:"restAnnotationDomain"){
            action = [GET:"listByImageAndUser"]
        }
        "/api/project/$id/annotation/download"(controller: "restAnnotationDomain"){
            action = [GET:"downloadDocumentByProject"]
        }
        "/api/term/$idterm/project/$idproject/annotation"(controller:"restAnnotationDomain"){
            action = [GET: "listAnnotationByProjectAndTerm"]
        }
        "/api/term/$idterm/annotation"(controller:"restAnnotationDomain"){
            action = [GET: "listAnnotationByTerm"]
        }

        /**
         * User Annotation
         */
        "/api/userannotation"(controller:"restUserAnnotation"){
            action = [GET: "list",POST:"add"]
        }
        "/api/userannotation/$id"(controller:"restUserAnnotation"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/project/$id/userannotation"(controller: "restUserAnnotation"){
             action = [GET:"listByProject"]
         }
        "/api/imageinstance/$id/userannotation"(controller:"restUserAnnotation"){
            action = [GET:"listByImage"]
        }
        "/api/user/$idUser/imageinstance/$idImage/userannotation"(controller:"restUserAnnotation"){
            action = [GET:"listByImageAndUser"]
        }
        "/api/term/$idterm/userannotation"(controller:"restUserAnnotation"){
            action = [GET: "listAnnotationByTerm"]
        }
        "/api/term/$idterm/project/$idproject/userannotation"(controller:"restUserAnnotation"){
            action = [GET: "listAnnotationByProjectAndTerm"]
        }
        "/api/term/$idterm/imageinstance/$idimageinstance/userannotation"(controller:"restAnnotationTerm"){
            action = [GET: "listAnnotationByProjectAndImageInstance"]
        }

        "/api/project/$id/userannotation/download"(controller: "restUserAnnotation"){
            action = [GET:"downloadDocumentByProject"]
        }

        /**
         * Comment annotation
         */
        "/api/annotation/$userannotation/comment"(controller:"restUserAnnotation"){
            action = [POST: "addComment", GET:"listComments"]
        }
        "/api/annotation/$userannotation/comment/$id"(controller:"restUserAnnotation"){
            action = [GET:"showComment"]
        }


        /**
         * Retrieval annotation suggestion
         */
        "/api/retrieval/missing/userannotation"(controller: "restRetrieval"){
            action = [GET:"missingAnnotation"]
        }
        "/api/annotation/$idannotation/retrieval"(controller:"restRetrieval"){
            action = [GET:"listSimilarAnnotationAndBestTerm",POST:"index"]
        }

        /**
         * Algo Annotation
         */
        "/api/algoannotation/union"(controller:"restAlgoAnnotation"){
            action = [PUT:"union", GET:"union"]
        }
        "/api/algoannotation"(controller:"restAlgoAnnotation"){
            action = [GET: "list",POST:"add"]
        }
        "/api/algoannotation/$id"(controller:"restAlgoAnnotation"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/imageinstance/$id/algoannotation"(controller:"restAlgoAnnotation"){
            action = [GET:"listByImage"]
        }
        "/api/project/$id/algoannotation"(controller: "restAlgoAnnotation"){
             action = [GET:"listByProject"]
         }
        "/api/imageinstance/$id/algoannotation"(controller:"restAlgoAnnotation"){
            action = [GET:"listByImage"]
        }
        "/api/user/$idUser/imageinstance/$idImage/algoannotation"(controller:"restAlgoAnnotation"){
            action = [GET:"listByImageAndUser"]
        }
        "/api/project/$id/algoannotation/download"(controller: "restAlgoAnnotation"){
            action = [GET:"downloadDocumentByProject"]
        }
        "/api/term/$idterm/project/$idproject/algoannotation"(controller:"restAlgoAnnotation"){
            action = [GET: "listAnnotationByProjectAndTerm"]
        }

        /**
         * Annotation correction
         */
        "/api/annotationcorrection"(controller:"restAnnotationDomain"){
            action = [POST:"addCorrection"]
        }
    }
}
