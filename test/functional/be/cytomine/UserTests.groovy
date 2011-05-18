package be.cytomine
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import be.cytomine.security.User
import org.codehaus.groovy.grails.web.json.JSONArray
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
    assert json instanceof JSONArray
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
    String jsonUser = userToAdd.encodeAsJSON()

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

    /*log.info("test undo")
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
    assertEquals(201,code)

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
    assertEquals(200,code)*/
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
    String jsonUser = userToAdd.encodeAsJSON()

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
    String jsonUser = userToAdd.encodeAsJSON()

    def jsonUpdate = JSON.parse(jsonUser)
    jsonUpdate.email = "invalid@email"
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

    String oldFirstname = "Firstname1"
    String newFirstname = "Firstname2"

    String oldLastname = "Lastname1"
    String newLastname = "Lastname2"

    String oldEmail = "old@email.com"
    String newEmail = "new@email.com"

    String oldUsername = "Username1"
    String newUsername = "Username2"


    def mapOld = ["firstname":oldFirstname,"lastname":oldLastname,"email":oldEmail,"username":oldUsername]
    def mapNew = ["firstname":newFirstname,"lastname":newLastname,"email":newEmail,"username":newUsername]


    /* Create a Name1 user */
    log.info("create user")
    User userToAdd = BasicInstance.createOrGetBasicUser()
    userToAdd.firstname = oldFirstname
    userToAdd.lastname = oldLastname
    userToAdd.email = oldEmail
    userToAdd.username = oldUsername
    assert (userToAdd.save(flush:true) != null)

    /* Encode a niew user Name2*/
    User userToEdit = User.get(userToAdd.id)
    def jsonUser = userToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonUser)
    jsonUpdate.firstname = newFirstname
    jsonUpdate.lastname = newLastname
    jsonUpdate.email = newEmail
    jsonUpdate.username = newUsername
    jsonUser = jsonUpdate.encodeAsJSON()

    log.info("put user:"+jsonUser.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/user/"+userToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonUser)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
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
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareUser(mapNew,json)

    /*log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idUser +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/user/"+idUser +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareUser(mapOld,json)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL + ".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idUser +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/user/"+idUser +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareUser(mapNew,json)


    log.info("check if object "+ idUser +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/user/"+idUser +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject */

  }

  void testEditUserWithUsernameAlreadyExist() {

    /* Create a Name1 user */
    log.info("create user")
    User userToAdd = BasicInstance.createOrGetBasicUser()

    /* Encode a niew user Name2*/
    User userToEdit = User.get(userToAdd.id)
    def jsonUser = userToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonUser)
    jsonUpdate.username = BasicInstance.getOldUser().username
    jsonUser = jsonUpdate.encodeAsJSON()

    log.info("put user:"+jsonUser.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/user/"+userToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonUser)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testEditUserWithEmailInvalid() {
     /* Create a Name1 user */
    log.info("create user")
    User userToAdd = BasicInstance.createOrGetBasicUser()

    /* Encode a niew user Name2*/
    User userToEdit = User.get(userToAdd.id)
    def jsonUser = userToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonUser)
    jsonUpdate.email = "badmail"
    jsonUser = jsonUpdate.encodeAsJSON()

    log.info("put user:"+jsonUser.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/user/"+userToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonUser)
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testDeleteUser() {

    log.info("create user")
    def userToDelete = BasicInstance.getBasicUserNotExist()
    assert userToDelete.save(flush:true) != null
    String jsonUser = userToDelete.encodeAsJSON()
    int idUser = userToDelete.id
    log.info("delete user:"+jsonUser.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/user/"+idUser+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idUser +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/user/"+idUser +".json"
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

    log.info("check if object "+ idUser +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/user/"+idUser  +".json"
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

    log.info("check if object "+ idUser +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/user/"+idUser +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code) */

  }

  void testDeleteUserWithData() {
    log.info("create user")
    def userToDelete = BasicInstance.createOrGetBasicUser()
    def image =  BasicInstance.createOrGetBasicImageInstance()
    image.user = userToDelete
    assert image.save(flush:true)!=null

    String jsonUser = userToDelete.encodeAsJSON()
    int idUser = userToDelete.id
    log.info("delete user:"+jsonUser.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/user/"+idUser+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testDeleteUserNotExist() {

     log.info("create user")
    def userToDelete = BasicInstance.createOrGetBasicUser()
    String jsonUser = userToDelete.encodeAsJSON()

    log.info("delete user:"+jsonUser.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/user/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }
}
