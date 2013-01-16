package be.cytomine.test.http

import be.cytomine.image.AbstractImage
import be.cytomine.image.Mime
import be.cytomine.image.acquisition.Instrument
import be.cytomine.laboratory.Sample
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.ontology.AlgoAnnotation

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage AbstractImage to Cytomine with HTTP request during functional test
 */
class AbstractImageAPI extends DomainAPI {

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image.json"
        return doGET(URL, username, password)
    }

    static def list(boolean datatable,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image.json?datatable=${datatable}&_search=false&nd=1358249507672&rows=10&page=1&sidx=id&sord=asc"
        return doGET(URL, username, password)
    }

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/image.json"
        return doGET(URL, username, password)
    }

    static def getMetadata(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/" + id + "/metadata.json?extract=false"
        return doGET(URL, username, password)
    }

    static def getInfo(Long id, String type,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/" + id + "/${type}.json"
        return doGET(URL, username, password)
    }

    static def getCrop(Long id, String type,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/$type/$id/crop.json"
        return doGET(URL, username, password)
    }

    static def create(String jsonAbstractImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image.json"
        def result = doPOST(URL, jsonAbstractImage,username, password)
        if(JSON.parse(jsonAbstractImage) instanceof JSONArray) return result
        result.data = AbstractImage.read(JSON.parse(result.data)?.abstractimage?.id)
        return result
    }

    static def update(def id, def jsonAbstractImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/" + id + ".json"
        return doPUT(URL,jsonAbstractImage,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/image/" + id + ".json"
        return doDELETE(URL,username,password)
    }

}
