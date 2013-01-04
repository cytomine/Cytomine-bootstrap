package be.cytomine.test.http

import be.cytomine.processing.Job
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.command.Task

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Job to Cytomine with HTTP request during functional test
 */
class JobAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info "show job $id"
        String URL = Infos.CYTOMINEURL + "api/job/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list job"
        String URL = Infos.CYTOMINEURL + "api/job.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def listBySoftware(Long id, String username, String password) {
        log.info "list job by software $id"
        String URL = Infos.CYTOMINEURL + "api/software/$id/job.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listBySoftwareAndProject(Long idSoftware, Long idProject,String username, String password, boolean light) {
        log.info "list job by software $idSoftware and project $idProject"
        String URL = ""
        if(!light) {
            URL = Infos.CYTOMINEURL + "api/software/$idSoftware/project/$idProject/job.json"
        } else {
            URL = Infos.CYTOMINEURL + "api/project/$idProject/job.json?software="+idSoftware + "&light=true&max=5"
        }
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(Job jobToAdd, User user) {
       create(jobToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(Job jobToAdd, String username, String password) {
        return create(jobToAdd.encodeAsJSON(), username, password)
    }

    static def create(String json, User user) {
        create(json,user.username,user.password)
    }

    static def create(String json, String username, String password) {
        log.info "create job"
        String URL = Infos.CYTOMINEURL + "api/job.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(json)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def jsonResponse = JSON.parse(response)
        Long idJob = jsonResponse?.job?.id
        return [data: Job.get(idJob), code: code]
    }

    static def update(Job job, String username, String password) {
        log.info "update job"
        Integer oldProgress = 0
        Integer newProgress = 100

        def mapNew = ["progress": newProgress]
        def mapOld = ["progress": oldProgress]

        /* Create a Name1 job */
        log.info("create job")
        Job jobToAdd = BasicInstance.createOrGetBasicJob()
        jobToAdd.progress = oldProgress
        assert (jobToAdd.save(flush: true) != null)

        /* Encode a niew job Name2*/
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.progress = newProgress
        jsonJob = jsonUpdate.encodeAsJSON()
        def data = update(job.id, jsonJob, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonJob, String username, String password) {
        log.info "delete job"
        String URL = Infos.CYTOMINEURL + "api/job/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonJob)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info "delete job"
        String URL = Infos.CYTOMINEURL + "api/job/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def deleteAllJobData(def id, String username, String password) {
        log.info "delete job"
        String URL = Infos.CYTOMINEURL + "api/job/" + id + "/alldata.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def deleteAllJobData(def id, def task,String username, String password) {
        log.info "delete job"
        String URL = Infos.CYTOMINEURL + "api/job/" + id + "/alldata.json?task="+task
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listAllJobData(def id, String username, String password) {
        log.info "get job"
        String URL = Infos.CYTOMINEURL + "api/job/" + id + "/alldata.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

}
