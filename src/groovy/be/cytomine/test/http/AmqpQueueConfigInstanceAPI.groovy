package be.cytomine.test.http

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
