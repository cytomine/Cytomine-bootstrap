package be.cytomine

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.ontology.Annotation
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import be.cytomine.ontology.Term
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.project.Project
import be.cytomine.project.Slide

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 22/02/11
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
class ProjectSlideTests extends functionaltestplugin.FunctionalTestCase {


  void testListProjectSlideByProjectWithCredential() {

    Project project = BasicInstance.createOrGetBasicProject()

    log.info("get by project")
    String URL = Infos.CYTOMINEURL+"api/project/"+project.id+"/slide.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONArray

  }

  void testListProjectSlideByProjectWithProjectNotExist() {

    Project project = BasicInstance.createOrGetBasicProject()

    log.info("get by project not exist")
    String URL = Infos.CYTOMINEURL+"api/project/-99/slide.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
    def json = JSON.parse(response)

  }

  void testListProjectSlideBySlideWithCredential() {

    Slide slide = BasicInstance.createOrGetBasicSlide()

    log.info("get by slide")
    String URL = Infos.CYTOMINEURL+"api/slide/"+slide.id+"/project.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONArray

  }

  void testListProjectSlideBySlideWithProjectNotExist() {

    Project project = BasicInstance.createOrGetBasicProject()

    log.info("get by slide not exist")
    String URL = Infos.CYTOMINEURL+"api/slide/-99/project.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
    def json = JSON.parse(response)

  }


  void testGetProjectSlideWithCredential() {

    def projectSlideToAdd = BasicInstance.createOrGetBasicProjectSlide()

    log.info("get project")
    String URL = Infos.CYTOMINEURL+"api/project/"+ projectSlideToAdd.project.id +"/slide/"+projectSlideToAdd.slide.id +".json"
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


  void testAddProjectSlideCorrect() {

    log.info("create ProjectSlide")
    def projectSlideToAdd = BasicInstance.getBasicProjectSlideNotExist("testAddProjectSlideCorrect")
    projectSlideToAdd.discard()
    String jsonProjectSlide = projectSlideToAdd.encodeAsJSON()

    log.info("post projectSlide:"+jsonProjectSlide.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/"+ projectSlideToAdd.project.id +"/slide/"+ projectSlideToAdd.slide.id +".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonProjectSlide)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idProject= json.projectSlide.project
    int idSlide= json.projectSlide.slide

    log.info("check if object "+ idProject +"/"+ idSlide +"exist in DB")
    client = new HttpClient();

    URL = Infos.CYTOMINEURL+"api/project/"+idProject+"/slide/"+idSlide +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    /*log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idProject +"/"+ idSlide +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+ idProject +"/slide/"+idSlide +".json"
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
    assertEquals(201,code)

    //must be done because redo change id
    json = JSON.parse(response)
    assert json instanceof JSONObject

    log.info("check if object "+ idProject +"/"+ idSlide +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+ idProject +"/slide/"+idSlide +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)*/

  }

   void testAddProjectSlideAlreadyExist() {

    log.info("create ProjectSlide")
    def projectSlideToAdd = BasicInstance.getBasicProjectSlideNotExist("testAddProjectSlideAlreadyExist")
    projectSlideToAdd.save(flush:true)
    //projectSlideToAdd is in database, we will try to add it twice
    String jsonProjectSlide = projectSlideToAdd.encodeAsJSON()

    log.info("post projectSlide:"+jsonProjectSlide.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/"+ projectSlideToAdd.project.id +"/slide/"+ projectSlideToAdd.slide.id +".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonProjectSlide)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }
  void testAddProjectSlideWithProjectNotExist() {

    log.info("create projectslide")
    def projectSlideAdd = BasicInstance.getBasicProjectSlideNotExist("testAddProjectSlideWithProjectNotExist")
    String jsonProjectSlide = projectSlideAdd.encodeAsJSON()
    log.info("jsonProjectSlide="+jsonProjectSlide)
    def jsonUpdate = JSON.parse(jsonProjectSlide)
    jsonUpdate.project = -99
    jsonProjectSlide = jsonUpdate.encodeAsJSON()

    log.info("post projectslide:"+jsonProjectSlide.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/-99/slide/" + projectSlideAdd.slide.id  + ".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonProjectSlide)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testAddProjectSlideWithSlideNotExist() {

    log.info("create projectslide")
    def projectSlideAdd = BasicInstance.getBasicProjectSlideNotExist("testAddProjectSlideWithSlideNotExist")
    String jsonProjectSlide = projectSlideAdd.encodeAsJSON()
    log.info("jsonProjectSlide="+jsonProjectSlide)
    def jsonUpdate = JSON.parse(jsonProjectSlide)
    jsonUpdate.slide = -99
    jsonProjectSlide = jsonUpdate.encodeAsJSON()

    log.info("post projectslide:"+jsonProjectSlide.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/"+ projectSlideAdd.project.id +"/slide/"+ projectSlideAdd.slide.id +".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonProjectSlide)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testDeleteProjectSlide() {

    log.info("create projectSlide")
    def projectSlideToDelete = BasicInstance.createOrGetBasicProjectSlide()
    String jsonProjectSlide = projectSlideToDelete.encodeAsJSON()

    int idProject = projectSlideToDelete.project.id
    int idSlide = projectSlideToDelete.slide.id
    log.info("delete projectSlide:"+jsonProjectSlide.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/"+projectSlideToDelete.project.id + "/slide/"+projectSlideToDelete.slide.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idProject +"/" + idSlide + " exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject + "/slide/"+idSlide+".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();

    assertEquals(404,code)

    /*log.info("test undo")
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
    //int newIdProjectSlide  = json.projectSlide.id

    log.info("check if object "+ idProject +"/" + idSlide +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject + "/slide/"+idSlide+".json"
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
    assertEquals(200,code)

    log.info("check if object "+ idProject +"/" + idSlide +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/project/"+idProject + "/slide/"+idSlide+".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code) */

  }

  void testDeleteProjectSlideNotExist() {

     log.info("create project")
    def projectSlideToDelete = BasicInstance.createOrGetBasicProjectSlide()
    String jsonProjectSlide = projectSlideToDelete.encodeAsJSON()

    log.info("delete projectSlide:"+jsonProjectSlide.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/-99/slide/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }
}
