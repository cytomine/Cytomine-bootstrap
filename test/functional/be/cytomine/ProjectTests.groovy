package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.project.Project

import org.codehaus.groovy.grails.web.json.JSONArray

import be.cytomine.security.User
import be.cytomine.test.http.ProjectAPI
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 17/02/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
class ProjectTests extends functionaltestplugin.FunctionalTestCase {

    void testListProjectWithCredential() {
        log.info("list project")
        def result = ProjectAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListProjectWithoutCredential() {
        log.info("list project")
        def result = ProjectAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        log.info("check response:" + response)
        log.info("check response")
        assertEquals(401, result.code)
    }

    void testShowProjectWithCredential() {
        log.info("create project")
        Project project = BasicInstance.createOrGetBasicProjectWithRight()
        log.info("show project")
        def result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListProjectByUser() {
        log.info("create project")
        Project project = BasicInstance.createOrGetBasicProject()
        User user = BasicInstance.createOrGetBasicUser()
        log.info("list project by user")
        def result = ProjectAPI.listByUser(user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testAddProjectCorrect() {
        log.info("create project")
        def projectToAdd = BasicInstance.getBasicProjectNotExist()
        def result = ProjectAPI.create(projectToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(200, result.code)
        Project project = result.data
        log.info("check if object " + project.id + " exist in DB")
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddProjectWithNameAlreadyExist() {
        log.info("init project with bad name")
        def projectToAdd = BasicInstance.createOrGetBasicProject()
        String jsonProject = projectToAdd.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        //jsonUpdate.name = null
        log.info("create project")
        def result = ProjectAPI.create(jsonUpdate.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(409, result.code)
    }

    void testEditProjectCorrect() {
        log.info("create project")
        Project projectToAdd = BasicInstance.createOrGetBasicProjectWithRight()
        def result = ProjectAPI.update(projectToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:"+result)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idProject = json.project.id
        def showResult = ProjectAPI.show(idProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareProject(result.mapNew, json)
    }

    void testEditProjectWithBadName() {
        log.info("create project")
        Project projectToAdd = BasicInstance.createOrGetBasicProjectWithRight()
        log.info("init project with bad name")
        Project projectToEdit = Project.get(projectToAdd.id)
        def jsonProject = projectToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = null
        jsonProject = jsonUpdate.encodeAsJSON()
        log.info("update project")
        def result = ProjectAPI.update(projectToAdd.id, jsonProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)

    }

    void testEditProjectWithNameAlreadyExist() {
        log.info("create 2 projects")
        Project projectWithOldName = BasicInstance.createOrGetBasicProjectWithRight()
        Project projectWithNewName = BasicInstance.getBasicProjectNotExist()
        projectWithNewName.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN, projectWithNewName)
        log.info("init project with name already exist")
        Project projectToEdit = Project.get(projectWithNewName.id)
        def jsonProject = projectToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = projectWithOldName.name
        jsonProject = jsonUpdate.encodeAsJSON()
        log.info("update project")
        def result = ProjectAPI.update(projectToEdit.id, jsonProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(409, result.code)
    }

    void testEditProjectNotExist() {
        log.info("create project")
        Project projectWithOldName = BasicInstance.createOrGetBasicProject()
        Project projectWithNewName = BasicInstance.getBasicProjectNotExist()
        projectWithNewName.save(flush: true)
        log.info("init project not exist")
        Project projectToEdit = Project.get(projectWithNewName.id)
        def jsonProject = projectToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = projectWithOldName.name
        jsonUpdate.id = -99
        jsonProject = jsonUpdate.encodeAsJSON()
        log.info("update project")
        def result = ProjectAPI.update(-99, jsonProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)

    }

    void testDeleteProject() {
        log.info("create project")
        def projectToDelete = BasicInstance.getBasicProjectNotExist()
        assert projectToDelete.save(flush: true) != null
        Infos.addUserRight(Infos.GOODLOGIN, projectToDelete)
        log.info("delete project")
        def result = ProjectAPI.delete(projectToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def showResult = ProjectAPI.show(projectToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(404, showResult.code)
    }

    void testDeleteProjectNotExist() {
        log.info("delete project")
        def result = ProjectAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(404, result.code)
    }




//  void testDeleteProjectWithGroup1() {
//  log.info("create project")
//    def projectToDelete = BasicInstance.getBasicProjectNotExist()
//    def group = BasicInstance.createOrGetBasicGroup()
//
//    assert projectToDelete.save(flush:true)!=null
//      Infos.addUserRight(Infos.GOODLOGIN,projectToDelete)
//    ProjectGroup.link(projectToDelete,group)
//    String jsonProject = projectToDelete.encodeAsJSON()
//    int idProject = projectToDelete.id
//    log.info("delete project:"+jsonProject.replace("\n",""))
//    String URL = Infos.CYTOMINEURL+"api/project/"+idProject+".json"
//    HttpClient client = new HttpClient()
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//    client.delete()
//    int code  = client.getResponseCode()
//    client.disconnect();
//
//    log.info("check response")
//    assertEquals(200,code)
//
//    log.info("check if object "+ idProject +" exist in DB")
//    client = new HttpClient();
//    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
//    client.get()
//    code  = client.getResponseCode()
//    client.disconnect();
//
//    assertEquals(404,code)
//  }
//
//  void testDeleteProjectWithGroup2() {
//  log.info("create project")
//    def projectToDelete = BasicInstance.getBasicProjectNotExist()
//    def group = BasicInstance.createOrGetBasicGroup()
//
//
//    assert projectToDelete.save()!=null
//      Infos.addUserRight(Infos.GOODLOGIN,projectToDelete)
//    ProjectGroup.link(projectToDelete,group)
//    String jsonProject = projectToDelete.encodeAsJSON()
//    int idProject = projectToDelete.id
//    log.info("delete project:"+jsonProject.replace("\n",""))
//    String URL = Infos.CYTOMINEURL+"api/project/"+idProject+".json"
//    HttpClient client = new HttpClient()
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//    client.delete()
//    int code  = client.getResponseCode()
//    client.disconnect();
//
//    log.info("check response")
//    assertEquals(200,code)
//
//    log.info("check if object "+ idProject +" exist in DB")
//    client = new HttpClient();
//    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
//    client.get()
//    code  = client.getResponseCode()
//    client.disconnect();
//
//    assertEquals(404,code)
//  }


}
