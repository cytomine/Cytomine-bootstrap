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

import be.cytomine.middleware.AmqpQueueConfigInstance
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * Created by julien 
 * Date : 03/03/15
 * Time : 14:33
 */
class AmqpQueueConfigInstanceAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config_instance/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config_instance.json"
        return doGET(URL, username, password)
    }

    static def listByQueue(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue/$id/amqp_queue_config_instance.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config_instance.json"
        def result = doPOST(URL,json,username,password)
        result.data = AmqpQueueConfigInstance.get(JSON.parse(result.data)?.amqpqueueconfiginstance?.id)
        return result
    }

    static def update(Long id, def jsonAmqpQueueConfigInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config_instance/" + id + ".json"
        return doPUT(URL, jsonAmqpQueueConfigInstance, username, password)
    }

    static def delete(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config_instance/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
