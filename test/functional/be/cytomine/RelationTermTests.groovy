package be.cytomine

import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.project.RelationTerm
import be.cytomine.test.BasicInstance

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 23/02/11
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */
class RelationTermTests extends functionaltestplugin.FunctionalTestCase{

  void testListRelationTermWithCredential() {

    log.info("get relationTerm")
    String URL = Infos.CYTOMINEURL+"api/relationterm.json"
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

  void testListRelationTermWithoutCredential() {

    log.info("get relationterm")
    String URL = Infos.CYTOMINEURL+"api/relationterm.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)
  }

  void testShowRelationTermWithCredential() {

    log.info("create relationTerm")
    RelationTerm relationTerm =  BasicInstance.createOrGetBasicRelationTerm()

    log.info("get relationTerm")
    String URL = Infos.CYTOMINEURL+"api/relationterm/"+ relationTerm.id +".json"
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

  void testAddRelationTermCorrect() {

    log.info("create relationTerm")
    def relationTermToAdd = BasicInstance.getBasicRelationTermNotExist()
    String jsonRelationTerm = ([relationTerm : relationTermToAdd]).encodeAsJSON()

    log.info("post relationTerm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relationterm.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idRelationTerm = json.relationTerm.id

    log.info("check if object "+ idRelationTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relationterm/"+idRelationTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)
/*
    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(201,code)

    log.info("check if object "+ idRelationTerm +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relationterm/"+idRelationTerm +".json"
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
    idRelationTerm = json.relationTerm.id

    log.info("check if object "+ idRelationTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relationterm/"+idRelationTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code) */

  }

  void testAddRelationTermWithRelationNotExist() {

    /*log.info("create relationterm")
    def relationTermAdd = BasicInstance.getBasicRelationTermNotExist()
    String jsonRelationTerm = ([relationTerm : relationTermAdd]).encodeAsJSON()
    log.info("jsonRelationTerm="+jsonRelationTerm)
    def jsonUpdate = JSON.parse(jsonRelationTerm)
    jsonUpdate.relationTerm.relation.id = -99
    jsonRelationTerm = jsonUpdate.encodeAsJSON()

    log.info("post relationterm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relationterm.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)*/

  }

  void testAddRelationTerm1WithTermNotExist() {

    /*log.info("create relationterm")
    def relationTermAdd = BasicInstance.getBasicRelationTermNotExist()
    String jsonRelationTerm = ([relationTerm : relationTermAdd]).encodeAsJSON()
    log.info("jsonRelationTerm="+jsonRelationTerm)
    def jsonUpdate = JSON.parse(jsonRelationTerm)
    jsonUpdate.relationTerm.term1.id = -99
    jsonRelationTerm = jsonUpdate.encodeAsJSON()

    log.info("post relationterm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relationterm.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)  */

  }

  void testAddRelationTerm2WithTermNotExist() {

   /* log.info("create relationterm")
    def relationTermAdd = BasicInstance.getBasicRelationTermNotExist()
    String jsonRelationTerm = ([relationTerm : relationTermAdd]).encodeAsJSON()
    log.info("jsonRelationTerm="+jsonRelationTerm)
    def jsonUpdate = JSON.parse(jsonRelationTerm)
    jsonUpdate.relationTerm.term2.id = -99
    jsonRelationTerm = jsonUpdate.encodeAsJSON()

    log.info("post relationterm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relationterm.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)   */

  }

  void testDeleteRelationTerm() {

   /* log.info("create relationTerm")
    def relationTermToDelete = BasicInstance.createOrGetBasicRelationTerm()
    String jsonRelationTerm = ([relationTerm : relationTermToDelete]).encodeAsJSON()
    int idRelationTerm = relationTermToDelete.id
    log.info("delete relationTerm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relationterm/"+idRelationTerm+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(204,code)

    log.info("check if object "+ idRelationTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relationterm/"+idRelationTerm +".json"
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
    int newIdRelationTerm  = json.relationTerm.id

    log.info("check if object "+ idRelationTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relationterm/"+newIdRelationTerm  +".json"
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

    log.info("check if object "+ newIdRelationTerm +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relationterm/"+idRelationTerm +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code) */

  }

  void testDeleteRelationTermNotExist() {

    /* log.info("create project")
    def relationTermToDelete = BasicInstance.createOrGetBasicRelationTerm()
    String jsonRelationTerm = ([relationTerm : relationTermToDelete]).encodeAsJSON()

    log.info("delete relationTerm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relationterm/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code) */
  }

  /*void testRel() {
    def relationTermToDelete = BasicInstance.createOrGetBasicRelationTerm()
      def relation = relationTermToDelete.relation
      def term1 = relationTermToDelete.term1
      def term2 = relationTermToDelete.term2
     log.info("create relationterm")

     log.info("unlink")
    RelationTerm.unlink(relation,term1,term2)
    log.info("link")
    RelationTerm.link(relation,term1,term2)
    log.info("unlink")
    RelationTerm.unlink(relation,term1,term2)
    log.info("end")
    def relationTermToDelete2 = BasicInstance.createOrGetBasicRelationTerm()
    String jsonRelationTerm = ([relationTerm : relationTermToDelete]).encodeAsJSON()

    log.info("delete relationTerm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relationterm/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }  */

}
