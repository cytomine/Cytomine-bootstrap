package be.cytomine

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider

import be.cytomine.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.project.Annotation
import org.codehaus.groovy.grails.commons.*
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.api.project.RestAnnotationController

import be.cytomine.test.BasicInstance
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

 /*   String oldGeom = "POINT (1111 1111)"
    String newGeom = "POINT (9999 9999)"

//    /* Create a old annotation with point 1111 1111 */
//    log.info("create annotation")
//    Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
//    annotationToAdd.location =  new WKTReader().read(oldGeom)
//    annotationToAdd.save()
//
//    /* Encode a niew annotation with point 9999 9999 */
//    Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
//    def json = [annotation : annotationToEdit]
//    def jsonAnnotation = json.encodeAsJSON()
//    def jsonUpdate = JSON.parse(jsonAnnotation)
//    jsonUpdate.annotation.location = newGeom
//    jsonAnnotation = jsonUpdate.encodeAsJSON()
//    println "jsonAnnotation="+jsonAnnotation.toString();

    //update annotation a

    //put

    //check if 200 + equals


//
//      /* Call command to update POINT 1111 1111 => 9999 9999 */
//      Command editAnnotationCommand = new EditAnnotationCommand(postData : jsonAnnotation.toString())
//      def result = editAnnotationCommand.execute()
//      assertEquals(200,result.status)
//      Annotation annotation = result.data.annotation
//      assertTrue("Annotation result is not a correct annotation", (annotation instanceof Annotation))
//
//      /* Test if exist and is equal */
//       println "annotation.id=" +annotation.id
//      def newAnnotation = Annotation.get(annotation.id)
//        println "annotation.location=" +newAnnotation.location
//
//      assertNotNull("Annotation is not in database", newAnnotation)
//      assertEquals("Annotation geom is not modified",newGeom.replace(' ', ''),newAnnotation.location.toString().replace(' ',''))
//
//       /* Test if undo work and is equal to old annotation */
//      editAnnotationCommand.undo()
//      newAnnotation = Annotation.get(annotation.id)
//      println "annotation.location=" +newAnnotation.location
//      assertEquals("Annotation undo don't work",oldGeom.replace(' ', ''),newAnnotation.location.toString().replace(' ',''))
//
//      /* Test if redo work and is equal to old annotation */
//      editAnnotationCommand.redo()
//      newAnnotation = Annotation.get(annotation.id)
//      assertEquals("Annotation redo don't work",newGeom.replace(' ', ''),newAnnotation.location.toString().replace(' ',''))
//    }*/



  }

  void testEditAnnotationNotExist() {


  }

  void testEditAnnotationWithBadGeometry() {


  }

  void testDeleteAnnotation() {


  }

  void testDeleteAnnotationNotExist() {

  }

}
