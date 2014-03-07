package be.cytomine.test.http

import be.cytomine.ontology.Term
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Term to Cytomine with HTTP request during functional test
 */
class TermAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term.json"
        return doGET(URL, username, password)
    }

    static def listByOntology(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology/$id/term.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/term.json"
        return doGET(URL, username, password)
    }

    static def statsTerm(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$id/project/stat.json"
        return doGET(URL, username, password)
    }

    static def create(def jsonTerm, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term.json"
        def result = doPOST(URL,jsonTerm,username,password)
        def json = JSON.parse(result.data)
        if(JSON.parse(jsonTerm) instanceof JSONArray) return [code: result.code]
        Long idTerm = json?.term?.id
        return [data: Term.get(idTerm), code: result.code]
    }

    static def update(def id, def jsonTerm, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/" + id + ".json"
        return doPUT(URL,jsonTerm,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/" + id + ".json"
        return doDELETE(URL,username,password)
    }

}
