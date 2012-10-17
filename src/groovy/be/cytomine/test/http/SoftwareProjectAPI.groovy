package be.cytomine.test.http

import be.cytomine.processing.SoftwareProject
import be.cytomine.security.User
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage SoftwareProjectAPI to Cytomine with HTTP request during functional test
 */
class SoftwareProjectAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info "show softwareproject $id"
        String URL = Infos.CYTOMINEURL + "api/softwareproject/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list softwareproject"
        String URL = Infos.CYTOMINEURL + "api/softwareproject.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def listBySoftware(Long id, String username, String password) {
        log.info "list softwareproject by software $id"
        String URL = Infos.CYTOMINEURL + "api/software/$id/project.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id, String username, String password) {
        log.info "list softwareproject by project $id"
        String URL = Infos.CYTOMINEURL + "api/project/$id/software.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(SoftwareProject softwareprojectToAdd, User user) {
       create(softwareprojectToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(SoftwareProject softwareprojectToAdd, String username, String password) {
        return create(softwareprojectToAdd.encodeAsJSON(), username, password)
    }

    static def create(String json, User user) {
        create(json,user.username,user.password)
    }

    static def create(String json, String username, String password) {
        log.info "create softwareproject"
        String URL = Infos.CYTOMINEURL + "api/softwareproject.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def jsonResponse = JSON.parse(response)
        Long idSoftwareProject = jsonResponse?.softwareproject?.id
        return [data: SoftwareProject.get(idSoftwareProject), code: code]
    }

    static def delete(def id, String username, String password) {
        log.info "delete softwareproject"
        String URL = Infos.CYTOMINEURL + "api/softwareproject/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
