package be.cytomine.test.http

import be.cytomine.project.Discipline
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.processing.ImageFilterProject

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage ImageFilterProject to Cytomine with HTTP request during functional test
 */
class ImageFilterProjectAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)


    static def list(String username, String password) {
        log.info "list image filter project"
        String URL = Infos.CYTOMINEURL + "api/imagefilterproject.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id,String username, String password) {
        log.info "list image filter project"
        String URL = Infos.CYTOMINEURL + "api/project/$id/imagefilterproject.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(String json, String username, String password) {
        log.info "create imageFilterToAdd"
        String URL = Infos.CYTOMINEURL + "api/imagefilterproject.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def jsonResponse = JSON.parse(response)
        println jsonResponse
        def id = jsonResponse.imagefilterproject.id
        return [data: ImageFilterProject.get(id), code: code]
    }

    static def delete(def id, String username, String password) {
        log.info "delete imagefilterproject"
        String URL = Infos.CYTOMINEURL + "api/imagefilterproject/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
