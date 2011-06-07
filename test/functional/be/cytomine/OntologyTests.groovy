package be.cytomine
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance

import be.cytomine.test.Infos

import be.cytomine.test.HttpClient
import be.cytomine.ontology.Ontology
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class OntologyTests extends functionaltestplugin.FunctionalTestCase {

  void testListOntologyWithCredential() {

    log.info("list ontology")
    String URL = Infos.CYTOMINEURL+"api/ontology.json"
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

  void testListOntologyWithoutCredential() {

    log.info("list ontology")
    String URL = Infos.CYTOMINEURL+"api/ontology.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)

  }

  void testShowOntologyWithCredential() {

    Ontology ontology = BasicInstance.createOrGetBasicOntology()

    log.info("list ontology")
    String URL = Infos.CYTOMINEURL+"api/ontology/"+ ontology.id +".json"
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

  void testAddOntologyCorrect() {

   log.info("create ontology")
    def ontologyToAdd = BasicInstance.getBasicOntologyNotExist()
    println("ontologyToAdd.version="+ontologyToAdd.version)
    String jsonOntology = ontologyToAdd.encodeAsJSON()

    log.info("post ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonOntology)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idOntology = json.ontology.id

    log.info("check if object "+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology +".json"
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

    log.info("check if object "+ idOntology +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology +".json"
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
    idOntology = json.ontology.id

    log.info("check if object "+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)*/

  }

  void testAddOntologyAlreadyExist() {
    log.info("create ontology")
    def ontologyToAdd = BasicInstance.createOrGetBasicOntology()
    //ontologyToAdd is save in DB
    String jsonOntology = ontologyToAdd.encodeAsJSON()

    log.info("post ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonOntology)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testAddOntologyWithBadName() {
    log.info("create ontology")
    def ontologyToAdd = BasicInstance.getBasicOntologyNotExist()
    String jsonOntology = ontologyToAdd.encodeAsJSON()

    def jsonUpdate = JSON.parse(jsonOntology)
    jsonUpdate.name = null
    jsonOntology = jsonUpdate.encodeAsJSON()

    log.info("post ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonOntology)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testUpdateOntologyCorrect() {

    String oldName = "Name1"
    String newName = "Name2"

    def mapNew = ["name":newName]
    def mapOld = ["name":oldName]

    /* Create a Name1 ontology */
    log.info("create ontology")
    Ontology ontologyToAdd = BasicInstance.createOrGetBasicOntology()
    ontologyToAdd.name = oldName
    assert (ontologyToAdd.save(flush:true) != null)

    /* Encode a niew ontology Name2*/
    Ontology ontologyToEdit = Ontology.get(ontologyToAdd.id)
    def jsonOntology = ontologyToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonOntology)
    jsonUpdate.name = newName
    jsonOntology = jsonUpdate.encodeAsJSON()

    log.info("put ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology/"+ontologyToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonOntology)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idOntology = json.ontology.id

    log.info("check if object "+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareOntology(mapNew,json)

    /*log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareOntology(mapOld,json)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareOntology(mapNew,json)


    log.info("check if object "+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject  */

  }

  void testUpdateOntologyNotExist() {
    /* Create a Name1 ontology */
    log.info("create ontology")
    Ontology ontologyToAdd = BasicInstance.createOrGetBasicOntology()

    /* Encode a niew ontology Name2*/
    Ontology ontologyToEdit = Ontology.get(ontologyToAdd.id)
    def jsonOntology = ontologyToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonOntology)
    jsonUpdate.id = -99
    jsonOntology = jsonUpdate.encodeAsJSON()

    log.info("put ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonOntology)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }

  void testUpdateOntologyWithNameAlreadyExist() {

    /* Create a Name1 ontology */
    log.info("create ontology")
    Ontology ontologyWithOldName = BasicInstance.createOrGetBasicOntology()
    Ontology ontologyWithNewName = BasicInstance.getBasicOntologyNotExist()
    ontologyWithNewName.save(flush:true)


    /* Encode a niew ontology Name2*/
    Ontology ontologyToEdit = Ontology.get(ontologyWithNewName.id)
    log.info("ontologyToEdit="+ontologyToEdit)
    def jsonOntology = ontologyToEdit.encodeAsJSON()
    log.info("jsonOntology="+jsonOntology)
    def jsonUpdate = JSON.parse(jsonOntology)
    jsonUpdate.name = ontologyWithOldName.name
    jsonOntology = jsonUpdate.encodeAsJSON()

    log.info("put ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology/"+ontologyToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonOntology)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testUpdateOntologyWithBadName() {

    /* Create a Name1 ontology */
    log.info("create ontology")
    Ontology ontologyToAdd = BasicInstance.createOrGetBasicOntology()

    /* Encode a niew ontology Name2*/
    Ontology ontologyToEdit = Ontology.get(ontologyToAdd.id)
    def jsonOntology = ontologyToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonOntology)
    jsonUpdate.name = null
    jsonOntology = jsonUpdate.encodeAsJSON()

    log.info("put ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology/"+ontologyToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonOntology)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testDeleteOntology() {

    log.info("create ontology")
    def ontologyToDelete = BasicInstance.getBasicOntologyNotExist()
    assert ontologyToDelete.save(flush:true)!=null
    String jsonOntology = ontologyToDelete.encodeAsJSON()
    int idOntology = ontologyToDelete.id
    log.info("delete ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology +".json"
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
    int newIdOntology  = json.ontology.id

    log.info("check if object "+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/ontology/"+newIdOntology  +".json"
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

    log.info("check if object "+ newIdOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)*/

  }

  void testDeleteOntologyNotExist() {

     log.info("create ontology")
    def ontologyToDelete = BasicInstance.createOrGetBasicOntology()
    String jsonOntology = ontologyToDelete.encodeAsJSON()

    log.info("delete ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }

  void testDeleteOntologyWithProject() {

    log.info("create ontology")
    //create project and try to delete his ontology
    def project = BasicInstance.createOrGetBasicProject()
    def ontologyToDelete = project.ontology
    assert ontologyToDelete.save(flush:true)!=null
    String jsonOntology = ontologyToDelete.encodeAsJSON()
    int idOntology = ontologyToDelete.id
    log.info("delete ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testDeleteOntologyWithTerms() {

    log.info("create ontology")
    //create project and try to delete his ontology
    def relationTerm = BasicInstance.getBasicRelationTermNotExist()
    relationTerm.save(flush:true)
    def ontologyToDelete = relationTerm.term1.ontology
    assert ontologyToDelete.save(flush:true)!=null
    String jsonOntology = ontologyToDelete.encodeAsJSON()
    int idOntology = ontologyToDelete.id
    log.info("delete ontology:"+jsonOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/ontology/"+idOntology+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

  }
}
