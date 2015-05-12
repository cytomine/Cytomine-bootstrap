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
import be.cytomine.security.UserJob
import grails.converters.JSON

/**
 * Software that compute suggest term of each annotation of a project
 * It compute prediction every X days (or week,month, year,...) between two dates
 */
class RetrievalEvolutionJobService extends AbstractJobService{

    static transactional = false
    def springSecurityService
    def cytomineService
    def commandService
    def modelService
    def jobParameterService
    def algoAnnotationTermService

    def init(Job job, UserJob userJob) {
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
    
    def listAVGEvolution(Job job) {
        List<UserJob> userJobs = UserJob.findAllByJob(job, [sort : "created", order: "desc"])
        return algoAnnotationTermService.listAVGEvolution(userJobs, job.project)
    }

    def listAVGEvolution(Job job, Term term) {
        List<UserJob> userJobs = UserJob.findAllByJob(job, [sort : "created", order: "desc"])
        return algoAnnotationTermService.listAVGEvolution(userJobs, job.project,term)
    }

    @Override
    Double computeRate(Job job) {
        if(job.rate==-1 && job.status==Job.SUCCESS) {
            def result = listAVGEvolution(job)
            log.info "result="+result
            if(!result.isEmpty()) {
                job.rate = result.first().avg
//                job.save(flush: true)
            }
        }
        return job.rate
    }
}
