package be.cytomine

import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.ontology.SuggestedTerm

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:31
 * To change this template use File | Settings | File Templates.
 */
class SuggestedTermTests extends functionaltestplugin.FunctionalTestCase {


  void testShowSuggestedTermByImageWithCredential() {

    SuggestedTerm suggestedTerm = BasicInstance.createOrGetBasicSuggestedTerm()

    log.info("show")
    String URL = Infos.CYTOMINEURL+"api/annotation/"+suggestedTerm.annotation.id+"/term/"+ suggestedTerm.term.id +"/job/" + suggestedTerm.job.id + "/suggest.json"
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

  void testShowSuggestedTermWithAnnotationNotExist() {

    log.info("show with bad filter")
    String URL = Infos.CYTOMINEURL+"api/annotation/-99/term/-99/term/-99.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }
  void testListSuggestedTermWithCredential() {

    log.info("get suggestedTerm")
    String URL = Infos.CYTOMINEURL+"api/annotation/term/suggest.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response:"+response)
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONArray
  }

  void testListSuggestedTermWithoutCredential() {

    log.info("get suggestedTerm")
    String URL = Infos.CYTOMINEURL+"api/annotation/term/suggest.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)
  }
  void testListSuggestedTermByAnnotationWithCredential() {
    SuggestedTerm suggestedTerm = BasicInstance.createOrGetBasicSuggestedTerm()
    log.info("get suggestedTerm")
    String URL = Infos.CYTOMINEURL+"api/annotation/"+suggestedTerm.annotation.id+"/term/suggest.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response:"+response)
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONArray
  }

/**

        "/api/annotation/$idannotation/term/suggest"(controller:"restSuggestedTerm"){
            action = [,POST:"add"]
        }

        "/api/annotation/$idannotation/term/$idterm/job/$idjob/suggest"(controller:"restSuggestedTerm"){
            action = [DELETE:"delete"]
        }
 */


  void testAddSuggestedTermCorrect() {

    log.info("create suggestedTerm")
    def suggestedTermToAdd = BasicInstance.getBasicSuggestedTermNotExist()

    String jsonSuggestedTerm = suggestedTermToAdd.encodeAsJSON()
    String idannotation = suggestedTermToAdd.getIdAnnotation()
    String idterm = suggestedTermToAdd.getIdTerm()
    String idjob = suggestedTermToAdd.getIdJob()
    log.info("post suggestedTerm:"+jsonSuggestedTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/$idannotation/term/suggest.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonSuggestedTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idSuggestedTerm = json.suggestedterm.id

    log.info("check if object "+ idSuggestedTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/$idannotation/term/$idterm/job/$idjob/suggest.json"
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

    log.info("check if object "+ idSuggestedTerm +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/$idannotation/term/$idterm/job/$idjob/suggest.json"
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
    idSuggestedTerm = json[0].suggestedterm.id

    log.info("check if object "+ idSuggestedTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/$idannotation/term/$idterm/job/$idjob/suggest.json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

  void testAddSuggestedTermWithBadAnnotation() {

    def suggestedTermToAdd = BasicInstance.getBasicSuggestedTermNotExist()
    String jsonSuggestedTerm = suggestedTermToAdd.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonSuggestedTerm)
    jsonUpdate.annotation = -99
    jsonSuggestedTerm = jsonUpdate.encodeAsJSON()

    log.info("post suggestedTerm:"+jsonSuggestedTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/-99/term/suggest.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonSuggestedTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testDeleteSuggestedTerm() {

    log.info("create suggestedTerm")
    def suggestedTermToDelete = BasicInstance.createOrGetBasicSuggestedTerm()
    String idannotation = suggestedTermToDelete.getIdAnnotation()
    String idterm = suggestedTermToDelete.getIdTerm()
    String idjob = suggestedTermToDelete.getIdJob()

    String jsonSuggestedTerm = suggestedTermToDelete.encodeAsJSON()
    int idSuggestedTerm = suggestedTermToDelete.id
    log.info("delete suggestedTerm:"+jsonSuggestedTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/$idannotation/term/$idterm/job/$idjob/suggest.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idSuggestedTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/$idannotation/term/$idterm/job/$idjob/suggest.json"
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

    log.info("check if object "+ idSuggestedTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/$idannotation/term/$idterm/job/$idjob/suggest.json"
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

    log.info("check if object "+ idSuggestedTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/$idannotation/term/$idterm/job/$idjob/suggest.json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteSuggestedTermNotExist() {

    log.info("delete suggestedTerm not exist")
    String URL = Infos.CYTOMINEURL+"api/annotation/-99/term/-99/job/-99/suggest.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }

}
