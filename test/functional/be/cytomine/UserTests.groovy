package be.cytomine
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import be.cytomine.security.User
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class UserTests extends functionaltestplugin.FunctionalTestCase {

  void testListUserWithCredential() {

    log.info("get user")
    String URL = Infos.CYTOMINEURL+"api/user.json"
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

  void testListUserWithoutCredential() {

    log.info("get user")
    String URL = Infos.CYTOMINEURL+"api/user.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)
  }

  void testShowUserWithCredential() {

    log.info("create user")
    User user =  BasicInstance.createOrGetBasicUser()

    log.info("get user")
    String URL = Infos.CYTOMINEURL+"api/user/"+ user.id +".json"
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

  void testAddUserCorrect() {
    log.info("create user")
    def userToAdd = BasicInstance.getBasicUserNotExist()
    String jsonUser = ([user : userToAdd]).encodeAsJSON()

    log.info("post user:"+jsonUser.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/user.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonUser)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idUser = json.user.id

    log.info("check if object "+ idUser +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/user/"+idUser +".json"
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

    log.info("check if object "+ idUser +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/user/"+idUser +".json"
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
    idUser = json.user.id

    log.info("check if object "+ idUser +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/user/"+idUser +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)
  }

  void testAddUserWithoutPassword() {

    /*log.info("create user")
    def userToAdd = BasicInstance.getBasicUserNotExist()
    String jsonUser = ([user : userToAdd]).encodeAsJSON()

    def jsonUpdate = JSON.parse(jsonUser)
    //jsonUpdate.user.password = ""
    jsonUser = jsonUpdate.encodeAsJSON()

    log.info("post user:"+jsonUser.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/user.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonUser)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)*/

  }

  void testAddUserWithUsernameAlreadyExist() {

    log.info("create user")
    def userToAdd = BasicInstance.createOrGetBasicUser()
    String jsonUser = ([user : userToAdd]).encodeAsJSON()

    def jsonUpdate = JSON.parse(jsonUser)
    jsonUser = jsonUpdate.encodeAsJSON()

    log.info("post user:"+jsonUser.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/user.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonUser)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testAddUserWithEmailInvalid() {

    log.info("create user")
    def userToAdd = BasicInstance.getBasicUserNotExist()
    String jsonUser = ([user : userToAdd]).encodeAsJSON()

    def jsonUpdate = JSON.parse(jsonUser)
    jsonUpdate.user.email = "invalid@email"
    jsonUser = jsonUpdate.encodeAsJSON()

    log.info("post user:"+jsonUser.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/user.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonUser)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testEditUser() {

  }

  void testEditUserWithUsernameAlreadyExist() {

  }

  void testEditUserWithEmailInvalid() {

  }

  void testDeleteUser(){

  }

  void testDeleteUserNotExist() {

  }
}
