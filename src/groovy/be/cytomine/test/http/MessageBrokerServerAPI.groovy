package be.cytomine.test.http

import be.cytomine.middleware.MessageBrokerServer
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * Created by julien 
 * Date : 05/02/15
 * Time : 16:47
 */
class MessageBrokerServerAPI extends DomainAPI{

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/message_broker_server/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password, String name = null) {
        String URL

        if(name) {
            URL = Infos.CYTOMINEURL + "api/message_broker_server.json?name=" + name
        }
        else
            URL = Infos.CYTOMINEURL + "api/message_broker_server.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/message_broker_server.json"
        def result = doPOST(URL,json,username,password)
        result.data = MessageBrokerServer.get(JSON.parse(result.data)?.messagebrokerserver?.id)
        return result
    }

    static def update(Long id, def jsonMessageBrokerServer, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/message_broker_server/" + id + ".json"
        return doPUT(URL,jsonMessageBrokerServer,username,password)
    }

    static def delete(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/message_broker_server/" + id + ".json"
        return doDELETE(URL,username,password)
    }

}
