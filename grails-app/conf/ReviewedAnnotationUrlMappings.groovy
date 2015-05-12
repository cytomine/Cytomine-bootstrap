/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
        "/api/reviewedannotation.$format"(controller: "restReviewedAnnotation"){
            action = [GET: "list",POST:"add"]
         }
        "/api/reviewedannotation/$id.$format"(controller:"restReviewedAnnotation"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }

        "/api/user/$iduser/reviewedannotation.$format"(controller: "restReviewedAnnotation"){
            action = [POST:"add"]
         }

        "/api/annotation/$id/review.$format"(controller: "restReviewedAnnotation"){
            action = [GET: "addAnnotationReview",POST:"addAnnotationReview",PUT:"addAnnotationReview",DELETE:"deleteAnnotationReview"]
         }

//        "/api/annotation/$id/review/fill.$format"(controller: "restReviewedAnnotation"){
//            action = [PUT:"fillAnnotationReview"]
//         }

        "/api/imageinstance/$id/review.$format"(controller: "restReviewedAnnotation"){
            action = [POST:"startImageInstanceReview",PUT:"startImageInstanceReview",DELETE: "stopImageInstanceReview"]
         }

        "/api/imageinstance/$image/annotation/review.$format"(controller: "restReviewedAnnotation"){
            action = [POST:"reviewLayer",PUT:"reviewLayer",DELETE: "unReviewLayer"]
         }


        "/api/imageinstance/$image/reviewedannotation/stats.$format"(controller: "restReviewedAnnotation"){
            action = [GET:"stats"]
         }


        //validate all annotation for a job?
    }
}
