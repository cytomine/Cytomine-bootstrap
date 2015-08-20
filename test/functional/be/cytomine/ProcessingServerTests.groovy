package be.cytomine

import be.cytomine.processing.ProcessingServer

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

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ProcessingServerAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class ProcessingServerTests  {

    void testListProcessingServerWithCredential() {
        def result = ProcessingServerAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testShowProcessingServerWithCredential() {
        def result = ProcessingServerAPI.show(BasicInstanceBuilder.getProcessingServer().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        result = ProcessingServerAPI.show(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testAddProcessingServerCorrect() {
        def processingServerToAdd = BasicInstanceBuilder.getProcessingServerNotExist()

        def result = ProcessingServerAPI.create(processingServerToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idSoftware = result.data.id

        result = ProcessingServerAPI.show(idSoftware, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddProcessingServerAlreadyExist() {
        def processingServerToAdd = BasicInstanceBuilder.getProcessingServer()
        def result = ProcessingServerAPI.create(processingServerToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }

    void testAddProcessingServerWithoutCredential() {
        def processingServerToAdd = BasicInstanceBuilder.getProcessingServerNotExist()
        def result = ProcessingServerAPI.create(processingServerToAdd.encodeAsJSON(), Infos.ADMINLOGIN, Infos.ADMINPASSWORD)
        assert 403 == result.code
    }

    void testDeleteProcessingServerWithCredential() {
        def processingServerToDelete = BasicInstanceBuilder.getProcessingServerNotExist(true)
        def id = processingServerToDelete.id
        def result = ProcessingServerAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def showResult = ProcessingServerAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code
    }

    void testDeleteProcessingServerWithoutCredential() {
        def processingServerToDelete = BasicInstanceBuilder.getProcessingServerNotExist(true)
        def id = processingServerToDelete.id
        def result = ProcessingServerAPI.delete(id, Infos.ADMINLOGIN, Infos.ADMINPASSWORD)
        assert 403 == result.code
    }

    void testDeleteSoftwareNotExist() {
        def result = ProcessingServerAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

}
