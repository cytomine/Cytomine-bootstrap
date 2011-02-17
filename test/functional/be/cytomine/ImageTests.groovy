package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.project.Image
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 13:49
 * To change this template use File | Settings | File Templates.
 */
class ImageTests extends functionaltestplugin.FunctionalTestCase{

  void testGetImagesWithCredential() {

    log.info("create annotation")
    //Image image =  BasicInstance.createOrGetBasicImage()

    log.info("get annotation")
    String URL = Infos.CYTOMINEURL+"api/image.json"
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

  void testGetImagesWithoutCredential() {

    log.info("create annotation")
    Image image =  BasicInstance.createOrGetBasicImage()

    log.info("get annotation")
    String URL = Infos.CYTOMINEURL+"api/annotation.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)

  }

   void testGetImageWithCredential() {

    log.info("create annotation")
    Image image =  BasicInstance.createOrGetBasicImage()

    log.info("get annotation")
    String URL = Infos.CYTOMINEURL+"api/image/"+ image.id +".json"
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

  void testaddImageCorrect() {

    log.info("create image")
    def imageToAdd = BasicInstance.createOrGetBasicImage()
    String jsonImage = ([image : imageToAdd]).encodeAsJSON()

    log.info("post image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idImage = json.image.id

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idImage +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(404,code)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    //must be done because redo change id
    json = JSON.parse(response)
    assert json instanceof JSONObject
    idImage = json.image.id

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

  }

  void testaddImageWithUnexistingImageServer() {


  }
}
