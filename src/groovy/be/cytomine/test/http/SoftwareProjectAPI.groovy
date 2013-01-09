package be.cytomine.test.http

import be.cytomine.processing.SoftwareProject
import be.cytomine.security.User
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage SoftwareProjectAPI to Cytomine with HTTP request during functional test
 */
class SoftwareProjectAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/softwareproject/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/softwareproject.json"
        return doGET(URL, username, password)
    }

    static def listBySoftware(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/software/$id/project.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/software.json"
        return doGET(URL, username, password)
    }

    static def stats(Long idProject, Long idSoftware, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$idProject/software/$idSoftware/stats.json"
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/softwareproject.json"
        def result = doPOST(URL,json,username,password)
        result.data = SoftwareProject.get(JSON.parse(result.data)?.softwareproject?.id)
        return result
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/softwareproject/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
