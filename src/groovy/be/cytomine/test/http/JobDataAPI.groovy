package be.cytomine.test.http

import be.cytomine.ontology.Ontology
import be.cytomine.processing.JobData
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.processing.JobData
import be.cytomine.processing.Job

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 *
 */
class JobDataAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info("show Jobdata:" + id)
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info("list Jobdata")
        String URL = Infos.CYTOMINEURL + "api/jobdata.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def listByJob(Long id, String username, String password) {
        log.info("list Jobdata")
        String URL = Infos.CYTOMINEURL + "api/job/$id/jobdata.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(JobData JobdataToAdd, User user) {
       create(JobdataToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(JobData JobdataToAdd, String username, String password) {
        return create(JobdataToAdd.encodeAsJSON(), username, password)
    }

    static def create(String jsonJobData, User user) {
        create(jsonJobData,user.username,user.password)
    }

    static def create(String jsonJobData, String username, String password) {
        log.info("post Jobdata:" + jsonJobData.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/jobdata.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonJobData)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def json = JSON.parse(response)
        Long idJobData = json?.jobdata?.id
        return [data: JobData.get(idJobData), code: code]
    }

    static def update(JobData Jobdata, String username, String password) {
        String oldName = "Name1"
        String newName = Math.random()+""

        Job oldJob = BasicInstance.createOrGetBasicJob()
        Job newJob = BasicInstance.getBasicJobNotExist()
        newJob.save(flush: true)

        def mapNew = ["key": newName, "job": newJob]
        def mapOld = ["key": oldName, "job": oldJob]

        def jsonJobData = Jobdata.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJobData)
        jsonUpdate.key = newName
        jsonUpdate.job = newJob.id
        jsonJobData = jsonUpdate.encodeAsJSON()

        def data = update(Jobdata.id, jsonJobData, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonJobData, String username, String password) {
        /* Encode a niew Jobdata Name2*/
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonJobData)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info("delete Jobdata")
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
