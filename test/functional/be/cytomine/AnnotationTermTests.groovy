package be.cytomine
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.project.Annotation
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 22/02/11
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTermTests extends functionaltestplugin.FunctionalTestCase {

  void testListAnnotationTermWithCredential() {

    log.info("get annotation")
    String URL = Infos.CYTOMINEURL+"api/annotationterm.json"
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

  void testGetAnnotationTermWithCredential() {

    def annotationTermToAdd = BasicInstance.createOrGetBasicAnnotationTerm()

    log.info("get annotation")
    String URL = Infos.CYTOMINEURL+"api/annotationterm/"+annotationTermToAdd.id +".json"
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


  void testAddAnnotationTermCorrect() {

    log.info("create AnnotationTerm")
    def annotationTermToAdd = BasicInstance.getBasicAnnotationTermNotExist()
    String jsonAnnotationTerm = ([annotationTerm : annotationTermToAdd]).encodeAsJSON()

    log.info("post annotationTerm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotationterm.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAnnotationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idAnnotationTerm = json.annotationTerm.id

    log.info("check if object "+ idAnnotationTerm +" exist in DB")
    client = new HttpClient();
    //NORMAL QUE CA FOIRE: créer un show pour annotation-term (et pas que pour annotation)!
    URL = Infos.CYTOMINEURL+"api/annotationterm/"+idAnnotationTerm +".json"
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

    log.info("check if object "+ idAnnotationTerm +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotationterm/"+idAnnotationTerm +".json"
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
    idAnnotationTerm  = json.annotationTerm.id

    log.info("check if object "+ idAnnotationTerm  +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotationterm/"+idAnnotationTerm  +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

  void testAddAnnotationTermWithAnnotationNotExist() {

    log.info("create annotationterm")
    def annotationTermAdd = BasicInstance.getBasicAnnotationTermNotExist()
    String jsonAnnotationTerm = ([annotationTerm : annotationTermAdd]).encodeAsJSON()
    log.info("jsonAnnotationTerm="+jsonAnnotationTerm)
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.annotationTerm.annotation.id = -99
    jsonAnnotationTerm = jsonUpdate.encodeAsJSON()

    log.info("post annotationterm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotationterm.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAnnotationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testAddAnnotationTermWithTermNotExist() {

    log.info("create annotationterm")
    def annotationTermAdd = BasicInstance.getBasicAnnotationTermNotExist()
    String jsonAnnotationTerm = ([annotationTerm : annotationTermAdd]).encodeAsJSON()
    log.info("jsonAnnotationTerm="+jsonAnnotationTerm)
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.annotationTerm.term.id = -99
    jsonAnnotationTerm = jsonUpdate.encodeAsJSON()

    log.info("post annotationterm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotationterm.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAnnotationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }


  void testDeleteAnnotationTerm() {

    log.info("create annotationTerm")
    def annotationTermToDelete = BasicInstance.createOrGetBasicAnnotationTerm()
    String jsonAnnotationTerm = ([annotationTerm : annotationTermToDelete]).encodeAsJSON()
    int idAnnotationTerm = annotationTermToDelete.id
    log.info("delete annotationTerm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotationterm/"+idAnnotationTerm+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(204,code)

    log.info("check if object "+ idAnnotationTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotationterm/"+idAnnotationTerm +".json"
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
    int newIdAnnotationTerm  = json.annotationTerm.id

    log.info("check if object "+ idAnnotationTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotationterm/"+newIdAnnotationTerm  +".json"
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

    log.info("check if object "+ newIdAnnotationTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotationterm/"+idAnnotationTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteAnnotationTermNotExist() {

     log.info("create project")
    def annotationTermToDelete = BasicInstance.createOrGetBasicAnnotationTerm()
    String jsonAnnotationTerm = ([annotationTerm : annotationTermToDelete]).encodeAsJSON()

    log.info("delete annotationTerm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotationterm/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }
}
