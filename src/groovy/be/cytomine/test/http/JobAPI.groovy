package be.cytomine.test.http

import be.cytomine.processing.Job
import be.cytomine.security.User

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
        String URL = Infos.CYTOMINEURL + "api/job/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job.json"
        return doGET(URL, username, password)
    }

    static def listBySoftware(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/software/$id/job.json"
        return doGET(URL, username, password)
    }

    static def listBySoftwareAndProject(Long idSoftware, Long idProject,String username, String password, boolean light) {
        log.info "list job by software $idSoftware and project $idProject"
        String URL = ""
        if(!light) {
            URL = Infos.CYTOMINEURL + "api/software/$idSoftware/project/$idProject/job.json"
        } else {
            URL = Infos.CYTOMINEURL + "api/project/$idProject/job.json?software="+idSoftware + "&light=true&max=5"
        }
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job.json"
        def result = doPOST(URL,json,username,password)
        result.data = Job.get(JSON.parse(result.data)?.job?.id)
        return result
    }

    static def update(def id, def jsonJob, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + ".json"
        return doPUT(URL,jsonJob,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + ".json"
        return doDELETE(URL,username,password)
    }

    static def deleteAllJobData(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + "/alldata.json"
        return doDELETE(URL,username,password)
    }

    static def deleteAllJobData(def id, def task,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + "/alldata.json?task="+task
        return doDELETE(URL,username,password)
    }

    static def listAllJobData(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/job/" + id + "/alldata.json"
        return doGET(URL, username, password)
    }

}
