package be.cytomine.test.http

import be.cytomine.processing.Job
import be.cytomine.processing.JobData
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage JobData to Cytomine with HTTP request during functional test
 */
class JobDataAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata.json"
        return doGET(URL, username, password)
    }

    static def listByJob(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/$id/jobdata.json"
        return doGET(URL, username, password)
    }

    static def create(String jsonJobData, String username, String password) {
        log.info("post Jobdata:" + jsonJobData.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/jobdata.json"
        def result = doPOST(URL,jsonJobData,username,password)
        result.data = JobData.get(JSON.parse(result.data)?.jobdata?.id)
        return result
    }

    static def update(def id, def jsonJobData, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + ".json"
        return doPUT(URL,jsonJobData,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + ".json"
        return doDELETE(URL,username,password)
    }

    static def upload(def id, byte[] data, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + "/upload.json"
        return doPUT(URL,data,username,password)
    }

    static def download(def id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/jobdata/" + id + "/download.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        byte[] data = client.getData()
        int code = client.getResponseCode()
        client.disconnect();
        log.info("check response")
        return [data: data, code: code]
    }
}
