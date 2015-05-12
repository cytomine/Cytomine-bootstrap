package be.cytomine

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

import be.cytomine.middleware.AmqpQueueConfigInstance
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AmqpQueueConfigAPI
import be.cytomine.test.http.AmqpQueueConfigInstanceAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by julien 
 * Date : 03/03/15
 * Time : 15:06
 */
class AmqpQueueConfigInstanceTests {

    void testListAmqpQueueConfigInstanceWithCredentials() {
        def result = AmqpQueueConfigInstanceAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        AmqpQueueConfigInstance amqpQueueConfigInstance = BasicInstanceBuilder.getAmqpQueueConfigInstance()
        result = AmqpQueueConfigInstanceAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert ((JSONArray)json.collection).size() >= 1
        assert AmqpQueueConfigInstanceAPI.containsInJSONList(amqpQueueConfigInstance.id, json)
    }

    void testListAmqpQueueConfigInstanceByQueue() {
        AmqpQueueConfigInstance amqpQueueConfigInstance = BasicInstanceBuilder.getAmqpQueueConfigInstance()
        def result = AmqpQueueConfigInstanceAPI.listByQueue(amqpQueueConfigInstance.queue.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = AmqpQueueConfigInstanceAPI.listByQueue(-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testAddAmqpQueueConfigInstanceCorrect() {
        def amqpQueueConfigInstanceToAdd = BasicInstanceBuilder.getAmqpQueueConfigInstanceNotExist()
        def result = AmqpQueueConfigInstanceAPI.create(amqpQueueConfigInstanceToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int id = result.data.id

        result = AmqpQueueConfigInstanceAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddAmqpQueueConfigInstanceAlreadyExist() {
        def amqpQueueConfigInstanceToAdd = BasicInstanceBuilder.getAmqpQueueConfig()
        def result = AmqpQueueConfigAPI.create(amqpQueueConfigInstanceToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }

    void testUpdateAmqpQueueConfigCorrect() {

        AmqpQueueConfigInstance amqpQueueConfigInstance = BasicInstanceBuilder.getAmqpQueueConfigInstance()
        def data = UpdateData.createUpdateSet(amqpQueueConfigInstance, [value: ["OLDValue","NEWValue"]])
        def result = AmqpQueueConfigInstanceAPI.update(amqpQueueConfigInstance.id, data.postData, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAmqpQueueConfigInstance = json.amqpqueueconfiginstance.id

        def showResult = AmqpQueueConfigInstanceAPI.show(idAmqpQueueConfigInstance, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = AmqpQueueConfigInstanceAPI.undo()
        assert 200 == result.code
        showResult = AmqpQueueConfigInstanceAPI.show(idAmqpQueueConfigInstance, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

        showResult = AmqpQueueConfigInstanceAPI.redo()
        assert 200 == result.code
        showResult = AmqpQueueConfigInstanceAPI.show(idAmqpQueueConfigInstance, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
    }

    void testDeleteAmqpQueueConfigInstance() {
        def amqpQueueConfigInstanceToDelete = BasicInstanceBuilder.getAmqpQueueConfigInstance()
        assert amqpQueueConfigInstanceToDelete.save(flush: true)!= null
        def id = amqpQueueConfigInstanceToDelete.id
        def result = AmqpQueueConfigInstanceAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def showResult = AmqpQueueConfigInstanceAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code


        result = AmqpQueueConfigInstanceAPI.undo()
        assert 200 == result.code

        result = AmqpQueueConfigInstanceAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = AmqpQueueConfigInstanceAPI.redo()
        assert 200 == result.code

        result = AmqpQueueConfigInstanceAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

    }

    void testDeleteAmqpQueueConfigInstanceNotExist() {
        def result = AmqpQueueConfigInstanceAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
}
