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
        //a supprimer
        "/api/annotation/$id/$zoom/crop.$format"(controller: "restAnnotationDomain"){
            action = [GET:"cropAnnotation"]
        }
        "/api/annotation/$id/crop.$format"(controller: "restAnnotationDomain"){
            action = [GET:"cropAnnotation"]
        }
        //a supprimer
        "/api/annotation/$id/cropMin.$format"(controller: "restAnnotationDomain"){
            action = [GET:"cropAnnotationMin"]
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
        "/api/userannotation/$annotation/alphamask-$term.$format"(controller: "restUserAnnotation"){
            action = [GET:"alphamaskUserAnnotation"]
        }
        "/api/user/$id/userannotation/count.$format"(controller:"restUserAnnotation"){
            action = [GET: "countByUser"]
        }
        "/api/userannotation/$annotation/mask-$term.$format"(controller: "restUserAnnotation"){
            action = [GET:"cropmask"]
        }
        //a supprimer
        "/api/userannotation/$id/$zoom/crop.$format"(controller: "restUserAnnotation"){
            action = [GET:"cropUserAnnotation"]
        }
        "/api/userannotation/$id/crop.$format"(controller: "restUserAnnotation"){
            action = [GET:"cropUserAnnotation"]
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

        "/api/algoannotation/$annotation/alphamask-$term.$format"(controller: "restAlgoAnnotation"){
            action = [GET:"alphamaskAlgoAnnotation"]
        }
        //a supprimer
        "/api/algoannotation/$id/$zoom/crop.$format"(controller: "restAlgoAnnotation"){
            action = [GET:"cropAlgoAnnotation"]
        }
        "/api/algoannotation/$id/crop.$format"(controller: "restAlgoAnnotation"){
            action = [GET:"cropAlgoAnnotation"]
        }



        /**
         * Annotation correction
         */
        "/api/annotationcorrection.$format"(controller:"restAnnotationDomain"){
            action = [POST:"addCorrection"]
        }



        /**
         * Review annotation
         */
        "/api/user/$id/reviewedannotation/count.$format"(controller:"restReviewedAnnotation"){
            action = [GET: "countByUser"]
        }
        "/api/reviewedannotation/$annotation/alphamask-$term.$format"(controller: "restReviewedAnnotation"){
            action = [GET:"alphamaskReviewedAnnotation"]
        }
        //a supprimer
        "/api/reviewedannotation/$id/$zoom/crop.$format"(controller: "restReviewedAnnotation"){
            action = [GET:"cropReviewedAnnotation"]
        }
        "/api/reviewedannotation/$id/crop.$format"(controller: "restReviewedAnnotation"){
            action = [GET:"cropReviewedAnnotation"]
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




    }
}
