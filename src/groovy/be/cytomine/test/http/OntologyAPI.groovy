package be.cytomine.test.http

import be.cytomine.ontology.Ontology
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Ontology to Cytomine with HTTP request during functional test
 */
class OntologyAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        list(username,password,false)
    }

    static def list(String username, String password, boolean light) {
        String URL = Infos.CYTOMINEURL +  (light? "api/ontology/light.json":"api/ontology.json")
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology.json"
        def result = doPOST(URL,json,username,password)
        println "result=$result"
        result.data = Ontology.get(JSON.parse(result.data)?.ontology?.id)
        return result
    }

    static def update(def id, def jsonOntology, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology/" + id + ".json"
        return doPUT(URL,jsonOntology,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
