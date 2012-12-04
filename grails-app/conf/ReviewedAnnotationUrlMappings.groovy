/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 10/10/11
 * Time: 13:49
 */
class ReviewedAnnotationUrlMappings {

    static mappings = {
        /**
         * Reviewed annotation
         */
        "/api/reviewedannotation"(controller: "restReviewedAnnotation"){
            action = [GET: "list",POST:"add"]
         }
        "/api/reviewedannotation/$id"(controller:"restReviewedAnnotation"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }

        "/api/user/$iduser/reviewedannotation"(controller: "restReviewedAnnotation"){
            action = [POST:"add"]
         }

        //?params.users+images+terms+conflict
        "/api/project/$idProject/reviewedannotation"(controller: "restReviewedAnnotation"){
             action = [GET:"listByProjectImageTermAndUser"]
         }
        "/api/imageinstance/$idImage/reviewedannotation"(controller: "restReviewedAnnotation"){
             action = [GET:"listByImage"]
         }
        "/api/term/$idTerm/imageinstance/$idImage/reviewedannotation"(controller: "restReviewedAnnotation"){
             action = [GET:"listByImageAndTerm"]
         }
        "/api/user/$idUser/imageinstance/$idImage/reviewedannotation"(controller:"restReviewedAnnotation"){
            action = [GET:"listByImageAndUser"]
        }

        "/api/annotation/$id/review"(controller: "restReviewedAnnotation"){
            action = [POST:"addAnnotationReview",PUT:"addAnnotationReview",DELETE:"deleteAnnotationReview"]
         }

//        "/api/annotation/$id/review/fill"(controller: "restReviewedAnnotation"){
//            action = [PUT:"fillAnnotationReview"]
//         }

        "/api/imageinstance/$id/review"(controller: "restReviewedAnnotation"){
            action = [POST:"startImageInstanceReview",PUT:"startImageInstanceReview",DELETE: "stopImageInstanceReview"]
         }

        "/api/imageinstance/$image/annotation/review"(controller: "restReviewedAnnotation"){
            action = [POST:"reviewLayer",PUT:"reviewLayer",DELETE: "unReviewLayer"]
         }


        //validate all annotation for a job?
    }
}
