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

import be.cytomine.processing.JobData
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage JobData to Cytomine with HTTP request during functional test
 */
class JobDataAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata.json"
        return doGET(URL, username, password)
    }

    static def listByJob(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/$id/jobdata.json"
        return doGET(URL, username, password)
    }

    static def create(String jsonJobData, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata.json"
        def result = doPOST(URL,jsonJobData,username,password)
        result.data = JobData.get(JSON.parse(result.data)?.jobdata?.id)
        return result
    }

    static def update(def id, def jsonJobData, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + ".json"
        return doPUT(URL,jsonJobData,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + ".json"
        return doDELETE(URL,username,password)
    }

    static def upload(def id, byte[] data, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + "/upload.json"
        return doPUT(URL,data,username,password)
    }

    static def download(def id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + "/download"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        byte[] data = client.getData()
        int code = client.getResponseCode()
        client.disconnect();
        return [data: data, code: code]
    }

    static def view(def id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + "/view"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        byte[] data = client.getData()
        int code = client.getResponseCode()
        client.disconnect();
        return [data: data, code: code]
    }
}
