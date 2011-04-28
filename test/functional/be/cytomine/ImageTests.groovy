package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.image.Image
import be.cytomine.image.acquisition.Scanner
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.security.User
import be.cytomine.project.Slide
import be.cytomine.image.Mime
import com.vividsolutions.jts.io.WKTReader
import org.codehaus.groovy.grails.web.json.JSONArray

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
    assert json instanceof JSONArray

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

  void testListAnnotationsByUserWithCredential() {

    log.info("create annotation")
    Image image =  BasicInstance.createOrGetBasicImage()
    User user = BasicInstance.createOrGetBasicUser()

    log.info("get annotation")
    String URL = Infos.CYTOMINEURL+"api/user/"+user.id+"/image.json"
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

  void testListAnnotationsByUserNoExistWithCredential() {

    log.info("create annotation")
    Image image =  BasicInstance.createOrGetBasicImage()

    log.info("get annotation")
    String URL = Infos.CYTOMINEURL+"api/user/-99/image.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

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

  void testAddImageCorrect() {

    log.info("create image")
    def imageToAdd = BasicInstance.createOrGetBasicImage()
    String jsonImage = imageToAdd.encodeAsJSON()

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
    assertEquals(201,code)

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

  void testaddImageWithBadGeometry() {

    log.info("create image")
    def imageToAdd = BasicInstance.createOrGetBasicImage()
    String jsonImage = imageToAdd.encodeAsJSON()
    def updateImage = JSON.parse(jsonImage)
    updateImage.roi = 'POINT(BAD GEOMETRY)'
    jsonImage = updateImage.encodeAsJSON()

    log.info("post image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testaddImageWithUnexistingScanner() {

    log.info("create image")
    def imageToAdd = BasicInstance.createOrGetBasicImage()
    String jsonImage = imageToAdd.encodeAsJSON()
    def updateImage = JSON.parse(jsonImage)
    updateImage.scanner = -99
    jsonImage = updateImage.encodeAsJSON()

    log.info("post image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testaddImageWithUnexistingSlide() {

    log.info("create image")
    def imageToAdd = BasicInstance.createOrGetBasicImage()
    String jsonImage = imageToAdd.encodeAsJSON()
    def updateImage = JSON.parse(jsonImage)
    updateImage.slide = -99
    jsonImage = updateImage.encodeAsJSON()

    log.info("post image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testaddImageWithUnexistingMime() {

    log.info("create image")
    def imageToAdd = BasicInstance.createOrGetBasicImage()
    String jsonImage = imageToAdd.encodeAsJSON()
    def updateImage = JSON.parse(jsonImage)
    updateImage.mime = -99
    jsonImage = updateImage.encodeAsJSON()

    log.info("post image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testaddImageWithUnexistingImageServer() {

    log.info("create image")

    def mimeToAdd = BasicInstance.getBasicMimeNotExist()

    def imageToAdd = BasicInstance.createOrGetBasicImage()


    String jsonImage = imageToAdd.encodeAsJSON()
    def updateImage = JSON.parse(jsonImage)
    updateImage.mime = mimeToAdd.id
    jsonImage = updateImage.encodeAsJSON()

    log.info("post image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }


  void testEditImage() {

    String oldFilename = "oldName"
    String newFilename = "newName"

    String oldGeom = "POINT (1111 1111)"
    String newGeom = "POINT (9999 9999)"

    User oldUser = BasicInstance.getOldUser()
    User newUser = BasicInstance.getNewUser()

    Scanner oldScanner = BasicInstance.createOrGetBasicScanner()
    Scanner newScanner = BasicInstance.getNewScannerNotExist()

    Slide oldSlide = BasicInstance.createOrGetBasicSlide()
    Slide newSlide = BasicInstance.getBasicSlideNotExist()

    String oldPath = "oldPath"
    String newPath = "newPath"

    Mime oldMime = BasicInstance.createOrGetBasicMime() //TODO: replace by a mime different with image server
    Mime newMime = BasicInstance.createOrGetBasicMime()  //jp2

    Integer oldWidth = 1000
    Integer newWidth = 9000

    Integer oldHeight = 10000
    Integer newHeight = 900000

    Double oldScale = 1
    Double newScale = 9


    def mapNew = ["filename":newFilename,"geom":newGeom,"user":newUser,"scanner":newScanner,"slide":newSlide,"path":newPath,"mime":newMime,"width":newWidth,"height":newHeight,"scale":newScale]
    def mapOld = ["filename":oldFilename,"geom":oldGeom,"user":oldUser,"scanner":oldScanner,"slide":oldSlide,"path":oldPath,"mime":oldMime,"width":oldWidth,"height":oldHeight,"scale":oldScale]



    /* Create a old image */
    log.info("create image")
    Image imageToAdd = BasicInstance.createOrGetBasicImage()
    imageToAdd.filename = oldFilename
    imageToAdd.roi = new WKTReader().read(oldGeom)
    imageToAdd.user = oldUser
    imageToAdd.scanner = oldScanner
    imageToAdd.slide = oldSlide
    imageToAdd.path = oldPath
    imageToAdd.mime = oldMime
    imageToAdd.width = oldWidth
    imageToAdd.height = oldHeight
    imageToAdd.scale = oldScale
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    Image imageToEdit = Image.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)

    jsonUpdate.filename = newFilename
    jsonUpdate.roi = newGeom
    jsonUpdate.user = newUser.id
    jsonUpdate.scanner = newScanner.id
    jsonUpdate.slide = newSlide.id
    jsonUpdate.path = newPath
    jsonUpdate.mime = newMime.extension
    jsonUpdate.width = newWidth
    jsonUpdate.height = newHeight
    jsonUpdate.scale = newScale

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
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
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareImage(mapNew,json)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareImage(mapOld,json)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareImage(mapNew,json)

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testEditImageWithBadGeom()
  {
    String oldGeom = "POINT (1111 1111)"
    String newGeom = "BAD GEOMETRY"

    /* Create a old image */
    log.info("create image")
    Image imageToAdd = BasicInstance.createOrGetBasicImage()
    imageToAdd.roi = new WKTReader().read(oldGeom)
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    Image imageToEdit = Image.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)
    jsonUpdate.roi = newGeom

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testEditImageWithBadSlide()
  {
    Slide oldSlide = BasicInstance.createOrGetBasicSlide()
    Slide newSlide = BasicInstance.getBasicSlideNotExist()

    /* Create a old image */
    log.info("create image")
    Image imageToAdd = BasicInstance.createOrGetBasicImage()
    imageToAdd.slide = oldSlide
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    Image imageToEdit = Image.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)

    jsonUpdate.slide = -99

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testEditImageWithBadMime()
  {
    Mime oldMime = BasicInstance.createOrGetBasicMime() //TODO: replace by a mime different with image server
    Mime newMime = BasicInstance.createOrGetBasicMime()  //jp2

    /* Create a old image */
    log.info("create image")
    Image imageToAdd = BasicInstance.createOrGetBasicImage()
    imageToAdd.mime = oldMime
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    Image imageToEdit = Image.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)
    jsonUpdate.mime = -99

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testEditImageWithBadScanner()
  {
    Scanner oldScanner = BasicInstance.createOrGetBasicScanner()

    /* Create a old image */
    log.info("create image")
    Image imageToAdd = BasicInstance.createOrGetBasicImage()
    imageToAdd.scanner = oldScanner
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    Image imageToEdit = Image.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)

    jsonUpdate.scanner = -99

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testDeleteImage()
  {

    log.info("create image")
    def imageToDelete = BasicInstance.getBasicImageNotExist()
    assert imageToDelete.save(flush:true)!=null
    String jsonImage = imageToDelete.encodeAsJSON()
    int idImage = imageToDelete.id
    log.info("delete image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/"+idImage+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+idImage +".json"
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
    int newIdImage  = json.image.id



    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+newIdImage +".json"
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
    URL = Infos.CYTOMINEURL+Infos.REDOURL+".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/image/"+newIdImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    client.disconnect();
    assertEquals(404,code)


  }

  void testDeleteImageWithData()
  {
    log.info("create image")
    def imageToDelete = BasicInstance.createOrGetBasicImage()
    def annotation = BasicInstance.createOrGetBasicAnnotation()
    annotation.image = imageToDelete
    annotation.save(flush:true)
    String jsonImage = imageToDelete.encodeAsJSON()

    log.info("delete image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }

  void testDeleteImageNoExist()
  {
    log.info("create image")
    def imageToDelete = BasicInstance.createOrGetBasicImage()
    String jsonImage = imageToDelete.encodeAsJSON()

    log.info("delete image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/image/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }


}