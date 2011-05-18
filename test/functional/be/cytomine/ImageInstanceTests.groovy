package be.cytomine
import be.cytomine.test.BasicInstance
import be.cytomine.image.AbstractImage
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
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 9:11
 * To change this template use File | Settings | File Templates.
 */
class ImageInstanceTests extends functionaltestplugin.FunctionalTestCase{


  void testGetImagesInstanceWithCredential() {

    log.info("get imageinstance")
    String URL = Infos.CYTOMINEURL+"api/imageinstance.json"
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

  void testGetImagesInstanceWithoutCredential() {

    log.info("create imageinstance")
    ImageInstance image =  BasicInstance.createOrGetBasicImageInstance()

    log.info("get annotation")
    String URL = Infos.CYTOMINEURL+"api/imageinstance.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)

  }

  void testListImagesInstanceByUserWithCredential() {

    log.info("create imageinstance")
    ImageInstance image =  BasicInstance.createOrGetBasicImageInstance()
    User user = BasicInstance.createOrGetBasicUser()

    log.info("get imageinstance")
    String URL = Infos.CYTOMINEURL+"api/user/"+user.id+"/imageinstance.json"
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

  void testListImagesInstanceByUserNoExistWithCredential() {

    log.info("create imageinstance")
    ImageInstance image =  BasicInstance.createOrGetBasicImageInstance()

    log.info("get imageinstance")
    String URL = Infos.CYTOMINEURL+"api/user/-99/imageinstance.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }

  void testListImagesInstanceByImageWithCredential() {

    log.info("create imageinstance")
    ImageInstance image =  BasicInstance.createOrGetBasicImageInstance()
    AbstractImage abstractImage = BasicInstance.createOrGetBasicAbstractImage()

    log.info("get imageinstance")
    String URL = Infos.CYTOMINEURL+"api/image/"+abstractImage.id+"/imageinstance.json"
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

  void testListImagesInstanceByProjectWithCredential() {

    log.info("create imageinstance")
    ImageInstance image =  BasicInstance.createOrGetBasicImageInstance()
    Project project = BasicInstance.createOrGetBasicProject()

    log.info("get imageinstance")
    String URL = Infos.CYTOMINEURL+"api/project/"+project.id+"/imageinstance.json"
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

  void testGetImageInstanceWithCredential() {

    log.info("create imageinstance")
    ImageInstance image =  BasicInstance.createOrGetBasicImageInstance()

    log.info("get imageinstance")
    String URL = Infos.CYTOMINEURL+"api/imageinstance/"+ image.id +".json"
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

  void testAddImageInstanceCorrect() {

    log.info("create imageinstance")
    def imageToAdd = BasicInstance.createOrGetBasicImageInstance()
    String jsonImage = imageToAdd.encodeAsJSON()

    log.info("post imageinstance:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/imageinstance.json"
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
    int idImage = json.imageinstance.id

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/imageinstance/"+idImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    /*log.info("test undo")
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
    assertEquals(200,code)*/

  }

  void testaddImageInstanceWithUnexistingAbstractImage() {

    log.info("create imageinstance")
    def imageToAdd = BasicInstance.createOrGetBasicImageInstance()
    String jsonImage = imageToAdd.encodeAsJSON()
    def updateImage = JSON.parse(jsonImage)
    updateImage.baseImage = -99
    jsonImage = updateImage.encodeAsJSON()

    log.info("post image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/imageinstance.json"
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

  void testaddImageInstanceWithUnexistingProject() {

    log.info("create image")
    def imageToAdd = BasicInstance.createOrGetBasicImageInstance()
    String jsonImage = imageToAdd.encodeAsJSON()
    def updateImage = JSON.parse(jsonImage)
    updateImage.project = -99
    jsonImage = updateImage.encodeAsJSON()

    log.info("post image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/imageinstance.json"
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

 /* void testaddImageInstanceWithUnexistingUser() {

    log.info("create image")
    def imageToAdd = BasicInstance.createOrGetBasicImageInstance()
    String jsonImage = imageToAdd.encodeAsJSON()
    def updateImage = JSON.parse(jsonImage)
    updateImage.user = -99
    jsonImage = updateImage.encodeAsJSON()

    log.info("post image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/imageinstance.json"
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

  }  */



  void testEditImageInstance() {

    Project oldProject = BasicInstance.createOrGetBasicProject()
    Project newProject = BasicInstance.getBasicProjectNotExist()
    newProject.save(flush:true)

    AbstractImage oldImage = BasicInstance.createOrGetBasicAbstractImage()
    AbstractImage newImage = BasicInstance.getBasicAbstractImageNotExist()
    newImage.save(flush:true)

    User oldUser = BasicInstance.createOrGetBasicUser()
    User newUser = BasicInstance.getBasicUserNotExist()
    newUser.save(flush:true)

    def mapNew = ["project":newProject,"baseImage":newImage,"user":newUser]
    def mapOld = ["project":oldProject,"baseImage":oldImage,"user":oldUser]

    /* Create a old image */
    log.info("create image")
    ImageInstance imageToAdd = BasicInstance.createOrGetBasicImageInstance()
    imageToAdd.project =  oldProject;
    imageToAdd.baseImage =  oldImage;
    imageToAdd.user =  oldUser;
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    ImageInstance imageToEdit = ImageInstance.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)

    jsonUpdate.project = newProject.id
    jsonUpdate.baseImage = newImage.id
    jsonUpdate.user = newUser.id

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/imageinstance/"+imageToEdit.id+".json"
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
    int idImage = json.imageinstance.id

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/imageinstance/"+idImage +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareImageInstance(mapNew,json)

    /*log.info("test undo")
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
    assert json instanceof JSONObject  */

  }


  void testEditImageInstanceWithBadProject()
  {
    Project oldProject = BasicInstance.createOrGetBasicProject()
    Project newProject = BasicInstance.getBasicProjectNotExist()

    /* Create a old image */
    log.info("create imageinstance")
    ImageInstance imageToAdd = BasicInstance.createOrGetBasicImageInstance()
    imageToAdd.project = oldProject
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    ImageInstance imageToEdit = ImageInstance.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)

    jsonUpdate.project = -99

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/imageinstance/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testEditImageInstanceWithBadUser()
  {
    User oldUser = BasicInstance.createOrGetBasicUser()
    User newUser = BasicInstance.getBasicUserNotExist()

    /* Create a old image */
    log.info("create imageinstance")
    ImageInstance imageToAdd = BasicInstance.createOrGetBasicImageInstance()
    imageToAdd.user = oldUser
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    ImageInstance imageToEdit = ImageInstance.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)

    jsonUpdate.user = -99

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/imageinstance/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testEditImageInstanceWithBadImage()
  {
    AbstractImage oldImage = BasicInstance.createOrGetBasicAbstractImage()
    AbstractImage newImage = BasicInstance.getBasicAbstractImageNotExist()

    /* Create a old image */
    log.info("create imageinstance")
    ImageInstance imageToAdd = BasicInstance.createOrGetBasicImageInstance()
    imageToAdd.baseImage = oldImage
    imageToAdd.save(flush:true)

    /* Encode a new image to modify */
    ImageInstance imageToEdit = ImageInstance.get(imageToAdd.id)
    def jsonImage = imageToEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonImage)

    jsonUpdate.baseImage = -99

    jsonImage = jsonUpdate.encodeAsJSON()

    log.info("put image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/imageinstance/"+imageToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonImage)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }


  void testDeleteImageInstance()
  {

    log.info("create imageinstance")
    def imageToDelete = BasicInstance.getBasicImageInstanceNotExist()
    log.info  "imageToDelete.baseimage="+ imageToDelete.baseImage
    log.info  "imageToDelete.project="+ imageToDelete.project
    log.info  "imageToDelete.user="+ imageToDelete.user
    log.info  "imageToDelete="+ imageToDelete
    log.info  "validation="+ imageToDelete.validate()
    log.info  "errors="+ imageToDelete.errors



    assert imageToDelete.save(flush:true)!=null
    String jsonImage = imageToDelete.encodeAsJSON()
    int idImage = imageToDelete.id
    log.info("delete image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/"+imageToDelete.project.id + "/image/"+imageToDelete.baseImage.id + "/imageinstance.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)

    log.info("check if object "+ idImage +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/imageinstance/"+idImage +".json"
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
    assertEquals(404,code) */


  }

  void testDeleteImageInstanceNoExist()
  {
    log.info("create image")
    def imageToDelete = BasicInstance.createOrGetBasicImageInstance()
    String jsonImage = imageToDelete.encodeAsJSON()

    log.info("delete image:"+jsonImage.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/project/-99/image/-99/imageinstance.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }
}
