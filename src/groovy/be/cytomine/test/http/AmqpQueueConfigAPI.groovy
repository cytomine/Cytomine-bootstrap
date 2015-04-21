package be.cytomine.test.http

import be.cytomine.middleware.AmqpQueueConfig
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * Created by julien 
 * Date : 27/02/15
 * Time : 14:22
 */
class AmqpQueueConfigAPI extends DomainAPI{

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def showByName(String name, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config/name/" + name + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config.json"
        def result = doPOST(URL,json,username,password)
        result.data = AmqpQueueConfig.get(JSON.parse(result.data)?.amqpqueueconfig?.id)
        return result
    }

    static def update(Long id, def jsonAmqpQueueConfig, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config/" + id + ".json"
        return doPUT(URL,jsonAmqpQueueConfig,username,password)
    }

    static def delete(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue_config/" + id + ".json"
        return doDELETE(URL,username,password)
    }

}