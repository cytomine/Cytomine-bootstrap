package be.cytomine.processing.job

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

import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.processing.structure.ConfusionMatrix
import be.cytomine.security.UserJob
import grails.converters.JSON

/**
 * Software that suggest term for each annotation from a projecy
 */
class RetrievalSuggestedTermJobService extends AbstractJobService {

    static transactional = false
    def cytomineService
    def commandService
    def modelService
    def jobParameterService
    def jobService
    def algoAnnotationTermService

    def init(Job job, UserJob userJob) {

        /*
           * 0: type (=> cytomine) or standalone if execute with ide/java -jar  => STRING
           * 1: public key
           * 2: private key
           * 3: N value
           * 4: T value
           * 5: Working dir
           * 6: Cytomine Host
           * 7: Force download crop (even if already exist) => BOOLEAN
           * 8: storeName (KYOTOSINGLEFILE)
           * 9: index project (list: x,y,z)
           * 10: search project (only one)
        */
        //Create Job-parameter
//        jobParameterService.add(JSON.parse(createJobParameter("execType",job,"cytomine").encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("publicKey",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("privateKey",job,userJob.privateKey).encodeAsJSON()))
        //Execute Job
        log.info "Execute Job..."
    }

    def execute(Job job, UserJob userJob, boolean preview) {

        String[] jobParams = getParametersValues(job)
        String[] mainArgs = createArgsArray(job)
        String[] allArgs = new String[mainArgs.length+jobParams.length+2]

        int index = 0
        mainArgs.each {
            allArgs[index] = mainArgs[index]
            index++
        }
        //build software params
        allArgs[index++] = job.id
        allArgs[index++] = UserJob.findByJob(job).id

        jobParams.each {
            allArgs[index++] = it
        }

        printStartJobInfo(job,allArgs)
        launchSoftware(allArgs,job)
        printStopJobInfo(job,allArgs)
    }

    def listAVGEvolution(UserJob userJob) {
        //Get all project userJob
        List userJobs = jobService.getAllLastUserJob(userJob?.job?.project,userJob?.job?.software)
        return algoAnnotationTermService.listAVGEvolution(userJobs, userJob?.job?.project)
    }

    double computeAVG(def userJob) {
       return algoAnnotationTermService.computeAVG(userJob)
   }

   double computeAVGAveragePerClass(def userJob) {
        return  algoAnnotationTermService.computeAVGAveragePerClass(userJob)
  }

    ConfusionMatrix computeConfusionMatrix(List<Term> projectTerms, def userJob) {
       return algoAnnotationTermService.computeConfusionMatrix(projectTerms,userJob)
   }

    Double computeRate(Job job) {
        if(job.rate==-1 && job.status==Job.SUCCESS) {
            try {
            job.rate = computeAVG(UserJob.findByJob(job))
            }catch(Exception e) {
                log.warn "computeRate is null:"+e.toString()
                job.rate = 0
            }
        }
        return job.rate
    }       
}
