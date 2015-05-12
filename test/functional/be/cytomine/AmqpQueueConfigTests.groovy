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

import be.cytomine.middleware.AmqpQueueConfig
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AmqpQueueConfigAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by julien 
 * Date : 27/02/15
 * Time : 15:08
 */
class AmqpQueueConfigTests {
    void testListAmqpQueueConfigWithCredentials() {

        AmqpQueueConfig amqpQueueConfig = BasicInstanceBuilder.getAmqpQueueConfigNotExist(true)
        def result = AmqpQueueConfigAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert ((JSONArray)json.collection).size() >= 1
        assert AmqpQueueConfigAPI.containsInJSONList(amqpQueueConfig.id, json)
    }

    void testShowAmqpQueueConfigWithCredentials() {
        AmqpQueueConfig amqpQueueConfig = BasicInstanceBuilder.getAmqpQueueConfig()
        def result = AmqpQueueConfigAPI.show(amqpQueueConfig.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject


        result = AmqpQueueConfigAPI.showByName(amqpQueueConfig.name, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddAmqpQueueConfigCorrect() {
        def amqpQueueConfigToAdd = BasicInstanceBuilder.getAmqpQueueConfigNotExist()
        println "AMQP TYPE LOL : " + amqpQueueConfigToAdd.type
        def result = AmqpQueueConfigAPI.create(amqpQueueConfigToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idConfig = result.data.id

        result = AmqpQueueConfigAPI.show(idConfig, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = AmqpQueueConfigAPI.undo()
        assert 200 == result.code

        result = AmqpQueueConfigAPI.show(idConfig, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = AmqpQueueConfigAPI.redo()
        assert 200 == result.code

        result = AmqpQueueConfigAPI.show(idConfig, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddAmqpQueueConfigAlreadyExist() {
        def amqpQueueConfigToAdd = BasicInstanceBuilder.getAmqpQueueConfig()
        def result = AmqpQueueConfigAPI.create(amqpQueueConfigToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }

    void testAddAmqpQueueConfigBadName() {
        def amqpQueueConfigToAdd = BasicInstanceBuilder.getAmqpQueueConfigNotExist()
        amqpQueueConfigToAdd.name = "NomInvalide*"
        def result = AmqpQueueConfigAPI.create(amqpQueueConfigToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

    void testUpdateAmqpQueueConfigCorrect() {

        AmqpQueueConfig amqpQueueConfig = BasicInstanceBuilder.getAmqpQueueConfig()
        def data = UpdateData.createUpdateSet(amqpQueueConfig, [defaultValue: ["OLDValue","NEWValue"]])
        def result = AmqpQueueConfigAPI.update(amqpQueueConfig.id, data.postData, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAmqpQueueConfig = json.amqpqueueconfig.id

        def showResult = AmqpQueueConfigAPI.show(idAmqpQueueConfig, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = AmqpQueueConfigAPI.undo()
        assert 200 == result.code
        showResult = AmqpQueueConfigAPI.show(idAmqpQueueConfig, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

        showResult = AmqpQueueConfigAPI.redo()
        assert 200 == result.code
        showResult = AmqpQueueConfigAPI.show(idAmqpQueueConfig, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
    }


    void testDeleteAmqpQueueConfig() {
        def amqpQueueConfigToDelete = BasicInstanceBuilder.getAmqpQueueConfig()
        assert amqpQueueConfigToDelete.save(flush: true)!= null
        def id = amqpQueueConfigToDelete.id
        def result = AmqpQueueConfigAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def showResult = AmqpQueueConfigAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code


        result = AmqpQueueConfigAPI.undo()
        assert 200 == result.code

        result = AmqpQueueConfigAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = AmqpQueueConfigAPI.redo()
        assert 200 == result.code

        result = AmqpQueueConfigAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

    }

    void testDeleteAmqpQueueConfigNotExist() {
        def result = AmqpQueueConfigAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
}
