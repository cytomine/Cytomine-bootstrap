package be.cytomine


import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.project.Annotation
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.security.User
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTests extends functionaltestplugin.FunctionalTestCase {

  void testGetAnnotationsWithCredential() {

    log.info("create annotation")
    Annotation annotation =  BasicInstance.createOrGetBasicAnnotation()

    log.info("get annotation")
    String URL = Infos.CYTOMINEURL+"api/annotation.json"
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

  void testGetAnnotationsWithoutCredential() {

    log.info("create annotation")
    Annotation annotation =  BasicInstance.createOrGetBasicAnnotation()

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

  void testGetAnnotationWithCredential() {

    log.info("create annotation")
    Annotation annotation =  BasicInstance.createOrGetBasicAnnotation()

    log.info("get annotation:"+annotation.id)
    String URL = Infos.CYTOMINEURL+"api/annotation/"+annotation.id +".json"
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

  void testGetAnnotationWithoutCredential() {

    log.info("create annotation")
    Annotation annotation =  BasicInstance.createOrGetBasicAnnotation()

    log.info("get annotation:"+annotation.id)
    String URL = Infos.CYTOMINEURL+"api/annotation/"+annotation.id +".json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(401,code)

  }

  void testAddAnnotationCorrect() {

    log.info("create annotation")
    def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    String jsonAnnotation = ([annotation : annotationToAdd]).encodeAsJSON()

    log.info("post annotation:"+jsonAnnotation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idAnnotation = json.annotation.id

    log.info("check if object "+ idAnnotation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
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

    log.info("check if object "+ idAnnotation +" not exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
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

    println json
    //must be done because redo change id
    json = JSON.parse(response)
    assert json instanceof JSONObject
    idAnnotation = json.annotation.id

    log.info("check if object "+ idAnnotation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)
  }

  void testAddAnnotationBadGeom() {

    log.info("create annotation")
    def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    String jsonAnnotation = ([annotation : annotationToAdd]).encodeAsJSON()
    def updateAnnotation = JSON.parse(jsonAnnotation)
    updateAnnotation.annotation.location = 'POINT(BAD GEOMETRY)'
    jsonAnnotation = updateAnnotation.encodeAsJSON()

    log.info("post annotation:"+jsonAnnotation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testAddAnnotationScanNotExist() {

    log.info("create annotation")
    def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    def jsonAnnotation = ([annotation : annotationToAdd]).encodeAsJSON()
    def updateAnnotation = JSON.parse(jsonAnnotation)
    updateAnnotation.annotation.image = -99
    jsonAnnotation = updateAnnotation.encodeAsJSON()

    log.info("post annotation:"+jsonAnnotation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)
  }

  void testGetAnnotationWithCorrectScanId() {

    log.info("create annotation")
    Annotation annotation =  BasicInstance.createOrGetBasicAnnotation()

    log.info("get annotation with scan:"+annotation.image.id)
    String URL = Infos.CYTOMINEURL+"api/image/"+annotation.image.id +"/annotation.json"
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

  void testGetAnnotationWithBadScanId() {

    log.info("create annotation")
    Annotation annotation =  BasicInstance.createOrGetBasicAnnotation()

    log.info("get annotation with scan:"+annotation.image.id)
    String URL = Infos.CYTOMINEURL+"api/image/-99/annotation.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }


  void testGetAnnotationNotExist() {

    log.info("create annotation")
    Annotation annotation =  BasicInstance.createOrGetBasicAnnotation()

    log.info("get annotation:"+annotation.id)
    String URL = Infos.CYTOMINEURL+"api/annotation/-99.json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }

  void testEditAnnotation() {

    String oldGeom = "POINT (1111 1111)"
    String newGeom = "POINT (9999 9999)"

    Double oldZoomLevel = 1
    Double newZoomLevel = 9

    String oldChannels = "OLDCHANNELS"
    String newChannels = "NEWCHANNELS"

    User oldUser = BasicInstance.getBenjamin()
    User newUser = BasicInstance.getLoic()


    def mapNew = ["geom":newGeom,"zoomLevel":newZoomLevel,"channels":newChannels,"user":newUser]
    def mapOld = ["geom":oldGeom,"zoomLevel":oldZoomLevel,"channels":oldChannels,"user":oldUser]



    /* Create a old annotation with point 1111 1111 */
    log.info("create annotation")
    Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    annotationToAdd.location =  new WKTReader().read(oldGeom)
    annotationToAdd.zoomLevel = oldZoomLevel
    annotationToAdd.channels = oldChannels
    annotationToAdd.user = oldUser
    assert (annotationToAdd.save(flush:true) != null)

    /* Encode a niew annotation with point 9999 9999 */
    Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
    def jsonEdit = [annotation : annotationToEdit]
    def jsonAnnotation = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonAnnotation)
    jsonUpdate.annotation.location = newGeom
    jsonUpdate.annotation.zoomLevel = newZoomLevel
    jsonUpdate.annotation.channels = newChannels
    jsonUpdate.annotation.user = newUser.id
    jsonAnnotation = jsonUpdate.encodeAsJSON()

    log.info("put annotation:"+jsonAnnotation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/"+annotationToEdit.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    println response
    client.disconnect();

    log.info("check response")
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idAnnotation = json.annotation.id


    assertEquals("Annotation geom is not modified (response)",newGeom.replace(' ', ''),json.annotation.location.toString().replace(' ',''))

    log.info("check if object "+ idAnnotation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject

    BasicInstance.compareAnnotation(mapNew,json)

    log.info("test undo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idAnnotation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject
    assertEquals("Annotation geom is not modified (annother request)",oldGeom.replace(' ', ''),json.annotation.location.replace(' ',''))

    BasicInstance.compareAnnotation(mapOld,json)

    log.info("test redo")
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    log.info("check if object "+ idAnnotation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject
    assertEquals("Annotation geom is not modified (annother request)",newGeom.replace(' ', ''),json.annotation.location.replace(' ',''))

    BasicInstance.compareAnnotation(mapNew,json)


    log.info("check if object "+ idAnnotation +" exist in DB")
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();

    assertEquals(200,code)
    json = JSON.parse(response)
    assert json instanceof JSONObject
    assertEquals("Annotation geom is not modified (annother request)",newGeom.replace(' ', ''),json.annotation.location.replace(' ',''))


    //TODO: check for change in scan (?)
  }

  void testEditAnnotationDifferent() {

     String oldGeom = "POINT (1111 1111)"
    String newGeom = "POINT (9999 9999)"

    /* Create a old annotation with point 1111 1111 */
    log.info("create annotation")
    Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    annotationToAdd.location =  new WKTReader().read(oldGeom)
    annotationToAdd.save(flush:true)

    /* Encode a niew annotation with point 9999 9999 */
    Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
    def jsonEdit = [annotation : annotationToEdit]
    def jsonAnnotation = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonAnnotation)
    jsonUpdate.annotation.location = newGeom
    jsonAnnotation = jsonUpdate.encodeAsJSON()

    log.info("put annotation:"+jsonAnnotation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testEditAnnotationNotExist() {

     String oldGeom = "POINT (1111 1111)"
    String newGeom = "POINT (9999 9999)"

    /* Create a old annotation with point 1111 1111 */
    log.info("create annotation")
    Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    annotationToAdd.location =  new WKTReader().read(oldGeom)
    annotationToAdd.save(flush:true)

    /* Encode a niew annotation with point 9999 9999 */
    Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
    def jsonEdit = [annotation : annotationToEdit]
    def jsonAnnotation = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonAnnotation)
    jsonUpdate.annotation.location = newGeom
    jsonUpdate.annotation.id = "-99"
    jsonAnnotation = jsonUpdate.encodeAsJSON()

    log.info("put annotation:"+jsonAnnotation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)

  }

  void testEditAnnotationWithBadGeometry() {

    String oldGeom = "POINT (1111 1111)"
    String newGeom = "POINT (BAD GEOMETRY)"

    /* Create a old annotation with point 1111 1111 */
    log.info("create annotation")
    Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    annotationToAdd.location =  new WKTReader().read(oldGeom)
    annotationToAdd.save(flush:true)

    /* Encode a niew annotation with point 9999 9999 */
    Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
    def jsonEdit = [annotation : annotationToEdit]
    def jsonAnnotation = jsonEdit.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonAnnotation)
    jsonUpdate.annotation.location = newGeom
    jsonAnnotation = jsonUpdate.encodeAsJSON()

    log.info("put annotation:"+jsonAnnotation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/"+annotationToAdd.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.put(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();

    log.info("check response")
    assertEquals(400,code)

  }

  void testDeleteAnnotation() {

    log.info("create annotation")
    def annotationToDelete = BasicInstance.createOrGetBasicAnnotation()
    String jsonAnnotation = ([annotation : annotationToDelete]).encodeAsJSON()

    log.info("delete annotation:"+jsonAnnotation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/"+annotationToDelete.id+".json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(204,code)

  }

  void testDeleteAnnotationNotExist() {

     log.info("create annotation")
    def annotationToDelete = BasicInstance.createOrGetBasicAnnotation()
    String jsonAnnotation = ([annotation : annotationToDelete]).encodeAsJSON()

    log.info("delete annotation:"+jsonAnnotation.replace("\n",""))
    String URL = Infos.CYTOMINEURL+"api/annotation/-99.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.delete()
    int code  = client.getResponseCode()
    client.disconnect();

    log.info("check response")
    assertEquals(404,code)
  }

}
