package be.cytomine.test.http

import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Project to Cytomine with HTTP request during functional test
 */
class ProjectAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project.json"
        return doGET(URL, username, password)
    }

    static def listByUser(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$id/project.json"
        return doGET(URL, username, password)
    }

    static def listBySoftware(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/software/$id/project.json"
        return doGET(URL, username, password)
    }

    static def listByOntology(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology/$id/project.json"
        return doGET(URL, username, password)
    }

    static def listByDiscipline(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/discipline/$id/project.json"
        return doGET(URL, username, password)
    }

    static def listRetrieval(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/retrieval/$id/project.json"
        return doGET(URL, username, password)
    }

    static def create(String jsonProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project.json"
        def result = doPOST(URL,jsonProject,username,password)
        result.data = Project.get(JSON.parse(result.data)?.project?.id)
        return result
    }

    static def update(def id, String jsonProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + id + ".json"
        return doPUT(URL,jsonProject,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + id + ".json"
        return doDELETE(URL,username,password)
    }


    static def addUserProject(def idProject, def idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/${idProject}/user/${idUser}.json"
        return doPOST(URL,"",username,password)
    }

    static def addAdminProject(def idProject, def idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/${idProject}/user/${idUser}/admin.json"
        return doPOST(URL,"",username,password)
    }


    static def deleteUserProject(def idProject, def idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/${idProject}/user/${idUser}.json"
        return doDELETE(URL,username,password)
    }

    static def deleteAdminProject(def idProject, def idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/${idProject}/user/${idUser}/admin.json"
        return doDELETE(URL,username,password)
    }
}
