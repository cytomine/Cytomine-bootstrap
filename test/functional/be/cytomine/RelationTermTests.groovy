package be.cytomine

import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.ontology.RelationTerm
import be.cytomine.test.BasicInstance
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 23/02/11
 * Time: 11:01
 * To change this template use File | Settings | File Templates.
 */
class RelationTermTests extends functionaltestplugin.FunctionalTestCase{

  void testListRelationTermWithCredential() {

    RelationTerm relationTerm = BasicInstance.createOrGetBasicRelationTerm()

    log.info("get relationTerm")
    String URL = Infos.CYTOMINEURL+"api/relation/" + relationTerm.relation.id + "/term.json"
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

  void testListRelationTermWithoutCredential() {

    RelationTerm relationTerm = BasicInstance.createOrGetBasicRelationTerm()

    log.info("get relationTerm")
    String URL = Infos.CYTOMINEURL+"api/relation/" + relationTerm.relation.id + "/term.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)
  }

  void testListRelationTermByTerm1() {

    RelationTerm relationTerm = BasicInstance.createOrGetBasicRelationTerm()

    log.info("get relationTerm")
    String URL = Infos.CYTOMINEURL+"api/relation/term/1/"+relationTerm.term1.id+".json"
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
  void testListRelationTermByTerm2() {

    RelationTerm relationTerm = BasicInstance.createOrGetBasicRelationTerm()

    log.info("get relationTerm")
    String URL = Infos.CYTOMINEURL+"api/relation/term/2/"+relationTerm.term2.id+".json"
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
  void testAddRelationTermCorrect() {

    log.info("create RelationTerm")
    def relationTermToAdd = BasicInstance.getBasicRelationTermNotExist()
    relationTermToAdd.discard()
    String jsonRelationTerm = relationTermToAdd.encodeAsJSON()

    log.info("post relationTerm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/"+ relationTermToAdd.relation.id +"/term.json"
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
    int idRelation= json.relationTerm.relation.id
    int idTerm1= json.relationTerm.term1.id
    int idTerm2= json.relationTerm.term2.id

    log.info("check if object "+ idRelation +"/"+ idTerm1 +"/"+ idTerm2 +" exist in DB")
    client = new HttpClient();

    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation+"/term1/"+idTerm1 +"/term2/" + idTerm2 +".json"
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

    log.info("check if object "+ idRelation +"/"+ idTerm1 +"/"+ idTerm2 +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation+"/term1/"+idTerm1 +"/term2/" + idTerm2 +".json"
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

    log.info("check if object "+ idRelation +"/"+ idTerm1 +"/"+ idTerm2 +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation+"/term1/"+idTerm1 +"/term2/" + idTerm2 +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

  void testAddRelationTermAlreadyExist() {
    log.info("create RelationTerm")
    def relationTermToAdd = BasicInstance.createOrGetBasicRelationTerm()
    String jsonRelationTerm = relationTermToAdd.encodeAsJSON()

    log.info("post relationTerm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/"+ relationTermToAdd.relation.id +"/term.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testAddRelationTermWithRelationNotExist() {
     log.info("create RelationTerm")
    def relationTermToAdd = BasicInstance.createOrGetBasicRelationTerm()
    String jsonRelationTerm = relationTermToAdd.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonRelationTerm)
    jsonUpdate.relation.id = -99
    jsonRelationTerm = jsonUpdate.encodeAsJSON()

    log.info("post relationTerm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/-99/term.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testAddRelationTermWithTerm1NotExist() {
      log.info("create RelationTerm")
    def relationTermToAdd = BasicInstance.createOrGetBasicRelationTerm()
    String jsonRelationTerm = relationTermToAdd.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonRelationTerm)
    jsonUpdate.term1.id = -99
    jsonRelationTerm = jsonUpdate.encodeAsJSON()

    log.info("post relationTerm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/"+ relationTermToAdd.relation.id +"/term.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testAddRelationTermWithTerm2NotExist() {
      log.info("create RelationTerm")
    def relationTermToAdd = BasicInstance.createOrGetBasicRelationTerm()
    String jsonRelationTerm = relationTermToAdd.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonRelationTerm)
    jsonUpdate.term2.id = -99
    jsonRelationTerm = jsonUpdate.encodeAsJSON()

    log.info("post relationTerm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/"+ relationTermToAdd.relation.id +"/term.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelationTerm)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testDeleteRelationTerm() {

    log.info("create relationTerm")
    def relationTermToDelete = BasicInstance.getBasicRelationTermNotExist()
    assert relationTermToDelete.save(flush:true)!=null
   // relationTermToDelete.discard()
    String jsonRelationTerm = relationTermToDelete.encodeAsJSON()

    int idRelation = relationTermToDelete.relation.id
    int idTerm1 = relationTermToDelete.term1.id
    int idTerm2 = relationTermToDelete.term2.id
    log.info("delete relationTerm:"+jsonRelationTerm.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/"+relationTermToDelete.relation.id + "/term1/"+relationTermToDelete.term1.id+"/term2/"+relationTermToDelete.term2.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idRelation +"/" + idTerm1 + " exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation + "/term1/"+idTerm1+"/term2/"+idTerm2+".json"
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

    log.info("check if object "+ idRelation +"/" + idTerm1 +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation + "/term1/"+idTerm1+"/term2/"+idTerm2+".json"
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

    log.info("check if object "+ idRelation +"/" + idTerm1 +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation + "/term1/"+idTerm1+"/term2/"+idTerm2+".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteRelationTermNotExist() {

    log.info("create relationTerm")

    String URL = Infos.CYTOMINEURL+"api/relation/-99/term1/-99/term2/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }


}
