package be.cytomine.test.http

import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Term to Cytomine with HTTP request during functional test
 */
class TermAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info("show term:" + id)
        String URL = Infos.CYTOMINEURL + "api/term/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info("list term")
        String URL = Infos.CYTOMINEURL + "api/term.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByOntology(Long id, String username, String password) {
        log.info("list term")
        String URL = Infos.CYTOMINEURL + "api/ontology/$id/term.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id, String username, String password) {
        log.info("list term")
        String URL = Infos.CYTOMINEURL + "api/project/$id/term.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def statsTerm(Long id, String username, String password) {
        log.info("statsTerm")
        String URL = Infos.CYTOMINEURL + "api/term/$id/project/stat.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def create(Term termToAdd, User user) {
       create(termToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(Term termToAdd, String username, String password) {
        return create(termToAdd.encodeAsJSON(), username, password)
    }

    static def create(String jsonTerm, User user) {
        create(jsonTerm,user.username,user.password)
    }

    static def create(String jsonTerm, String username, String password) {
        log.info("post term:" + jsonTerm.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/term.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonTerm)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def json = JSON.parse(response)
        if(JSON.parse(jsonTerm) instanceof JSONArray) return [code: code]
        Long idTerm = json?.term?.id
        return [data: Term.get(idTerm), code: code]
    }

    static def update(Term term, String username, String password) {
        String oldName = "Name1"
        String newName = "Name2"

        String oldComment = "Comment1"
        String newComment = "Comment2"

        String oldColor = "000000"
        String newColor = "FFFFFF"

        Ontology oldOntology = BasicInstance.createOrGetBasicOntology()
        Ontology newOntology = BasicInstance.getBasicOntologyNotExist()
        newOntology.save(flush:true)

        def mapOld = ["name":oldName,"comment":oldComment,"color":oldColor,"ontology":oldOntology]
        def mapNew = ["name":newName,"comment":newComment,"color":newColor,"ontology":newOntology]


        /* Create a Name1 term */
        log.info("create term")
        Term termToAdd = BasicInstance.createOrGetBasicTerm()
        termToAdd.name = oldName
        termToAdd.comment = oldComment
        termToAdd.color = oldColor
        termToAdd.ontology = oldOntology
        assert (termToAdd.save(flush:true) != null)

        /* Encode a niew term Name2*/
        Term termToEdit = Term.get(termToAdd.id)
        def jsonTerm = termToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonTerm)
        jsonUpdate.name = newName
        jsonUpdate.comment = newComment
        jsonUpdate.color = newColor
        jsonUpdate.ontology = newOntology.id
        jsonTerm = jsonUpdate.encodeAsJSON()

        def data = update(term.id, jsonTerm, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonTerm, String username, String password) {
        /* Encode a niew term Name2*/
        String URL = Infos.CYTOMINEURL + "api/term/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonTerm)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info("delete term")
        String URL = Infos.CYTOMINEURL + "api/term/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

}
