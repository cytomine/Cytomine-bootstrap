package be.cytomine
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.project.Project
import be.cytomine.project.Relation
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 21/02/11
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
class RelationTests extends functionaltestplugin.FunctionalTestCase{

  void testListRelationWithCredential() {

    log.info("get relation")
    String URL = Infos.CYTOMINEURL+"api/relation.json"
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

  void testListRelationWithoutCredential() {

    log.info("get relation")
    String URL = Infos.CYTOMINEURL+"api/relation.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)
  }

  void testShowRelationWithCredential() {

    log.info("create relation")
    Relation relation =  BasicInstance.createOrGetBasicRelation()

    log.info("get relation")
    String URL = Infos.CYTOMINEURL+"api/relation/"+ relation.id +".json"
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


  void testAddRelationCorrect() {

    log.info("create relation")
    def relationToAdd = BasicInstance.getBasicRelationNotExist()
    String jsonRelation = ([relation : relationToAdd]).encodeAsJSON()

    log.info("post relation:"+jsonRelation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idRelation = json.relation.id

    log.info("check if object "+ idRelation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation +".json"
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

    log.info("check if object "+ idRelation +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation +".json"
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
    idRelation = json.relation.id

    log.info("check if object "+ idRelation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

  void testAddRelationWithBadName() {

    log.info("create relation")
    def relationToAdd = BasicInstance.getBasicRelationNotExist()
    String jsonRelation = ([relation : relationToAdd]).encodeAsJSON()

    def jsonUpdate = JSON.parse(jsonRelation)
    jsonUpdate.relation.name = null
    jsonRelation = jsonUpdate.encodeAsJSON()

    log.info("post relation:"+jsonRelation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testAddRelationWithNameAlreadyExist() {

    log.info("create relation")
    def relationToAdd = BasicInstance.getBasicRelationNotExist()
    String jsonRelation = ([relation : relationToAdd]).encodeAsJSON()

    //save the relation, so the "add" below must failed
    relationToAdd.save(flush:true)
    assertNotNull relationToAdd

    log.info("post relation:"+jsonRelation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonRelation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }



  void testEditRelationCorrect() {

    String oldName = "Name1"
    String newName = "Name2"

    def mapNew = ["name":newName]
    def mapOld = ["name":oldName]

    /* Create a Name1 relation */
    log.info("create relation")
    Relation relationToAdd = BasicInstance.createOrGetBasicRelation()
    relationToAdd.name = oldName
    assert (relationToAdd.save(flush:true) != null)

    /* Encode a niew relation Name2*/
    Relation relationToEdit = Relation.get(relationToAdd.id)
    def jsonEdit = [relation : relationToEdit]
    def jsonRelation = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonRelation)
    jsonUpdate.relation.name = newName
    jsonRelation = jsonUpdate.encodeAsJSON()

    log.info("put relation:"+jsonRelation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/"+relationToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonRelation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idRelation = json.relation.id

    log.info("check if object "+ idRelation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareRelation(mapNew,json)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idRelation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareRelation(mapOld,json)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idRelation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareRelation(mapNew,json)


    log.info("check if object "+ idRelation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testEditRelationWithBadName() {

    /* Create a Name1 relation */
    log.info("create relation")
    Relation relationToAdd = BasicInstance.createOrGetBasicRelation()

    /* Encode a niew relation Name2*/
    Relation relationToEdit = Relation.get(relationToAdd.id)
    def jsonEdit = [relation : relationToEdit]
    def jsonRelation = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonRelation)
    jsonUpdate.relation.name = null
    jsonRelation = jsonUpdate.encodeAsJSON()

    log.info("put relation:"+jsonRelation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/"+relationToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonRelation)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testEditRelationWithNameAlreadyExist() {

    /* Create a Name1 relation */
    log.info("create relation")
    Relation relationWithOldName = BasicInstance.createOrGetBasicRelation()
    Relation relationWithNewName = BasicInstance.getBasicRelationNotExist()
    relationWithNewName.save(flush:true)


    /* Encode a niew relation Name2*/
    Relation relationToEdit = Relation.get(relationWithNewName.id)
    log.info("relationToEdit="+relationToEdit)
    def jsonEdit = [relation : relationToEdit]
    def jsonRelation = jsonEdit.encodeAsJSON()
    log.info("jsonRelation="+jsonRelation)
    def jsonUpdate = JSON.parse(jsonRelation)
    jsonUpdate.relation.name = relationWithOldName.name
    jsonRelation = jsonUpdate.encodeAsJSON()

    log.info("put relation:"+jsonRelation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/"+relationToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonRelation)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testEditRelationNotExist() {

    /* Create a Name1 relation */
    log.info("create relation")
    Relation relationWithOldName = BasicInstance.createOrGetBasicRelation()
    Relation relationWithNewName = BasicInstance.getBasicRelationNotExist()
    relationWithNewName.save(flush:true)


    /* Encode a niew relation Name2*/
    Relation relationToEdit = Relation.get(relationWithNewName.id)
    log.info("relationToEdit="+relationToEdit)
    def jsonEdit = [relation : relationToEdit]
    def jsonRelation = jsonEdit.encodeAsJSON()
    log.info("jsonRelation="+jsonRelation)
    def jsonUpdate = JSON.parse(jsonRelation)
    jsonUpdate.relation.name = relationWithOldName.name
    jsonUpdate.relation.id = -99
    jsonRelation = jsonUpdate.encodeAsJSON()

    log.info("put relation:"+jsonRelation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonRelation)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }



  void testDeleteRelation() {

    log.info("create relation")
    def relationToDelete = BasicInstance.createOrGetBasicRelation()
    String jsonRelation = ([relation : relationToDelete]).encodeAsJSON()
    int idRelation = relationToDelete.id
    log.info("delete relation:"+jsonRelation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/"+idRelation+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(204,code)

    log.info("check if object "+ idRelation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation +".json"
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
    int newIdRelation  = json.relation.id

    log.info("check if object "+ idRelation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+newIdRelation  +".json"
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

    log.info("check if object "+ newIdRelation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/relation/"+idRelation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteRelationNotExist() {

     log.info("create relation")
    def relationToDelete = BasicInstance.createOrGetBasicRelation()
    String jsonRelation = ([relation : relationToDelete]).encodeAsJSON()

    log.info("delete relation:"+jsonRelation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/relation/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }


  void testAddRelationWithTerms() {

  }

  void testAddRelationNotExistWithTerm() {

  }

  void testAddRelationWithTermNotExist() {

  }


  void testAddRelationWithTermsAlreadyExist() {

  }
}
