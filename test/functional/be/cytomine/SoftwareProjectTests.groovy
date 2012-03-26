package be.cytomine

import be.cytomine.processing.Software
import be.cytomine.processing.SoftwareProject
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.project.Project

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class SoftwareProjectTests extends functionaltestplugin.FunctionalTestCase {

  void testListSoftwareProjectWithCredential() {

    log.info("list softwareproject")
    String URL = Infos.CYTOMINEURL+"api/softwareproject.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response="+response)
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONArray

  }

  void testListSoftwareProjectBySoftwareWithCredential() {
    Software software = BasicInstance.createOrGetBasicSoftware()
    log.info("list softwareproject")
    String URL = Infos.CYTOMINEURL+"api/software/$software.id/project.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response="+response)
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONArray

  }

  void testListSoftwareProjectByProjectWithCredential() {
    Project project = BasicInstance.createOrGetBasicProject()
    log.info("list softwareproject")
    String URL = Infos.CYTOMINEURL+"api/project/$project.id/software.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response="+response)
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONArray

  }

  void testShowSoftwareprojectWithCredential() {

    SoftwareProject softwareproject = BasicInstance.createOrGetBasicSoftwareProject()

    log.info("list softwareproject")
    String URL = Infos.CYTOMINEURL+"api/softwareproject/"+ softwareproject.id +".json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testAddSoftwareProjectCorrect() {

   log.info("create softwareproject")
    def softwareprojectToAdd = BasicInstance.getBasicSoftwareProjectNotExist()
    println("softwareprojectToAdd.version="+softwareprojectToAdd.version)
    String jsonSoftwareproject = softwareprojectToAdd.encodeAsJSON()

    log.info("post softwareproject:"+jsonSoftwareproject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareproject.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonSoftwareproject)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idSoftwareproject = json.softwareproject.id

    log.info("check if object "+ idSoftwareproject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareproject/"+idSoftwareproject +".json"
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
    assertEquals(200,code)

    log.info("check if object "+ idSoftwareproject +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareproject/"+idSoftwareproject +".json"
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
    assert json instanceof JSONArray

    log.info("check if object "+ idSoftwareproject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareproject/"+idSoftwareproject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

  void testAddSoftwareProjectAlreadyExist() {
    log.info("create softwareproject")
    def softwareprojectToAdd = BasicInstance.createOrGetBasicSoftwareProject()
    //softwareprojectToAdd is save in DB
    String jsonSoftwareproject = softwareprojectToAdd.encodeAsJSON()

    log.info("post softwareproject:"+jsonSoftwareproject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareproject.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonSoftwareproject)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(409,code)
  }

  void testDeleteSoftwareProject() {

    log.info("create softwareproject")
    def softwareprojectToDelete = BasicInstance.getBasicSoftwareProjectNotExist()
    assert softwareprojectToDelete.save(flush:true)!=null
    String jsonSoftwareProject = softwareprojectToDelete.encodeAsJSON()
    int idSoftwareProject = softwareprojectToDelete.id
    log.info("delete softwareproject:"+jsonSoftwareProject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareproject/"+idSoftwareProject+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idSoftwareProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareproject/"+idSoftwareProject +".json"
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
    assertEquals(200,code)

    log.info("check if object "+ idSoftwareProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareproject/"+idSoftwareProject  +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)


    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idSoftwareProject +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareproject/"+idSoftwareProject +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteSoftwareProjectNotExist() {

     log.info("create softwareproject")
    def softwareprojectToDelete = BasicInstance.createOrGetBasicSoftwareProject()
    String jsonSoftwareProject = softwareprojectToDelete.encodeAsJSON()

    log.info("delete softwareproject:"+jsonSoftwareProject.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareproject/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }
}
