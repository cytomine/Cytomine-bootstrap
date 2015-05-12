package be.cytomine.api.processing.algo

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

import be.cytomine.api.RestController
import be.cytomine.ontology.Term
import be.cytomine.security.UserJob

/**
 * Controller for request on stats with retrieval evolution job
 * Retrieval evolution computes the success rate of prediction during the time
 * -> Each iteration is a different user job (ex: 01/01/2012 - user 1 ; 01/04/2012 - user 2...) and their share
 * a common job domain
 */
//TODO:APIDOC
class RetrievalEvolutionStatsController extends RestController {

    def termService
    def algoAnnotationTermService
    def jobService
    def retrievalEvolutionJobService

    /**
     * Compute the retrieval evolution success for all annotation
     */
    //TODO:APIDOC
    def statRetrievalEvolution = {
        UserJob userJob = jobService.retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def data = []
        def evolution = retrievalEvolutionJobService.listAVGEvolution(userJob.job)
        if (evolution) {
            data = ['evolution': evolution]
        }
        responseSuccess(data)
    }

    /**
     * Compute the retrieval evolution success for annotation of a specific term
     */
    //TODO:APIDOC
    def statRetrievalEvolutionByTerm = {
        UserJob userJob = jobService.retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def data = []
        if (params.term!=null) {
            Term term = Term.read(params.term)
            def evolution = retrievalEvolutionJobService.listAVGEvolution(userJob.job,term)
            if (evolution) {
                data = ['evolution': evolution]
            }
            responseSuccess(data)            
        } else {
            responseNotFound("Term", params.term)
        }
    }
}
