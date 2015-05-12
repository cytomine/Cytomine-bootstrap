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

import be.cytomine.processing.AutoLungJob
import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import grails.converters.JSON

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 6/05/13
 * Time: 10:05
 */
class AutoLungJobService  extends AbstractJobService {

    static transactional = false

    static final rabbitQueue = 'jobQueue'

    def cytomineService
    def commandService
    def modelService

    def jobParameterService
    def jobDataService

    def init(Job job, UserJob userJob) {
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_host",job,"localhost:8080").encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_base_path",job,"/api/").encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_public_key",job,userJob.publicKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_private_key",job,userJob.privateKey).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_project",job,job.getProject().id.toString()).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_software",job,job.getSoftware().id.toString()).encodeAsJSON()))
        jobParameterService.add(JSON.parse(createJobParameter("cytomine_id_job",job,job.id.toString()).encodeAsJSON()))
    }

    def execute(Job job, UserJob userJob, boolean preview) {
        AutoLungJob.triggerNow([ job : job, userJob: userJob, preview : preview])
    }

    public boolean previewAvailable() {
        return true
    }

    def getPreviewROI(Job job) {
        return jobDataService.getJobDataBinaryValue(job, "preview_before")
    }

    def getPreview(Job job) {
        return jobDataService.getJobDataBinaryValue(job, "preview_after")
    }

    @Override
    Double computeRate(Job job) {
        return null;
    }
}
