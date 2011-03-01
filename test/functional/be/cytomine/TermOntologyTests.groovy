package be.cytomine

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.project.TermOntology
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import be.cytomine.project.Term
import be.cytomine.project.Ontology
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 1/03/11
 * Time: 9:33
 * To change this template use File | Settings | File Templates.
 */
class TermOntologyTests extends functionaltestplugin.FunctionalTestCase {

  void testListOntologyTermByOntologyWithCredential() {

    Ontology ontology = BasicInstance.createOrGetBasicOntology()
    Term term = BasicInstance.createOrGetBasicTerm()
    try {TermOntology.link(term,ontology) } catch(Exception e) { log.info e.toString()}

    log.info("get by ontology")
    String URL = Infos.CYTOMINEURL+"api/ontology/"+ontology.id+"/term.json"
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

  void testListTermOntologyByOntologyWithOntologyNotExist() {

    log.info("get by ontology not exist")
    String URL = Infos.CYTOMINEURL+"api/ontology/-99/term.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testListTermOntologyByTermWithCredential() {

    Ontology ontology = BasicInstance.createOrGetBasicOntology()
    Term term = BasicInstance.createOrGetBasicTerm()
    try {TermOntology.link(term,ontology) } catch(Exception e) { log.info e.toString()}

    log.info("get by term")
    String URL = Infos.CYTOMINEURL+"api/term/"+term.id+"/ontology.json"
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

  void testListTermOntologyByTermWithTermNotExist() {

    log.info("get by term not exist")
    String URL = Infos.CYTOMINEURL+"api/term/-99/ontology.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }


  void testGetTermOntologyWithCredential() {

    def termOntologyToAdd = BasicInstance.createOrGetBasicTermOntology()

    log.info("get ontology/term")
    String URL = Infos.CYTOMINEURL+"api/term/"+ termOntologyToAdd.term.id +"/ontology/"+termOntologyToAdd.ontology.id +".json"
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


  void testAddTermOntologyCorrect() {

    log.info("create TermOntology")
    def termOntologyToAdd = BasicInstance.getBasicTermOntologyNotExist("testAddTermOntologyCorrect")
    termOntologyToAdd.discard()
    String jsonTermOntology = ([termOntology : termOntologyToAdd]).encodeAsJSON()

    log.info("post termOntology:"+jsonTermOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/"+ termOntologyToAdd.term.id +"/ontology.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonTermOntology)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idOntology= json.termOntology.ontology.id
    int idTerm= json.termOntology.term.id

    log.info("check if object "+ idOntology +"/"+ idTerm +"exist in DB")
    client = new HttpClient();

    URL = Infos.CYTOMINEURL+"api/term/"+idTerm+"/ontology/"+idOntology +".json"
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

    log.info("check if object "+ idTerm +"/"+ idOntology +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm+"/ontology/"+idOntology +".json"
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

    log.info("check if object "+ idTerm +"/"+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm+"/ontology/"+idOntology +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

   void testAddTermOntologyAlreadyExist() {

    log.info("create TermOntology")
    def termOntologyToAdd = BasicInstance.getBasicTermOntologyNotExist("testAddTermOntologyAlreadyExist")
    termOntologyToAdd.save(flush:true)
    //termOntologyToAdd is in database, we will try to add it twice
    String jsonTermOntology = ([termOntology : termOntologyToAdd]).encodeAsJSON()

    log.info("post termOntology:"+jsonTermOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/"+ termOntologyToAdd.term.id +"/ontology.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonTermOntology)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }
  void testAddTermOntologyWithTermNotExist() {

    log.info("create termontology")
    def termOntologyAdd = BasicInstance.getBasicTermOntologyNotExist("testAddTermOntologyWithTermNotExist")
    String jsonTermOntology = ([termOntology : termOntologyAdd]).encodeAsJSON()
    log.info("jsonTermOntology="+jsonTermOntology)
    def jsonUpdate = JSON.parse(jsonTermOntology)
    jsonUpdate.termOntology.term.id = -99
    jsonTermOntology = jsonUpdate.encodeAsJSON()

    log.info("post termontology:"+jsonTermOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/-99/ontology.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonTermOntology)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testAddTermOntologyWithOntologyNotExist() {

    log.info("create atermontology")
    def termOntologyAdd = BasicInstance.getBasicTermOntologyNotExist("testAddTermOntologyWithTermNotExist")
    String jsonTermOntology = ([termOntology : termOntologyAdd]).encodeAsJSON()
    log.info("jsonTermOntology="+jsonTermOntology)
    def jsonUpdate = JSON.parse(jsonTermOntology)
    jsonUpdate.termOntology.ontology.id = -99
    jsonTermOntology = jsonUpdate.encodeAsJSON()

    log.info("post termontology:"+jsonTermOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/" + termOntologyAdd.term.id +"/ontology.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonTermOntology)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }







  void testEditTermOntology() {

    String oldColor = "FF0000"
    String newColor = "00FF00"

    def mapNew = ["color":newColor]
    def mapOld = ["color":oldColor]



    /* Create a old termontology with color FF0000 */
    log.info("create termontology")
    TermOntology termOntologyAdd = BasicInstance.createOrGetBasicTermOntology()
    termOntologyAdd.color = oldColor
    assert (termOntologyAdd.save(flush:true) != null)

    /* Encode a niew termontology with point 00FF00 */
    TermOntology termOntologyToEdit = TermOntology.get(termOntologyAdd.id)
    def jsonEdit = [termOntology : termOntologyToEdit]
    def jsonTermOntology = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonTermOntology)
    jsonUpdate.termOntology.color = newColor
    jsonTermOntology = jsonUpdate.encodeAsJSON()

    log.info("put termontology:"+jsonTermOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/"+termOntologyToEdit.term.id+"/ontology/"+ termOntologyToEdit.ontology.id +".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonTermOntology)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idTermOntology = json.termOntology.id

    log.info("check if object "+ idTermOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+termOntologyToEdit.term.id+"/ontology/"+ termOntologyToEdit.ontology.id +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareTermOntology(mapNew,json)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idTermOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+termOntologyToEdit.term.id+"/ontology/"+ termOntologyToEdit.ontology.id +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareTermOntology(mapOld,json)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idTermOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+termOntologyToEdit.term.id+"/ontology/"+ termOntologyToEdit.ontology.id +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject


    BasicInstance.compareTermOntology(mapNew,json)


    log.info("check if object "+ idTermOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+termOntologyToEdit.term.id+"/ontology/"+ termOntologyToEdit.ontology.id +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

  }


  void testEditTermOntologyNotExist() {

    String oldColor = "FF0000"
    String newColor = "00FF00"

    def mapNew = ["color":oldColor]
    def mapOld = ["color":newColor]



    /* Create a old termontology with color FF0000 */
    log.info("create termontology")
    TermOntology termOntologyAdd = BasicInstance.createOrGetBasicTermOntology()
    termOntologyAdd.color = oldColor
    assert (termOntologyAdd.save(flush:true) != null)

    /* Encode a niew termontology with point 00FF00 */
    TermOntology termOntologyToEdit = TermOntology.get(termOntologyAdd.id)
    def jsonEdit = [termOntology : termOntologyToEdit]
    def jsonTermOntology = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonTermOntology)
    jsonUpdate.termOntology.location = newColor
    jsonUpdate.termOntology.term.id = -99
    jsonTermOntology = jsonUpdate.encodeAsJSON()

    log.info("put termontology:"+jsonTermOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/-99/ontology/"+ termOntologyToEdit.ontology.id +".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonTermOntology)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }




























  void testDeleteTermOntology() {

    log.info("create termOntology")
    def termOntologyToDelete = BasicInstance.createOrGetBasicTermOntology()
    String jsonTermOntology = ([termOntology : termOntologyToDelete]).encodeAsJSON()

    int idOntology = termOntologyToDelete.ontology.id
    int idTerm = termOntologyToDelete.term.id
    log.info("delete termOntology:"+jsonTermOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/"+termOntologyToDelete.term.id + "/ontology/"+termOntologyToDelete.ontology.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(204,code)

    log.info("check if object "+ idTerm +"/"+ idOntology + " exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm + "/ontology/"+idOntology+".json"
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
    //int newIdTermOntology  = json.termOntology.id

    log.info("check if object "+ idTerm +"/"+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm + "/ontology/"+idOntology+".json"
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

    log.info("check if object "+ idTerm +"/"+ idOntology +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/term/"+idTerm + "/ontology/"+idOntology+".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteTermOntologyNotExist() {

     log.info("create project")
    def termOntologyToDelete = BasicInstance.createOrGetBasicTermOntology()
    String jsonTermOntology = ([termOntology : termOntologyToDelete]).encodeAsJSON()

    log.info("delete termOntology:"+jsonTermOntology.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/term/-99/ontology/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }
}