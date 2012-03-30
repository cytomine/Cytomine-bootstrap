package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.image.AbstractImage
import be.cytomine.security.Group

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 22/02/11
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
class AbstractImageGroupTests extends functionaltestplugin.FunctionalTestCase {


  void testListAbstractImageGroupByAbstractImageWithCredential() {

    AbstractImage abstractimage = BasicInstance.createOrGetBasicAbstractImage()

    log.info("get by abstractimage")
    String URL = Infos.CYTOMINEURL+"api/image/"+abstractimage.id+"/group.json"
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

  void testListAbstractImageGroupByAbstractImageWithAbstractImageNotExist() {

    AbstractImage abstractimage = BasicInstance.createOrGetBasicAbstractImage()

    log.info("get by abstractimage not exist")
    String URL = Infos.CYTOMINEURL+"api/image/-99/group.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
    def json = JSON.parse(response)

  }

  void testListAbstractImageGroupByGroupWithCredential() {

    Group group = BasicInstance.createOrGetBasicGroup()

    log.info("get by group")
    String URL = Infos.CYTOMINEURL+"api/group/"+group.id+"/image.json"
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

  /*void testListAbstractImageGroupByCurrentUser() {

    log.info("get by group")
    String URL = Infos.CYTOMINEURL+"api/currentuser/image.json"
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
  }*/

  void testListAbstractImageGroupByGroupWithAbstractImageNotExist() {

    AbstractImage abstractimage = BasicInstance.createOrGetBasicAbstractImage()

    log.info("get by group not exist")
    String URL = Infos.CYTOMINEURL+"api/group/-99/image.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
    def json = JSON.parse(response)

  }

  void testGetAbstractImageGroupWithCredential() {

    def abstractimageGroupToAdd = BasicInstance.createOrGetBasicAbstractImageGroup()

    log.info("get abstractimage")
    String URL = Infos.CYTOMINEURL+"api/image/"+ abstractimageGroupToAdd.abstractimage.id +"/group/"+abstractimageGroupToAdd.group.id +".json"
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

  void testAddAbstractImageGroupCorrect() {

    log.info("create AbstractImageGroup")
    def abstractimageGroupToAdd = BasicInstance.getBasicAbstractImageGroupNotExist("testAddAbstractImageGroupCorrect")
    abstractimageGroupToAdd.discard()
    String jsonAbstractImageGroup = abstractimageGroupToAdd.encodeAsJSON()

    log.info("post abstractimageGroup:"+jsonAbstractImageGroup.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+ abstractimageGroupToAdd.abstractimage.id +"/group/"+ abstractimageGroupToAdd.group.id +".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAbstractImageGroup)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idAbstractImage= json.abstractimagegroup.abstractimage
    int idGroup= json.abstractimagegroup.group

    log.info("check if object "+ idAbstractImage +"/"+ idGroup +"exist in DB")
    client = new HttpClient();

    URL = Infos.CYTOMINEURL+"api/image/"+idAbstractImage+"/group/"+idGroup +".json"
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

    log.info("check if object "+ idAbstractImage +"/"+ idGroup +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+ idAbstractImage +"/group/"+idGroup +".json"
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
    //assert json instanceof JSONObject

    log.info("check if object "+ idAbstractImage +"/"+ idGroup +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+ idAbstractImage +"/group/"+idGroup +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

   void testAddAbstractImageGroupAlreadyExist() {

    log.info("create AbstractImageGroup")
    def abstractimageGroupToAdd = BasicInstance.getBasicAbstractImageGroupNotExist("testAddAbstractImageGroupAlreadyExist")
    abstractimageGroupToAdd.save(flush:true)
    //abstractimageGroupToAdd is in database, we will try to add it twice
    String jsonAbstractImageGroup = abstractimageGroupToAdd.encodeAsJSON()

    log.info("post abstractimageGroup:"+jsonAbstractImageGroup.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+ abstractimageGroupToAdd.abstractimage.id +"/group/"+ abstractimageGroupToAdd.group.id +".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAbstractImageGroup)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(409,code)
  }
  void testAddAbstractImageGroupWithAbstractImageNotExist() {

    log.info("create abstractimagegroup")
    def abstractimageGroupAdd = BasicInstance.getBasicAbstractImageGroupNotExist("testAddAbstractImageGroupWithAbstractImageNotExist")
    String jsonAbstractImageGroup = abstractimageGroupAdd.encodeAsJSON()
    log.info("jsonAbstractImageGroup="+jsonAbstractImageGroup)
    def jsonUpdate = JSON.parse(jsonAbstractImageGroup)
    jsonUpdate.abstractimage = -99
    jsonAbstractImageGroup = jsonUpdate.encodeAsJSON()

    log.info("post abstractimagegroup:"+jsonAbstractImageGroup.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/-99/group/" + abstractimageGroupAdd.group.id  + ".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAbstractImageGroup)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testAddAbstractImageGroupWithGroupNotExist() {

    log.info("create abstractimagegroup")
    def abstractimageGroupAdd = BasicInstance.getBasicAbstractImageGroupNotExist("testAddAbstractImageGroupWithGroupNotExist")
    String jsonAbstractImageGroup = abstractimageGroupAdd.encodeAsJSON()
    log.info("jsonAbstractImageGroup="+jsonAbstractImageGroup)
    def jsonUpdate = JSON.parse(jsonAbstractImageGroup)
    jsonUpdate.group = -99
    jsonAbstractImageGroup = jsonUpdate.encodeAsJSON()

    log.info("post abstractimagegroup:"+jsonAbstractImageGroup.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+ abstractimageGroupAdd.abstractimage.id +"/group/"+ abstractimageGroupAdd.group.id +".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAbstractImageGroup)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testDeleteAbstractImageGroup() {

    log.info("create abstractimageGroup")
    def abstractimageGroupToDelete = BasicInstance.createOrGetBasicAbstractImageGroup()
    String jsonAbstractImageGroup = abstractimageGroupToDelete.encodeAsJSON()

    int idAbstractImage = abstractimageGroupToDelete.abstractimage.id
    int idGroup = abstractimageGroupToDelete.group.id
    log.info("delete abstractimageGroup:"+jsonAbstractImageGroup.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+abstractimageGroupToDelete.abstractimage.id + "/group/"+abstractimageGroupToDelete.group.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idAbstractImage +"/" + idGroup + " exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idAbstractImage + "/group/"+idGroup+".json"
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
    def json = JSON.parse(response)
    //assert json instanceof JSONObject
    //int newIdAbstractImageGroup  = json.abstractimagegroup.id

    log.info("check if object "+ idAbstractImage +"/" + idGroup +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idAbstractImage + "/group/"+idGroup+".json"
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

    log.info("check if object "+ idAbstractImage +"/" + idGroup +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idAbstractImage + "/group/"+idGroup+".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)

  }

  void testDeleteAbstractImageGroupNotExist() {

     log.info("create project")
    def abstractimageGroupToDelete = BasicInstance.createOrGetBasicAbstractImageGroup()
    String jsonAbstractImageGroup = abstractimageGroupToDelete.encodeAsJSON()

    log.info("delete abstractimageGroup:"+jsonAbstractImageGroup.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/-99/group/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }
}
