package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.project.Image
import be.cytomine.acquisition.Scanner
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.security.User
import be.cytomine.project.Slide
import be.cytomine.warehouse.Mime
import be.cytomine.project.Project
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 17/02/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
class ProjectTests extends functionaltestplugin.FunctionalTestCase{

  void testListProjectWithCredential() {

    log.info("get project")
    String URL = Infos.CYTOMINEURL+"api/project.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response:"+response)
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
  }

  void testListProjectWithoutCredential() {

    log.info("get project")
    String URL = Infos.CYTOMINEURL+"api/project.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)
  }

  void testShowProjectWithCredential() {

    log.info("create project")
    Project project =  BasicInstance.createOrGetBasicProject()

    log.info("get project")
    String URL = Infos.CYTOMINEURL+"api/project/"+ project.id +".json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response:"+response)
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
  }


  void testAddProjectCorrect() {

    log.info("create project")
    def projectToAdd = BasicInstance.getBasicProjectNotExist()
    String jsonProject = ([project : projectToAdd]).encodeAsJSON()

    log.info("post project:"+jsonProject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonProject)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idProject = json.project.id

    log.info("check if object "+ idProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(201,code)

    log.info("check if object "+ idProject +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(404,code)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    //must be done because redo change id
    json = JSON.parse(response)
    assert json instanceof JSONObject
    idProject = json.project.id

    log.info("check if object "+ idProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

  void testAddProjectWithBadName() {

    log.info("create project")
    def projectToAdd = BasicInstance.createOrGetBasicProject()
    String jsonProject = ([project : projectToAdd]).encodeAsJSON()

    def jsonUpdate = JSON.parse(jsonProject)
    jsonUpdate.project.name = null
    jsonProject = jsonUpdate.encodeAsJSON()

    log.info("post project:"+jsonProject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonProject)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testEditProjectCorrect() {

    String oldName = "Name1"
    String newName = "Name2"

    def mapNew = ["name":newName]
    def mapOld = ["name":oldName]

    /* Create a Name1 project */
    log.info("create project")
    Project projectToAdd = BasicInstance.createOrGetBasicProject()
    projectToAdd.name = oldName
    assert (projectToAdd.save(flush:true) != null)

    /* Encode a niew project Name2*/
    Project projectToEdit = Project.get(projectToAdd.id)
    def jsonEdit = [project : projectToEdit]
    def jsonProject = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonProject)
    jsonUpdate.project.name = newName
    jsonProject = jsonUpdate.encodeAsJSON()

    log.info("put project:"+jsonProject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/"+projectToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonProject)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idProject = json.project.id

    log.info("check if object "+ idProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareProject(mapNew,json)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareProject(mapOld,json)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareProject(mapNew,json)


    log.info("check if object "+ idProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testEditProjectWithBadName() {

    /* Create a Name1 project */
    log.info("create project")
    Project projectToAdd = BasicInstance.createOrGetBasicProject()

    /* Encode a niew project Name2*/
    Project projectToEdit = Project.get(projectToAdd.id)
    def jsonEdit = [project : projectToEdit]
    def jsonProject = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonProject)
    jsonUpdate.project.name = null
    jsonProject = jsonUpdate.encodeAsJSON()

    log.info("put project:"+jsonProject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/"+projectToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonProject)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testEditProjectWithNameAlreadyExist() {

    /* Create a Name1 project */
    log.info("create project")
    Project projectWithOldName = BasicInstance.createOrGetBasicProject()
    Project projectWithNewName = BasicInstance.getBasicProjectNotExist()
    projectWithNewName.save(flush:true)


    /* Encode a niew project Name2*/
    Project projectToEdit = Project.get(projectWithNewName.id)
    log.info("projectToEdit="+projectToEdit)
    def jsonEdit = [project : projectToEdit]
    def jsonProject = jsonEdit.encodeAsJSON()
    log.info("jsonProject="+jsonProject)
    def jsonUpdate = JSON.parse(jsonProject)
    jsonUpdate.project.name = projectWithOldName.name
    jsonProject = jsonUpdate.encodeAsJSON()

    log.info("put project:"+jsonProject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/"+projectToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonProject)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testEditProjectNotExist() {

    /* Create a Name1 project */
    log.info("create project")
    Project projectWithOldName = BasicInstance.createOrGetBasicProject()
    Project projectWithNewName = BasicInstance.getBasicProjectNotExist()
    projectWithNewName.save(flush:true)


    /* Encode a niew project Name2*/
    Project projectToEdit = Project.get(projectWithNewName.id)
    log.info("projectToEdit="+projectToEdit)
    def jsonEdit = [project : projectToEdit]
    def jsonProject = jsonEdit.encodeAsJSON()
    log.info("jsonProject="+jsonProject)
    def jsonUpdate = JSON.parse(jsonProject)
    jsonUpdate.project.name = projectWithOldName.name
    jsonUpdate.project.id = -99
    jsonProject = jsonUpdate.encodeAsJSON()

    log.info("put project:"+jsonProject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonProject)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }

  void testDeleteProject() {

    log.info("create project")
    def projectToDelete = BasicInstance.createOrGetBasicProject()
    String jsonProject = ([project : projectToDelete]).encodeAsJSON()
    int idProject = projectToDelete.id
    log.info("delete project:"+jsonProject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/"+idProject+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(204,code)

    log.info("check if object "+ idProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();

    assertEquals(404,code)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int newIdProject  = json.project.id

    log.info("check if object "+ idProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+newIdProject  +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject


    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(204,code)

    log.info("check if object "+ newIdProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteProjectNotExist() {

     log.info("create project")
    def projectToDelete = BasicInstance.createOrGetBasicProject()
    String jsonProject = ([project : projectToDelete]).encodeAsJSON()

    log.info("delete project:"+jsonProject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }


}
