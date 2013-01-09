package be.cytomine.test.http

import be.cytomine.processing.JobParameter
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage JobParameter to Cytomine with HTTP request during functional test
 */
class JobParameterAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobparameter.json"
        return doGET(URL, username, password)
    }

    static def listByJob(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/$id/parameter.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobparameter.json"
        def result = doPOST(URL,json,username,password)
        result.data = JobParameter.get(JSON.parse(result.data)?.jobparameter?.id)
        return result
    }

    static def update(def id, def jsonJobParameter, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + id + ".json"
        return doPUT(URL,jsonJobParameter,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
