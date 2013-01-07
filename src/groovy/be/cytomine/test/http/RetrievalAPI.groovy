package be.cytomine.test.http

import be.cytomine.project.Discipline
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
 * This class implement all method to easily get/create/update/delete/manage Discipline to Cytomine with HTTP request during functional test
 */
class RetrievalAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def getResults(Long id, String username, String password) {
        log.info "search retrieval $id"
        String URL = Infos.CYTOMINEURL + "/api/annotation/$id/retrieval.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def index(Long id, def json, String username, String password) {
        log.info "index picture $id"
        String URL = Infos.CYTOMINEURL + "/api/annotation/$id/retrieval.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
