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

import be.cytomine.middleware.AmqpQueue
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AmqpQueueAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by julien 
 * Date : 02/03/15
 * Time : 09:34
 */
class AmqpQueueTests {

    void testListAmqpQueueWithCredentials() {
        def result = AmqpQueueAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, null)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        AmqpQueue amqpQueue = BasicInstanceBuilder.getAmqpQueueNotExist(true)
        result = AmqpQueueAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, null)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert ((JSONArray)json.collection).size() >= 1
        assert AmqpQueueAPI.containsInJSONList(amqpQueue.id, json)

        //List with a name
        result = AmqpQueueAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, amqpQueue.name)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert ((JSONArray)json.collection).size() >= 1
        assert AmqpQueueAPI.containsInJSONList(amqpQueue.id, json)
    }

    void testShowAmqpQueueWithCredentials() {
        AmqpQueue amqpQueue = BasicInstanceBuilder.getAmqpQueue()
        def result = AmqpQueueAPI.show(amqpQueue.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddAmqpQueueCorrect() {
        def amqpQueueToAdd = BasicInstanceBuilder.getAmqpQueueNotExist()
        def result = AmqpQueueAPI.create(amqpQueueToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idAmqpQueue = result.data.id

        result = AmqpQueueAPI.show(idAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = AmqpQueueAPI.undo()
        assert 200 == result.code

        result = AmqpQueueAPI.show(idAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = AmqpQueueAPI.redo()
        assert 200 == result.code

        result = AmqpQueueAPI.show(idAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddAmqpQueueAlreadyExist() {
        def amqpQueueToAdd = BasicInstanceBuilder.getAmqpQueue()
        def result = AmqpQueueAPI.create(amqpQueueToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }

    void testUpdateAmqpQueueCorrect() {

        AmqpQueue amqpQueue = BasicInstanceBuilder.getAmqpQueue()
        def data = UpdateData.createUpdateSet(amqpQueue, [name: ["OLDNAME","NEWNAME"]])
        def result = AmqpQueueAPI.update(amqpQueue.id, data.postData, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        println "JSON LOL : " + json
        Long idAmqpQueue = json.amqpqueue.id

        def showResult = AmqpQueueAPI.show(idAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = AmqpQueueAPI.undo()
        assert 200 == result.code
        showResult = AmqpQueueAPI.show(idAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

        showResult = AmqpQueueAPI.redo()
        assert 200 == result.code
        showResult = AmqpQueueAPI.show(idAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
    }

    void testUpdateAmqpQueueNotExist() {
        AmqpQueue amqpQueueWithOldName = BasicInstanceBuilder.getAmqpQueue()
        AmqpQueue amqpQueueWithNewName = BasicInstanceBuilder.getAmqpQueueNotExist()
        amqpQueueWithNewName.save(flush: true)
        AmqpQueue amqpQueueToEdit = AmqpQueue.get(amqpQueueWithNewName.id)
        def jsonAmqpQueue = amqpQueueToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAmqpQueue)
        jsonUpdate.name = amqpQueueWithOldName.name
        jsonUpdate.id = -99
        jsonAmqpQueue = jsonUpdate.toString()
        def result = AmqpQueueAPI.update(-99, jsonAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }


    void testDeleteAmqpQueue() {
        def amqpQueueToDelete = BasicInstanceBuilder.getAmqpQueue()
        assert amqpQueueToDelete.save(flush: true)!= null
        def idAmqpQueue = amqpQueueToDelete.id
        def result = AmqpQueueAPI.delete(idAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def showResult = AmqpQueueAPI.show(idAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code

        result = AmqpQueueAPI.undo()
        assert 200 == result.code

        result = AmqpQueueAPI.show(idAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = AmqpQueueAPI.redo()
        assert 200 == result.code

        result = AmqpQueueAPI.show(idAmqpQueue, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testDeleteAmqpQueueNotExist() {
        def result = AmqpQueueAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
}
