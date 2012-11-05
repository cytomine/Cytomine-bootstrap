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
        //user+image+project+term+conflict
        "/api/project/$idProject/reviewedannotation"(controller: "restReviewedAnnotation"){
             action = [GET:"listByProjectImageTermAndUser"]
         }
        "/api/imageinstance/$idImage/reviewedannotation"(controller: "restReviewedAnnotation"){
             action = [GET:"listByImage"]
         }
        "/api/user/$idUser/imageinstance/$idImage/reviewedannotation"(controller:"restReviewedAnnotation"){
            action = [GET:"listByImageAndUser"]
        }

        //validate all annotation for a job?
    }
}
