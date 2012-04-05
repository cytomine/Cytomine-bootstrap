package be.cytomine.test.http

import be.cytomine.ontology.Ontology
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
 *
 */
class OntologyAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info "show ontology $id"
        String URL = Infos.CYTOMINEURL + "api/ontology/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        list(username,password,false)
    }

    static def list(String username, String password, boolean light) {
        log.info "list ontology"
        String URL = Infos.CYTOMINEURL + "api/ontology.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByUserLight(String username, String password) {
        log.info "list ontology"
        String URL = Infos.CYTOMINEURL + "/api/currentuser/ontology/light.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(Ontology ontologyToAdd, User user) {
       create(ontologyToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(Ontology ontologyToAdd, String username, String password) {
        return create(ontologyToAdd.encodeAsJSON(), username, password)
    }

    static def create(String json, User user) {
        create(json,user.username,user.password)
    }

    static def create(String json, String username, String password) {
        log.info "create ontology"
        String URL = Infos.CYTOMINEURL + "api/ontology.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def jsonResponse = JSON.parse(response)
        Long idOntology = jsonResponse?.ontology?.id
        return [data: Ontology.get(idOntology), code: code]
    }

    static def update(Ontology ontology, String username, String password) {
        log.info "update ontology"
        String oldName = "NAME1"
         String newName = "NAME2"
         def mapNew = ["name":newName]
         def mapOld = ["name":oldName]
         /* Create a Name1 ontology */
         Ontology ontologyToAdd = BasicInstance.createOrGetBasicOntology()
         ontologyToAdd.name = oldName
         assert (ontologyToAdd.save(flush:true) != null)
         /* Encode a niew ontology Name2*/
         Ontology ontologyToEdit = Ontology.get(ontologyToAdd.id)
         def jsonOntology = ontologyToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonOntology)
         jsonUpdate.name = newName
         jsonOntology = jsonUpdate.encodeAsJSON()
        def data = update(ontology.id, jsonOntology, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonOntology, String username, String password) {
        log.info "delete ontology"
        String URL = Infos.CYTOMINEURL + "api/ontology/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonOntology)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info "delete ontology"
        String URL = Infos.CYTOMINEURL + "api/ontology/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
