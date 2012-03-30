package be.cytomine

import be.cytomine.processing.Software
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.SoftwareParameter

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class SoftwareParameterTests extends functionaltestplugin.FunctionalTestCase {

  void testListSoftwareParameterWithCredential() {

    log.info("list softwareparameter")
    String URL = Infos.CYTOMINEURL+"api/softwareparameter.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response="+response)
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONArray

  }

  void testListSoftwareParameterBySoftwareWithCredential() {
    Software software = BasicInstance.createOrGetBasicSoftware()
    log.info("list softwareparameter")
    String URL = Infos.CYTOMINEURL+"api/software/$software.id/parameter.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response="+response)
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONArray

  }

  void testShowSoftwareparameterWithCredential() {

    SoftwareParameter softwareparameter = BasicInstance.createOrGetBasicSoftwareParameter()

    log.info("list softwareparameter")
    String URL = Infos.CYTOMINEURL+"api/softwareparameter/"+ softwareparameter.id +".json"
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

  void testAddSoftwareParameterCorrect() {

   log.info("create softwareparameter")
    def softwareparameterToAdd = BasicInstance.getBasicSoftwareParameterNotExist()
    println("softwareparameterToAdd.version="+softwareparameterToAdd.version)
    String jsonSoftwareparameter = softwareparameterToAdd.encodeAsJSON()

    log.info("post softwareparameter:"+jsonSoftwareparameter.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareparameter.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonSoftwareparameter)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idSoftwareparameter = json.softwareparameter.id

    log.info("check if object "+ idSoftwareparameter +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareparameter +".json"
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

    log.info("check if object "+ idSoftwareparameter +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareparameter +".json"
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

    log.info("check if object "+ idSoftwareparameter +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareparameter +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

  void testAddSoftwareParameterAlreadyExist() {
    log.info("create softwareparameter")
    def softwareparameterToAdd = BasicInstance.createOrGetBasicSoftwareParameter()
    //softwareparameterToAdd is save in DB
    String jsonSoftwareparameter = softwareparameterToAdd.encodeAsJSON()

    log.info("post softwareparameter:"+jsonSoftwareparameter.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareparameter.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonSoftwareparameter)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(409,code)
  }

  void testAddSoftwareParameterWithBadName() {
    log.info("create softwareparameter")
    def softwareparameterToAdd = BasicInstance.createOrGetBasicSoftwareParameter()
    String jsonSoftwareparameter = softwareparameterToAdd.encodeAsJSON()

    def jsonUpdate = JSON.parse(jsonSoftwareparameter)
    jsonUpdate.name = null
    jsonSoftwareparameter = jsonUpdate.encodeAsJSON()

    log.info("post softwareparameter:"+jsonSoftwareparameter.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareparameter.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonSoftwareparameter)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

//  void testUpdateSoftwareParameterCorrect() {
//
//    String oldName = "Name1"
//    String newName = Math.random()+""
//
//    def mapNew = ["name":newName]
//    def mapOld = ["name":oldName]
//
//    /* Create a Name1 softwareparameter */
//    log.info("create softwareparameter")
//    SoftwareParameter softwareparameterToAdd = BasicInstance.createOrGetBasicSoftwareParameter()
//    softwareparameterToAdd.name = oldName
//    assert (softwareparameterToAdd.save(flush:true) != null)
//
//    /* Encode a niew softwareparameter Name2*/
//    SoftwareParameter softwareparameterToEdit = SoftwareParameter.get(softwareparameterToAdd.id)
//    def jsonSoftwareParameter = softwareparameterToEdit.encodeAsJSON()
//    def jsonUpdate = JSON.parse(jsonSoftwareParameter)
//    jsonUpdate.name = newName
//    jsonSoftwareParameter = jsonUpdate.encodeAsJSON()
//
//    log.info("put softwareparameter:"+jsonSoftwareParameter.replace("\n",""))
//    String URL = Infos.CYTOMINEURL+"api/softwareparameter/"+softwareparameterToEdit.id+".json"
//    HttpClient client = new HttpClient()
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//    client.put(jsonSoftwareParameter)
//    int code  = client.getResponseCode()
//    String response = client.getResponseData()
//    println response
//    client.disconnect();
//
//    log.info("check response")
//    assertEquals(200,code)
//    def json = JSON.parse(response)
//    assert json instanceof JSONObject
//    int idSoftwareParameter = json.softwareparameter.id
//
//    log.info("check if object "+ idSoftwareParameter +" exist in DB")
//    client = new HttpClient();
//    URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareParameter +".json"
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
//    client.get()
//    code  = client.getResponseCode()
//    response = client.getResponseData()
//    client.disconnect();
//
//    assertEquals(200,code)
//    json = JSON.parse(response)
//    assert json instanceof JSONObject
//
//    BasicInstance.compareSoftwareParameter(mapNew,json)
//
//    log.info("test undo")
//    client = new HttpClient()
//    URL = Infos.CYTOMINEURL+Infos.UNDOURL + ".json"
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//    client.get()
//    code  = client.getResponseCode()
//    response = client.getResponseData()
//    client.disconnect();
//    assertEquals(200,code)
//
//    log.info("check if object "+ idSoftwareParameter +" exist in DB")
//    client = new HttpClient();
//    URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareParameter +".json"
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
//    client.get()
//    code  = client.getResponseCode()
//    response = client.getResponseData()
//    client.disconnect();
//
//    assertEquals(200,code)
//    json = JSON.parse(response)
//    assert json instanceof JSONObject
//
//    BasicInstance.compareSoftwareParameter(mapOld,json)
//
//    log.info("test redo")
//    client = new HttpClient()
//    URL = Infos.CYTOMINEURL+Infos.REDOURL + ".json"
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//    client.get()
//    code  = client.getResponseCode()
//    response = client.getResponseData()
//    client.disconnect();
//    assertEquals(200,code)
//
//    log.info("check if object "+ idSoftwareParameter +" exist in DB")
//    client = new HttpClient();
//    URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareParameter +".json"
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
//    client.get()
//    code  = client.getResponseCode()
//    response = client.getResponseData()
//    client.disconnect();
//
//    assertEquals(200,code)
//    json = JSON.parse(response)
//    assert json instanceof JSONObject
//
//    BasicInstance.compareSoftwareParameter(mapNew,json)
//
//
//    log.info("check if object "+ idSoftwareParameter +" exist in DB")
//    client = new HttpClient();
//    URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareParameter +".json"
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
//    client.get()
//    code  = client.getResponseCode()
//    response = client.getResponseData()
//    client.disconnect();
//
//    assertEquals(200,code)
//    json = JSON.parse(response)
//    assert json instanceof JSONObject
//
//  }

  void testUpdateSoftwareParameterNotExist() {
    /* Create a Name1 softwareparameter */
    log.info("create softwareparameter")
    SoftwareParameter softwareparameterToAdd = BasicInstance.createOrGetBasicSoftwareParameter()

    /* Encode a niew softwareparameter Name2*/
    SoftwareParameter softwareparameterToEdit = SoftwareParameter.get(softwareparameterToAdd.id)
    def jsonSoftwareParameter = softwareparameterToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonSoftwareParameter)
    jsonUpdate.id = -99
    jsonSoftwareParameter = jsonUpdate.encodeAsJSON()

    log.info("put softwareparameter:"+jsonSoftwareParameter.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareparameter/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonSoftwareParameter)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }

  void testUpdateSoftwareParameterWithNameAlreadyExist() {

    /* Create a Name1 softwareparameter */
    log.info("create softwareparameter")
    SoftwareParameter softwareparameterWithOldName = BasicInstance.createOrGetBasicSoftwareParameter()
    SoftwareParameter softwareparameterWithNewName = BasicInstance.getBasicSoftwareParameterNotExist()
    softwareparameterWithNewName.save(flush:true)


    /* Encode a niew softwareparameter Name2*/
    SoftwareParameter softwareparameterToEdit = SoftwareParameter.get(softwareparameterWithNewName.id)
    log.info("softwareparameterToEdit="+softwareparameterToEdit)
    def jsonSoftwareParameter = softwareparameterToEdit.encodeAsJSON()
    log.info("jsonSoftwareParameter="+jsonSoftwareParameter)
    def jsonUpdate = JSON.parse(jsonSoftwareParameter)
    jsonUpdate.name = softwareparameterWithOldName.name
    jsonSoftwareParameter = jsonUpdate.encodeAsJSON()

    log.info("put softwareparameter:"+jsonSoftwareParameter.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareparameter/"+softwareparameterToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonSoftwareParameter)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(409,code)

  }

  void testUpdateSoftwareParameterWithBadName() {

    /* Create a Name1 softwareparameter */
    log.info("create softwareparameter")
    SoftwareParameter softwareparameterToAdd = BasicInstance.createOrGetBasicSoftwareParameter()

    /* Encode a niew softwareparameter Name2*/
    SoftwareParameter softwareparameterToEdit = SoftwareParameter.get(softwareparameterToAdd.id)
    def jsonSoftwareParameter = softwareparameterToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonSoftwareParameter)
    jsonUpdate.name = null
    jsonSoftwareParameter = jsonUpdate.encodeAsJSON()

    log.info("put softwareparameter:"+jsonSoftwareParameter.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareparameter/"+softwareparameterToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonSoftwareParameter)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testDeleteSoftwareParameter() {

    log.info("create softwareparameter")
    def softwareparameterToDelete = BasicInstance.getBasicSoftwareParameterNotExist()
    assert softwareparameterToDelete.save(flush:true)!=null
    String jsonSoftwareParameter = softwareparameterToDelete.encodeAsJSON()
    int idSoftwareParameter = softwareparameterToDelete.id
    log.info("delete softwareparameter:"+jsonSoftwareParameter.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareParameter+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idSoftwareParameter +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareParameter +".json"
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

    log.info("check if object "+ idSoftwareParameter +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareParameter  +".json"
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

    log.info("check if object "+ idSoftwareParameter +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/softwareparameter/"+idSoftwareParameter +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteSoftwareParameterNotExist() {

     log.info("create softwareparameter")
    def softwareparameterToDelete = BasicInstance.createOrGetBasicSoftwareParameter()
    String jsonSoftwareParameter = softwareparameterToDelete.encodeAsJSON()

    log.info("delete softwareparameter:"+jsonSoftwareParameter.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/softwareparameter/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }
}
