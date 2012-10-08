package be.cytomine.test.http

import be.cytomine.processing.JobParameter
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
class JobParameterAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info "show jobparameter $id"
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list jobparameter"
        String URL = Infos.CYTOMINEURL + "api/jobparameter.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def listByJob(Long id, String username, String password) {
        log.info "list jobparameter by job $id"
        String URL = Infos.CYTOMINEURL + "api/job/$id/parameter.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(JobParameter jobparameterToAdd, User user) {
       create(jobparameterToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(JobParameter jobparameterToAdd, String username, String password) {
        return create(jobparameterToAdd.encodeAsJSON(), username, password)
    }

    static def create(String json, User user) {
        create(json,user.username,user.password)
    }

    static def create(String json, String username, String password) {
        log.info "create jobparameter"
        String URL = Infos.CYTOMINEURL + "api/jobparameter.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def jsonResponse = JSON.parse(response)
        Long idJobParameter = jsonResponse?.jobparameter?.id
        return [data: JobParameter.get(idJobParameter), code: code]
    }

    static def update(JobParameter jobparameter, String username, String password) {

        String oldValue = "Name1"
        String newValue = "Name2"

        def mapNew = ["value": newValue]
        def mapOld = ["value": oldValue]

        /* Create a Name1 jobparameter */
        log.info("create jobparameter")
        JobParameter jobparameterToAdd = BasicInstance.createOrGetBasicJobParameter()
        jobparameterToAdd.value = oldValue
        assert (jobparameterToAdd.save(flush: true) != null)

        /* Encode a niew jobparameter Name2*/
        JobParameter jobparameterToEdit = JobParameter.get(jobparameterToAdd.id)
        def jsonJobparameter = jobparameterToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJobparameter)
        jsonUpdate.value = newValue
        jsonJobparameter = jsonUpdate.encodeAsJSON()
        def data = update(jobparameter.id, jsonJobparameter, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonJobParameter, String username, String password) {
        log.info "delete jobparameter"
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonJobParameter)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info "delete jobparameter"
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
