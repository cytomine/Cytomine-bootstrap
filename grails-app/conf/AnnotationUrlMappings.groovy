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
//        "/api/annotation/search.$format"(controller:"restAnnotationDomain"){
//            action = []
//        }

        /**
         * Annotation generic
         */
        "/api/annotation/method/union.$format"(controller:"restAnnotationDomain"){
            action = [PUT:"union", GET:"union"]
        }
        "/api/annotation.$format"(controller:"restAnnotationDomain"){
            action = [GET: "search",POST:"add"]
        }
        "/api/annotation/method/download"(controller:"restAnnotationDomain"){
            action = [GET: "downloadSearched"]
        }
        "/api/annotation/$id.$format"(controller:"restAnnotationDomain"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/annotation/$id/simplify.$format"(controller:"restAnnotationDomain"){
            action = [PUT:"simplify",GET:"simplify"]
        }

        "/api/simplify.$format"(controller:"restAnnotationDomain"){
            action = [PUT:"retrieveSimplify",POST:"retrieveSimplify"]
        }



        /**
         * User Annotation
         */
        "/api/userannotation.$format"(controller:"restUserAnnotation"){
            action = [GET: "list",POST:"add"]
        }
        "/api/userannotation/$id.$format"(controller:"restUserAnnotation"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }



        /**
         * Comment annotation
         */
        "/api/annotation/$userannotation/comment.$format"(controller:"restUserAnnotation"){
            action = [POST: "addComment", GET:"listComments"]
        }
        "/api/annotation/$userannotation/comment/$id.$format"(controller:"restUserAnnotation"){
            action = [GET:"showComment"]
        }


        /**
         * Retrieval annotation suggestion
         */
        "/api/retrieval/missing/userannotation.$format"(controller: "restRetrieval"){
            action = [GET:"missingAnnotation"]
        }
        "/api/annotation/$idannotation/retrieval.$format"(controller:"restRetrieval"){
            action = [GET:"listSimilarAnnotationAndBestTerm"]
        }

        /**
         * Algo Annotation
         */
        "/api/algoannotation/method/union.$format"(controller:"restAlgoAnnotation"){
            action = [PUT:"union", GET:"union"]
        }
        "/api/algoannotation.$format"(controller:"restAlgoAnnotation"){
            action = [GET: "list",POST:"add"]
        }
        "/api/algoannotation/$id.$format"(controller:"restAlgoAnnotation"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }



        /**
         * Annotation correction
         */
        "/api/annotationcorrection.$format"(controller:"restAnnotationDomain"){
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
        "/api/imageinstance/$idImage/annotation/included.$format"(controller:"restAnnotationDomain"){
                action = [GET: "listIncludedAnnotation"]
         }
        "/api/imageinstance/$idImage/annotation/included/download"(controller: "restAnnotationDomain"){
            action = [GET:"downloadIncludedAnnotation"]
        }


        "/api/user/$id/userannotation/count.$format"(controller:"restUserAnnotation"){
            action = [GET: "countByUser"]
        }
        "/api/user/$id/reviewedannotation/count.$format"(controller:"restReviewedAnnotation"){
            action = [GET: "countByUser"]
        }

    }
}
