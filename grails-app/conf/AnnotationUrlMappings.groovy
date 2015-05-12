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
        "/api/annotation/$id/crop.$format"(controller: "restAnnotationDomain"){
            action = [GET:"crop"]
        }

        //keep this for retrieval
        "/api/annotation/$id/cropMin.$format"(controller: "restAnnotationDomain"){
            action = [GET:"cropMin"]
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
        "/api/user/$id/userannotation/count.$format"(controller:"restUserAnnotation"){
            action = [GET: "countByUser"]
        }
        "/api/userannotation/$id/crop.$format"(controller: "restUserAnnotation"){
            action = [GET:"crop"]
        }
        "/api/userannotation/$id/mask.$format"(controller: "restUserAnnotation"){
            action = [GET:"cropMask"]
        }
        "/api/userannotation/$id/alphamask.$format"(controller: "restUserAnnotation"){
            action = [GET:"cropAlphaMask"]
        }



        /**
         * Review annotation
         */
        "/api/user/$id/reviewedannotation/count.$format"(controller:"restReviewedAnnotation"){
            action = [GET: "countByUser"]
        }
        "/api/reviewedannotation/$id/crop.$format"(controller: "restReviewedAnnotation"){
            action = [GET:"crop"]
        }
        "/api/reviewedannotation/$id/mask.$format"(controller: "restReviewedAnnotation"){
            action = [GET:"cropMask"]
        }
        "/api/reviewedannotation/$id/alphamask.$format"(controller: "restReviewedAnnotation"){
            action = [GET:"cropAlphaMask"]
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

        "/api/algoannotation/$id/crop.$format"(controller: "restAlgoAnnotation"){
            action = [GET:"crop"]
        }
        "/api/algoannotation/$id/alphamask.$format"(controller: "restAlgoAnnotation"){
            action = [GET:"cropMask"]
        }
        "/api/algoannotation/$id/mask.$format"(controller: "restAlgoAnnotation"){
            action = [GET:"cropAlphaMask"]
        }

        /**
         * Roi Annotation
         */
        "/api/roiannotation.$format"(controller:"restRoiAnnotation"){
            action = [GET: "list",POST:"add"]
        }
        "/api/roiannotation/$id.$format"(controller:"restRoiAnnotation"){
            action = [GET:"show",PUT:"update", DELETE:"delete"]
        }
        "/api/roiannotation/$id/crop.$format"(controller: "restRoiAnnotation"){
            action = [GET:"crop"]
        }
        "/api/roiannotation/$id/mask.$format"(controller: "restRoiAnnotation"){
            action = [GET:"cropMask"]
        }
        "/api/roiannotation/$id/alphamask.$format"(controller: "restRoiAnnotation"){
            action = [GET:"cropAlphaMask"]
        }



        /**
         * Annotation correction
         */
        "/api/annotationcorrection.$format"(controller:"restAnnotationDomain"){
            action = [POST:"addCorrection"]
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
