package be.cytomine.test.http

import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.test.Infos
import be.cytomine.utils.Task
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Project to Cytomine with HTTP request during functional test
 */
class ProjectAPI extends DomainAPI {

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

    static def listByUserLight(Long id, String type, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$id/project/light.json?$type=true"
        return doGET(URL, username, password)
    }

    static def listUser(Long id, String type, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/${type}.json"
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

    static def listRetrieval(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/retrieval/$id/project.json"
        return doGET(URL, username, password)
    }

    static def create(String jsonProject, String username, String password) {
        def json = JSON.parse(jsonProject)
        if(json.ontology && Ontology.read(json.ontology)) {
            Infos.addUserRight(username,Ontology.read(json.ontology))
        }
        String URL = Infos.CYTOMINEURL + "api/project.json"
        def result = doPOST(URL,jsonProject,username,password)
        result.data = Project.get(JSON.parse(result.data)?.project?.id)
        return result
    }

    static def update(def id, String jsonProject, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/" + id + ".json"
        return doPUT(URL,jsonProject,username,password)
    }

    static def delete(def id, String username, String password, Task task = null) {
        String URL = Infos.CYTOMINEURL + "api/project/" + id + ".json" + (task ? "?task=${task.id}" :"")
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

    static def listLastOpened(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/method/lastopened.json"
        return doGET(URL, username, password)
    }

    static def listLastOpened(int max,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/method/lastopened.json?max=$max"
        return doGET(URL, username, password)
    }

    static def doPing(Long idProject,String username, String password) {
        String url = Infos.CYTOMINEURL + "server/ping.json"
        return doPOST(url,'{"project": "' + idProject + '"}',username,password)
    }

    static def listCommandHistory(Long idProject,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$idProject/commandhistory.json"
        return doGET(URL, username, password)
    }

    static def listCommandHistory(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/commandhistory.json"
        return doGET(URL, username, password)
    }


}
