package be.cytomine.api.processing

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

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.CytomineException
import be.cytomine.api.RestController

/**
 * Controller that handle Retrieval request
 *
 */
//TODO:APIDOC
class RestRetrievalController extends RestController {

    def imageRetrievalService
    def cytomineService

    /**
     * Look for similar annotation and term suggested for annotation in params
     */
    def listSimilarAnnotationAndBestTerm = {

        log.info "List with id userannotation:" + params.idannotation
        try {

            AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(params.idannotation)

            if(!annotation) {
                responseNotFound("AnnotationDomain",params.idannotation)
            } else {
                def data = imageRetrievalService.listSimilarAnnotationAndBestTerm(annotation.project, annotation)
               responseSuccess(data)
            }
        } catch (CytomineException e) {
                log.error(e)
                response([success: false, errors: e.msg], e.code)
         }catch (java.net.ConnectException ex) {
            response.status = 500
            log.error "Retrieval connexion: " + ex.toString()
        }
    }


    def missingAnnotation = {
        log.info "get missing annotation"
        imageRetrievalService.indexMissingAnnotation()
        responseSuccess([])
    }
}
