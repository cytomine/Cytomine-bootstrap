package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.project.Discipline

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class DisciplineTests extends functionaltestplugin.FunctionalTestCase {

  void testListDisciplineWithCredential() {

    log.info("list discipline")
    String URL = Infos.CYTOMINEURL+"api/discipline.json"
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

  void testListDisciplineWithoutCredential() {

    log.info("list discipline")
    String URL = Infos.CYTOMINEURL+"api/discipline.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)

  }

  void testShowDisciplineWithCredential() {

    Discipline discipline = BasicInstance.createOrGetBasicDiscipline()

    log.info("list discipline")
    String URL = Infos.CYTOMINEURL+"api/discipline/"+ discipline.id +".json"
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

  void testAddDisciplineCorrect() {

   log.info("create discipline")
    def disciplineToAdd = BasicInstance.getBasicDisciplineNotExist()
    println("disciplineToAdd.version="+disciplineToAdd.version)
    String jsonDiscipline = disciplineToAdd.encodeAsJSON()

    log.info("post discipline:"+jsonDiscipline.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/discipline.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonDiscipline)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idDiscipline = json.discipline.id

    log.info("check if object "+ idDiscipline +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline +".json"
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

    log.info("check if object "+ idDiscipline +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline +".json"
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

    log.info("check if object "+ idDiscipline +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

  void testAddDisciplineAlreadyExist() {
    log.info("create discipline")
    def disciplineToAdd = BasicInstance.createOrGetBasicDiscipline()
    //disciplineToAdd is save in DB
    String jsonDiscipline = disciplineToAdd.encodeAsJSON()

    log.info("post discipline:"+jsonDiscipline.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/discipline.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonDiscipline)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testAddDisciplineWithBadName() {
    log.info("create discipline")
    def disciplineToAdd = BasicInstance.getBasicDisciplineNotExist()
    String jsonDiscipline = disciplineToAdd.encodeAsJSON()

    def jsonUpdate = JSON.parse(jsonDiscipline)
    jsonUpdate.name = null
    jsonDiscipline = jsonUpdate.encodeAsJSON()

    log.info("post discipline:"+jsonDiscipline.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/discipline.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonDiscipline)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testUpdateDisciplineCorrect() {

    String oldName = "NAME1"
    String newName = "NAME2"

    def mapNew = ["name":newName]
    def mapOld = ["name":oldName]

    /* Create a Name1 discipline */
    log.info("create discipline")
    Discipline disciplineToAdd = BasicInstance.createOrGetBasicDiscipline()
    disciplineToAdd.name = oldName
    assert (disciplineToAdd.save(flush:true) != null)

    /* Encode a niew discipline Name2*/
    Discipline disciplineToEdit = Discipline.get(disciplineToAdd.id)
    def jsonDiscipline = disciplineToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonDiscipline)
    jsonUpdate.name = newName
    jsonDiscipline = jsonUpdate.encodeAsJSON()

    log.info("put discipline:"+jsonDiscipline.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/discipline/"+disciplineToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonDiscipline)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idDiscipline = json.discipline.id

    log.info("check if object "+ idDiscipline +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareDiscipline(mapNew,json)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idDiscipline +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareDiscipline(mapOld,json)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idDiscipline +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareDiscipline(mapNew,json)


    log.info("check if object "+ idDiscipline +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testUpdateDisciplineNotExist() {
    /* Create a Name1 discipline */
    log.info("create discipline")
    Discipline disciplineToAdd = BasicInstance.createOrGetBasicDiscipline()

    /* Encode a niew discipline Name2*/
    Discipline disciplineToEdit = Discipline.get(disciplineToAdd.id)
    def jsonDiscipline = disciplineToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonDiscipline)
    jsonUpdate.id = -99
    jsonDiscipline = jsonUpdate.encodeAsJSON()

    log.info("put discipline:"+jsonDiscipline.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/discipline/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonDiscipline)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }

  void testUpdateDisciplineWithNameAlreadyExist() {

    /* Create a Name1 discipline */
    log.info("create discipline")
    Discipline disciplineWithOldName = BasicInstance.createOrGetBasicDiscipline()
    Discipline disciplineWithNewName = BasicInstance.getBasicDisciplineNotExist()
    disciplineWithNewName.save(flush:true)


    /* Encode a niew discipline Name2*/
    Discipline disciplineToEdit = Discipline.get(disciplineWithNewName.id)
    log.info("disciplineToEdit="+disciplineToEdit)
    def jsonDiscipline = disciplineToEdit.encodeAsJSON()
    log.info("jsonDiscipline="+jsonDiscipline)
    def jsonUpdate = JSON.parse(jsonDiscipline)
    jsonUpdate.name = disciplineWithOldName.name
    jsonDiscipline = jsonUpdate.encodeAsJSON()

    log.info("put discipline:"+jsonDiscipline.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/discipline/"+disciplineToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonDiscipline)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testUpdateDisciplineWithBadName() {

    /* Create a Name1 discipline */
    log.info("create discipline")
    Discipline disciplineToAdd = BasicInstance.createOrGetBasicDiscipline()

    /* Encode a niew discipline Name2*/
    Discipline disciplineToEdit = Discipline.get(disciplineToAdd.id)
    def jsonDiscipline = disciplineToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonDiscipline)
    jsonUpdate.name = null
    jsonDiscipline = jsonUpdate.encodeAsJSON()

    log.info("put discipline:"+jsonDiscipline.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/discipline/"+disciplineToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonDiscipline)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testDeleteDiscipline() {

    log.info("create discipline")
    def disciplineToDelete = BasicInstance.getBasicDisciplineNotExist()
    assert disciplineToDelete.save(flush:true)!=null
    String jsonDiscipline = disciplineToDelete.encodeAsJSON()
    int idDiscipline = disciplineToDelete.id
    log.info("delete discipline:"+jsonDiscipline.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idDiscipline +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline +".json"
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

    log.info("check if object "+ idDiscipline +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline  +".json"
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

    log.info("check if object "+ idDiscipline +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteDisciplineNotExist() {

     log.info("create discipline")
    def disciplineToDelete = BasicInstance.createOrGetBasicDiscipline()
    String jsonDiscipline = disciplineToDelete.encodeAsJSON()

    log.info("delete discipline:"+jsonDiscipline.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/discipline/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }

  void testDeleteDisciplineWithProject() {

    log.info("create discipline")
    //create project and try to delete his discipline
    def project = BasicInstance.createOrGetBasicProject()
    def disciplineToDelete = project.discipline
    assert disciplineToDelete.save(flush:true)!=null
    String jsonDiscipline = disciplineToDelete.encodeAsJSON()
    int idDiscipline = disciplineToDelete.id
    log.info("delete discipline:"+jsonDiscipline.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/discipline/"+idDiscipline+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }


}
