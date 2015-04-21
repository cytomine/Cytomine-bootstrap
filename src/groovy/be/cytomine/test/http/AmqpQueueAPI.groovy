package be.cytomine.test.http

import be.cytomine.middleware.AmqpQueue
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * Created by julien 
 * Date : 02/03/15
 * Time : 09:18
 */
class AmqpQueueAPI extends DomainAPI{

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password, String name) {
        String URL

        if(name) {
            URL = Infos.CYTOMINEURL + "api/amqp_queue.json?name=" + name
        }
        else
            URL = Infos.CYTOMINEURL + "api/amqp_queue.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue.json"
        def result = doPOST(URL,json,username,password)
        result.data = AmqpQueue.get(JSON.parse(result.data)?.amqpqueue?.id)
        return result
    }

    static def update(Long id, def jsonAmqpQueue, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue/" + id + ".json"
        return doPUT(URL,jsonAmqpQueue,username,password)
    }

    static def delete(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/amqp_queue/" + id + ".json"
        return doDELETE(URL,username,password)
    }

}