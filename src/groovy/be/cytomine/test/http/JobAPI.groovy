package be.cytomine.test.http

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

import be.cytomine.processing.Job
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Job to Cytomine with HTTP request during functional test
 */
class JobAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job.json"
        return doGET(URL, username, password)
    }

    static def listBySoftware(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/software/$id/job.json"
        return doGET(URL, username, password)
    }

    static def listBySoftwareAndProject(Long idSoftware, Long idProject,String username, String password, boolean light) {
        String URL = Infos.CYTOMINEURL + "api/job.json?software=$idSoftware&project=$idProject&light=$light"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job.json"
        def result = doPOST(URL,json,username,password)
        result.data = Job.get(JSON.parse(result.data)?.job?.id)
        return result
    }

    static def update(def id, def jsonJob, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + ".json"
        return doPUT(URL,jsonJob,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + ".json"
        return doDELETE(URL,username,password)
    }

    static def deleteAllJobData(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + "/alldata.json"
        return doDELETE(URL,username,password)
    }

    static def deleteAllJobData(def id, def task,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + "/alldata.json?task="+task
        return doDELETE(URL,username,password)
    }

    static def listAllJobData(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + "/alldata.json"
        return doGET(URL, username, password)
    }

    static def purgeProjectData(def id,def task,String username,String password) {
        String URL = Infos.CYTOMINEURL + "/api/project/$id/job/purge.json?task=$task"
        return doPOST(URL,"", username, password)
    }

    static def execute(def id,String username,String password) {
        String URL = Infos.CYTOMINEURL + "/api/job/$id/execute.json"
        return doPOST(URL,"", username, password)
    }

}
