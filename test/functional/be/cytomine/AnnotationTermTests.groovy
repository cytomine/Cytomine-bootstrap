package be.cytomine

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.ontology.Annotation
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import be.cytomine.ontology.Term
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 22/02/11
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTermTests extends functionaltestplugin.FunctionalTestCase {


  void testListAnnotationTermByAnnotationWithCredential() {

    Annotation annotation = BasicInstance.createOrGetBasicAnnotation()

    log.info("get by annotation")
    String URL = Infos.CYTOMINEURL+"api/annotation/"+annotation.id+"/term.json"
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

  void testListAnnotationTermByAnnotationWithAnnotationNotExist() {

    Annotation annotation = BasicInstance.createOrGetBasicAnnotation()

    log.info("get by annotation not exist")
    String URL = Infos.CYTOMINEURL+"api/annotation/-99/term.json"
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

  void testListAnnotationTermByTermWithCredential() {

    Term term = BasicInstance.createOrGetBasicTerm()

    log.info("get by term")
    String URL = Infos.CYTOMINEURL+"api/term/"+term.id+"/annotation.json"
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

  void testListAnnotationTermByTermWithAnnotationNotExist() {

    Annotation annotation = BasicInstance.createOrGetBasicAnnotation()

    log.info("get by term not exist")
    String URL = Infos.CYTOMINEURL+"api/term/-99/annotation.json"
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


  void testGetAnnotationTermWithCredential() {

    def annotationTermToAdd = BasicInstance.createOrGetBasicAnnotationTerm()

    log.info("get annotation")
    String URL = Infos.CYTOMINEURL+"api/annotation/"+ annotationTermToAdd.annotation.id +"/term/"+annotationTermToAdd.term.id +".json"
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
    def annotationTermToAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermCorrect")
    annotationTermToAdd.discard()
    String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()

    log.info("post annotationTerm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/"+ annotationTermToAdd.annotation.id +"/term/"+ annotationTermToAdd.term.id +".json"
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
    int idAnnotation= json.annotationTerm.annotation
    int idTerm= json.annotationTerm.term

    log.info("check if object "+ idAnnotation +"/"+ idTerm +"exist in DB")
    client = new HttpClient();

    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation+"/term/"+idTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

   /* log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idAnnotation +"/"+ idTerm +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+ idAnnotation +"/term/"+idTerm +".json"
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

    log.info("check if object "+ idAnnotation +"/"+ idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+ idAnnotation +"/term/"+idTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)  */

  }

   void testAddAnnotationTermAlreadyExist() {

    log.info("create AnnotationTerm")
    def annotationTermToAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermAlreadyExist")
    annotationTermToAdd.save(flush:true)
    //annotationTermToAdd is in database, we will try to add it twice
    String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()

    log.info("post annotationTerm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/"+ annotationTermToAdd.annotation.id +"/term/"+ annotationTermToAdd.term.id +".json"
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
  void testAddAnnotationTermWithAnnotationNotExist() {

    log.info("create annotationterm")
    def annotationTermAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermWithAnnotationNotExist")
    String jsonAnnotationTerm = annotationTermAdd.encodeAsJSON()
    log.info("jsonAnnotationTerm="+jsonAnnotationTerm)
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.annotation = -99
    jsonAnnotationTerm = jsonUpdate.encodeAsJSON()

    log.info("post annotationterm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/-99/term/" + annotationTermAdd.term.id  + ".json"
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
    def annotationTermAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermWithTermNotExist")
    String jsonAnnotationTerm = annotationTermAdd.encodeAsJSON()
    log.info("jsonAnnotationTerm="+jsonAnnotationTerm)
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.term = -99
    jsonAnnotationTerm = jsonUpdate.encodeAsJSON()

    log.info("post annotationterm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/"+ annotationTermAdd.annotation.id +"/term/"+ annotationTermAdd.term.id +".json"
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
    String jsonAnnotationTerm = annotationTermToDelete.encodeAsJSON()

    int idAnnotation = annotationTermToDelete.annotation.id
    int idTerm = annotationTermToDelete.term.id
    log.info("delete annotationTerm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/"+annotationTermToDelete.annotation.id + "/term/"+annotationTermToDelete.term.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idAnnotation +"/" + idTerm + " exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation + "/term/"+idTerm+".json"
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
    //int newIdAnnotationTerm  = json.annotationTerm.id

    log.info("check if object "+ idAnnotation +"/" + idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation + "/term/"+idTerm+".json"
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

    log.info("check if object "+ idAnnotation +"/" + idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation + "/term/"+idTerm+".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)*/

  }

  void testDeleteAnnotationTermNotExist() {

     log.info("create project")
    def annotationTermToDelete = BasicInstance.createOrGetBasicAnnotationTerm()
    String jsonAnnotationTerm = annotationTermToDelete.encodeAsJSON()

    log.info("delete annotationTerm:"+jsonAnnotationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/-99/term/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }
}
