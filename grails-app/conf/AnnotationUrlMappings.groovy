/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class AnnotationUrlMappings {

    static mappings = {

//        /**
//         * Annotation search
//         */
//        "/api/annotation/search"(controller:"restAnnotationDomain"){
//            action = []
//        }

        /**
         * Annotation generic
         */
        "/api/annotation/union"(controller:"restAnnotationDomain"){
            action = [PUT:"union", GET:"union"]
        }
        "/api/annotation"(controller:"restAnnotationDomain"){
            action = [GET: "search",POST:"add"]
        }
        "/api/annotation/download"(controller:"restAnnotationDomain"){
            action = [GET: "downloadSearched"]
        }
        "/api/annotation/$id"(controller:"restAnnotationDomain"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/annotation/$id/simplify"(controller:"restAnnotationDomain"){
            action = [PUT:"simplify"]
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
            action = [GET:"listSimilarAnnotationAndBestTerm"]
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



        /**
         * Annotation correction
         */
        "/api/annotationcorrection"(controller:"restAnnotationDomain"){
            action = [POST:"addCorrection"]
        }

        /**
         * Reporting
         */
        "/api/project/$id/userannotation/download"(controller: "restUserAnnotation"){
            action = [GET:"downloadDocumentByProject"]
        }
        "/api/project/$id/algoannotation/download"(controller: "restAlgoAnnotation"){
            action = [GET:"downloadDocumentByProject"]
        }
        "/api/project/$id/annotation/download"(controller: "restAnnotationDomain"){
              action = [GET:"downloadDocumentByProject"]
          }
        "/api/imageinstance/$idImage/annotation/included"(controller:"restAnnotationDomain"){
                action = [GET: "listIncludedAnnotation"]
         }
        "/api/imageinstance/$idImage/annotation/included/download"(controller: "restAnnotationDomain"){
            action = [GET:"downloadIncludedAnnotation"]
        }

    }
}
