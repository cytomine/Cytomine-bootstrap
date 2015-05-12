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

import be.cytomine.api.RestController
import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.processing.JobDataBinaryValue
import grails.converters.JSON
import org.restapidoc.annotation.RestApi
import org.restapidoc.annotation.RestApiMethod
import org.restapidoc.annotation.RestApiParam
import org.restapidoc.annotation.RestApiParams
import org.restapidoc.pojo.RestApiParamType
import org.springframework.web.multipart.MultipartHttpServletRequest

/**
 *  Controller that handle job data files
 *  Files can be saved on filesystem or in database
 */
@RestApi(name = "job services", description = "Methods for managing job data file. Files can be saved on filesystem or in database.")
class RestJobDataController extends RestController {

    def jobDataService

    /**
     * List all job data
     */
    @RestApiMethod(description="Get all job data", listing = true)
    def list() {
        responseSuccess(jobDataService.list())
    }

    /**
     * List all data files info by job
     */
    @RestApiMethod(description="Get all data for a job", listing = true)
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The job id")
    ])
    def listByJob() {
        Job job = Job.read(params.long('id'))
        if(job) {
            responseSuccess(jobDataService.list(job))
        } else {
            responseNotFound("Job", params.id)
        }
    }

    /**
     * Get a specific data file info
     */
    @RestApiMethod(description="Get a specific data file info")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The job data id")
    ])
    def show() {
        JobData jobData = jobDataService.read(params.long('id'))
        if (jobData) {
            responseSuccess(jobData)
        } else {
            responseNotFound("JobData", params.id)
        }
    }

    /**
     * Add a new data file description
     * We must call then "upload" action to upload the file
     */
    @RestApiMethod(description="Add a new data file description. After that, call then 'upload' action to upload the file")
    def add() {
        add(jobDataService, request.JSON)
    }

    /**
     * Update file info
     */
    @RestApiMethod(description="Edit a job data")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The job data id")
    ])
    def update() {
        update(jobDataService, request.JSON)
    }

    /**
     * Delete file description
     */
    @RestApiMethod(description="Delete a job data")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The job data id")
    ])
    def delete() {
        delete(jobDataService, JSON.parse("{id : $params.id}"),null)
    }

    /**
     * Upload a file
     */
    @RestApiMethod(description="Upload and add file to a job data")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The job data id")
    ])
    def upload() {
        log.info "Upload file = " + params.getLong('id')

        JobData jobData = jobDataService.read(params.getLong('id'))
        JobDataBinaryValue value = new JobDataBinaryValue(jobData:jobData)
        jobDataService.saveDomain(value)
        jobData.value = value
        jobDataService.saveDomain(jobData)

        byte[] bytes

        if(request instanceof MultipartHttpServletRequest) {
            def file = request.getFile('files[]')
            bytes = file.getBytes()
        } else {
            bytes = request.inputStream.bytes
        }

        if(!jobData) {
            responseNotFound("JobData", params.id)
        } else {
            jobData = jobDataService.save(jobData,bytes)
            responseSuccess(jobData)
        }
    }

    /**
     * View a job data file
     */
    @RestApiMethod(description="View a job data file in the browser")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The job data id")
    ])
    def view() {
        log.info "View file jobdata = " + params.getLong('id')
        JobData jobData = jobDataService.read(params.getLong('id'))
        if(!jobData) {
            responseNotFound("JobData", params.id)
        } else {
            //response.setContentType "image/png"
            response.setHeader "Content-disposition", "inline"
            response.outputStream << jobDataService.read(jobData)
            response.outputStream.flush()
        }
    }

    /**
     * Download a job data file
     */
    @RestApiMethod(description="Download a job data file")
    @RestApiParams(params=[
        @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The job data id")
    ])
    def download() {
        log.info "Download file jobdata = " + params.getLong('id')
        JobData jobData = jobDataService.read(params.getLong('id'))
        if(!jobData) {
            responseNotFound("JobData", params.id)
        } else {
            response.setContentType "application/octet-stream"
            response.setHeader "Content-disposition", "attachment; filename=${jobData.filename}"
            response.outputStream << jobDataService.read(jobData)
            response.outputStream.flush()
        }
    }


}
