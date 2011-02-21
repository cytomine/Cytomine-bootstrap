package be.cytomine

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance

import be.cytomine.test.Infos
import be.cytomine.project.Term
import org.apache.http.entity.ContentProducer
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.EntityTemplate
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.HttpResponse
import org.apache.commons.io.IOUtils
import org.apache.http.client.AuthCache
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.client.protocol.ClientContext
import org.apache.http.HttpHost
import be.cytomine.test.HttpClient
import be.cytomine.project.Annotation
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:31
 * To change this template use File | Settings | File Templates.
 */
class TermTests extends functionaltestplugin.FunctionalTestCase {

  void testListTermWithCredential() {

    log.info("get term")
    String URL = Infos.CYTOMINEURL+"api/term.json"
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

  void testListTermWithoutCredential() {

    log.info("get term")
    String URL = Infos.CYTOMINEURL+"api/term.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)
  }

  void testShowTermWithCredential() {

    log.info("create term")
    Term term =  BasicInstance.createOrGetBasicTerm()

    log.info("get term")
    String URL = Infos.CYTOMINEURL+"api/term/"+ term.id +".json"
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


  void testAddTermCorrect() {

    log.info("create term")
    def termToAdd = BasicInstance.getBasicTermNotExist()
    String jsonTerm = ([term : termToAdd]).encodeAsJSON()

    log.info("post term:"+jsonTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idTerm = json.term.id

    log.info("check if object "+ idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm +".json"
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

    log.info("check if object "+ idTerm +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm +".json"
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
    idTerm = json.term.id

    log.info("check if object "+ idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

  void testAddTermWithBadName() {

    log.info("create term")
    def termToAdd = BasicInstance.getBasicTermNotExist()
    String jsonTerm = ([term : termToAdd]).encodeAsJSON()

    def jsonUpdate = JSON.parse(jsonTerm)
    jsonUpdate.term.name = null
    jsonTerm = jsonUpdate.encodeAsJSON()

    log.info("post term:"+jsonTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }



  void testEditTermCorrect() {

    String oldName = "Name1"
    String newName = "Name2"

    String oldComment = "Comment1"
    String newComment = "Comment2"

    def mapNew = ["name":newName,"comment":newComment]
    def mapOld = ["name":oldName,"comment":oldComment]

    /* Create a Name1 term */
    log.info("create term")
    Term termToAdd = BasicInstance.createOrGetBasicTerm()
    termToAdd.name = oldName
    termToAdd.comment = oldComment
    assert (termToAdd.save(flush:true) != null)

    /* Encode a niew term Name2*/
    Term termToEdit = Term.get(termToAdd.id)
    def jsonEdit = [term : termToEdit]
    def jsonTerm = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonTerm)
    jsonUpdate.term.name = newName
    jsonUpdate.term.comment = newComment
    jsonTerm = jsonUpdate.encodeAsJSON()

    log.info("put term:"+jsonTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/"+termToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idTerm = json.term.id

    log.info("check if object "+ idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareTerm(mapNew,json)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareTerm(mapOld,json)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareTerm(mapNew,json)


    log.info("check if object "+ idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testEditTermWithBadName() {

    /* Create a Name1 term */
    log.info("create term")
    Term termToAdd = BasicInstance.createOrGetBasicTerm()

    /* Encode a niew term Name2*/
    Term termToEdit = Term.get(termToAdd.id)
    def jsonEdit = [term : termToEdit]
    def jsonTerm = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonTerm)
    jsonUpdate.term.name = null
    jsonTerm = jsonUpdate.encodeAsJSON()

    log.info("put term:"+jsonTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/"+termToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonTerm)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }


  void testDeleteTerm() {

    log.info("create term")
    def termToDelete = BasicInstance.createOrGetBasicTerm()
    String jsonTerm = ([term : termToDelete]).encodeAsJSON()
    int idTerm = termToDelete.id
    log.info("delete term:"+jsonTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/"+idTerm+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(204,code)

    log.info("check if object "+ idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm +".json"
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
    int newIdTerm  = json.term.id

    log.info("check if object "+ idTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+newIdTerm  +".json"
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

    log.info("check if object "+ newIdTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteTermNotExist() {

     log.info("create term")
    def termToDelete = BasicInstance.createOrGetBasicTerm()
    String jsonTerm = ([term : termToDelete]).encodeAsJSON()

    log.info("delete term:"+jsonTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }


  void testListByAnnotation() {

    log.info("create term")
    Annotation annotation = BasicInstance.createOrGetBasicAnnotation()

    log.info("get term")
    String URL = Infos.CYTOMINEURL+"api/term/annotation/"+ annotation.id +".json"
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

  void testListByAnnotationNotExist() {

    log.info("create term")

    log.info("get term")
    String URL = Infos.CYTOMINEURL+"api/term/annotation/-99.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response:"+response)
    assertEquals(404,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }
}
