package be.cytomine.test.http

import be.cytomine.project.Project
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import org.apache.commons.logging.LogFactory
import grails.converters.JSON
import be.cytomine.security.User
import be.cytomine.ontology.Ontology

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 *
 */
class ProjectAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info("show project:" + id)
        String URL = Infos.CYTOMINEURL + "api/project/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info("list project")
        String URL = Infos.CYTOMINEURL + "api/project.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByUser(Long id, String username, String password) {
        log.info("list project")
        String URL = Infos.CYTOMINEURL + "api/user/$id/project.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listBySoftware(Long id, String username, String password) {
        log.info("list project")
        String URL = Infos.CYTOMINEURL + "api/software/$id/project.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByOntology(Long id, String username, String password) {
        log.info("list project")
        String URL = Infos.CYTOMINEURL + "api/ontology/$id/project.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByDiscipline(Long id, String username, String password) {
        log.info("list project")
        String URL = Infos.CYTOMINEURL + "api/discipline/$id/project.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def create(Project projectToAdd, User user) {
       create(projectToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(Project projectToAdd, String username, String password) {
        return create(projectToAdd.encodeAsJSON(), username, password)
    }

    static def create(String jsonProject, User user) {
        create(jsonProject,user.username,user.password)
    }

    static def create(String jsonProject, String username, String password) {
        log.info("post project:" + jsonProject.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/project.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonProject)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def json = JSON.parse(response)
        Long idProject = json?.project?.id
        return [data: Project.get(idProject), code: code]
    }

    static def update(Project project, String username, String password) {
        String oldName = "Name1"
        String newName = Math.random()+""

        Ontology oldOtology = BasicInstance.createOrGetBasicOntology()
        Ontology newOtology = BasicInstance.getBasicOntologyNotExist()
        newOtology.save(flush: true)

        def mapNew = ["name": newName, "ontology": newOtology]
        def mapOld = ["name": oldName, "ontology": oldOtology]

        def jsonProject = project.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = newName
        jsonUpdate.ontology = newOtology.id
        jsonProject = jsonUpdate.encodeAsJSON()

        def data = update(project.id, jsonProject, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonProject, String username, String password) {
        /* Encode a niew project Name2*/
        String URL = Infos.CYTOMINEURL + "api/project/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonProject)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info("delete project")
        String URL = Infos.CYTOMINEURL + "api/project/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def addUserProject(def idProject, def idUser, User user) {
        addUserProject(idProject, idUser, user.username, user.password)
    }

    static def addUserProject(def idProject, def idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/${idProject}/user/${idUser}.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post('')
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: response, code: code]
    }

    static def deleteUserProject(def idProject, def idUser, User user) {
        deleteUserProject(idProject,idUser,user.username,user.password)
    }

    static def deleteUserProject(def idProject, def idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/${idProject}/user/${idUser}.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        return [data: response, code: code]
    }
}
