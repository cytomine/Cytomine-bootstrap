package be.cytomine.test.http

import be.cytomine.processing.JobParameter
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.processing.SoftwareParameter

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 *
 */
class SoftwareParameterAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info "show softwareparameter $id"
        String URL = Infos.CYTOMINEURL + "api/softwareparameter/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list softwareparameter"
        String URL = Infos.CYTOMINEURL + "api/softwareparameter.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def listBySoftware(Long id, String username, String password) {
        log.info "list softwareparameter by software $id"
        String URL = Infos.CYTOMINEURL + "api/software/$id/parameter.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(SoftwareParameter softwareparameterToAdd, User user) {
       create(softwareparameterToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(SoftwareParameter softwareparameterToAdd, String username, String password) {
        return create(softwareparameterToAdd.encodeAsJSON(), username, password)
    }

    static def create(String json, User user) {
        create(json,user.username,user.password)
    }

    static def create(String json, String username, String password) {
        log.info "create softwareparameter"
        String URL = Infos.CYTOMINEURL + "api/softwareparameter.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def jsonResponse = JSON.parse(response)
        Long idSoftwareParameter = jsonResponse?.softwareparameter?.id
        return [data: SoftwareParameter.get(idSoftwareParameter), code: code]
    }

    static def update(SoftwareParameter softwareparameter, String username, String password) {

        String oldValue = "Name1"
        String newValue = "Name2"

        def mapNew = ["value": newValue]
        def mapOld = ["value": oldValue]

        /* Create a Name1 softwareparameter */
        log.info("create softwareparameter")
        SoftwareParameter softwareparameterToAdd = BasicInstance.createOrGetBasicSoftwareParameter()
        softwareparameterToAdd.name = oldValue
        assert (softwareparameterToAdd.save(flush: true) != null)

        /* Encode a niew softwareparameter Name2*/
        SoftwareParameter softwareparameterToEdit = SoftwareParameter.get(softwareparameterToAdd.id)
        def jsonSoftwareparameter = softwareparameterToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSoftwareparameter)
        jsonUpdate.value = newValue
        jsonSoftwareparameter = jsonUpdate.encodeAsJSON()
        def data = update(softwareparameter.id, jsonSoftwareparameter, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonSoftwareParameter, String username, String password) {
        log.info "update softwareparameter"
        String URL = Infos.CYTOMINEURL + "api/softwareparameter/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonSoftwareParameter)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info "delete softwareparameter"
        String URL = Infos.CYTOMINEURL + "api/softwareparameter/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
