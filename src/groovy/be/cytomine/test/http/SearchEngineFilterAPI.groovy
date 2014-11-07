package be.cytomine.test.http

import be.cytomine.search.SearchEngineFilter
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: rhoyoux
 * Date: 30/10/14
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage SearchEngineFilter to Cytomine with HTTP request during functional test
 */
class SearchEngineFilterAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/searchenginefilter/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def listAll(String username, String password) {
        String URL = Infos.CYTOMINEURL +  "api/searchenginefilter.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/searchenginefilter.json"
        def result = doPOST(URL,json,username,password)
        result.data = SearchEngineFilter.get(JSON.parse(result.data)?.searchenginefilter?.id)
        return result
    }
    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/searchenginefilter/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
