package be.cytomine.test.http

import be.cytomine.processing.Software
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Software to Cytomine with HTTP request during functional test
 */
class SoftwareAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info "show software $id"
        String URL = Infos.CYTOMINEURL + "api/software/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list software"
        String URL = Infos.CYTOMINEURL + "api/software.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(Software softwareToAdd, User user) {
       create(softwareToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(Software softwareToAdd, String username, String password) {
        return create(softwareToAdd.encodeAsJSON(), username, password)
    }

    static def create(String json, User user) {
        create(json,user.username,user.password)
    }

    static def create(String json, String username, String password) {
        log.info "create software"
        String URL = Infos.CYTOMINEURL + "api/software.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def jsonResponse = JSON.parse(response)
        Long idSoftware = jsonResponse?.software?.id
        return [data: Software.get(idSoftware), code: code]
    }

    static def update(Software software, String username, String password) {
        log.info "update software"
        String oldName = "Name1"
        String newName = "Name2"
        String oldNameService = "projectService"
        String newNameService = "userAnnotationService"

        def mapNew = ["name": newName,"serviceName" : newNameService]
        def mapOld = ["name": oldName,"serviceName" : oldNameService]
         /* Create a Name1 software */
         Software softwareToAdd = BasicInstance.createOrGetBasicSoftware()
         softwareToAdd.name = oldName
        softwareToAdd.serviceName = oldNameService
         assert (softwareToAdd.save(flush:true) != null)
         /* Encode a niew software Name2*/
         Software softwareToEdit = Software.get(softwareToAdd.id)
         def jsonSoftware = softwareToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonSoftware)
         jsonUpdate.name = newName
        jsonUpdate.serviceName = newNameService
         jsonSoftware = jsonUpdate.encodeAsJSON()
        def data = update(software.id, jsonSoftware, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonSoftware, String username, String password) {
        log.info "delete software"
        String URL = Infos.CYTOMINEURL + "api/software/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonSoftware)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info "delete software"
        String URL = Infos.CYTOMINEURL + "api/software/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
