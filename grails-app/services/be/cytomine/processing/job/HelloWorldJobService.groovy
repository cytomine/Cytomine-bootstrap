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

import be.cytomine.processing.HelloWorldJob
import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON

/**
 * Simple software example
 * Just print hello world
 */
class HelloWorldJobService extends AbstractJobService {

    static transactional = false

    def grailsApplication
    def cytomineService
    def commandService
    def modelService

    def jobParameterService
    def jobDataService


    def init(Job job, UserJob userJob) {
        def serverUrl = grailsApplication.config.grails.serverURL.replace("http://", "").replace("https://", "")
        jobParameterService.add(JSON.parse(createJobParameter("host",job, serverUrl).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_base_path",job,"/api/").encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("publicKey",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("privateKey",job,userJob.privateKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_project",job,job.getProject().id.toString()).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_software",job,job.getSoftware().id.toString()).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_job",job,job.id.toString()).encodeAsJSON()))
    }

    def execute(Job job, UserJob userJob, boolean preview) {
        log.info "Hello world"
        HelloWorldJob.triggerNow([ job : job, userJob: userJob, preview : preview, jobParameters : jobParameterService.list(job)])
    }


    @Override
    Double computeRate(Job job) {
        return null;
    }
}
