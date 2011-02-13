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
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTests extends functionaltestplugin.FunctionalTestCase {


  void testGetAnnotationsWithCredential() {

    Annotation annotation =  BasicInstance.createOrGetBasicAnnotation()
    String URL = Infos.CYTOMINEURL+"api/annotation/"+annotation.id +".json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

  }

  void testGetAnnotationsWithoutCredential() {

    Annotation annotation =  BasicInstance.createOrGetBasicAnnotation()
    String URL = Infos.CYTOMINEURL+"api/annotation/"+annotation.id +".json"
    HttpClient client = new HttpClient();
    client.connect(URL,Infos.BADLOGIN,Infos.BADPASSWORD);
    client.get()
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();
    assertEquals(401,code)

  }

  void testAddAnnotationCorrect() {

    /*def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    String jsonAnnotation = ([annotation : annotationToAdd]).encodeAsJSON()
    println jsonAnnotation
    String URL = Infos.CYTOMINEURL+"api/annotation.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    int idAnnotation = json.annotation.id
    println "idAnnotation=" + idAnnotation
    //check if object exist in DB

    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)

    //test undo
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.UNDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    assertEquals(200,code)

    //test if deleted
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(404,code)

    //test redo
    client = new HttpClient()
    URL = Infos.CYTOMINEURL+Infos.REDOURL
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    assertEquals(200,code)

    //test is re-created
    client = new HttpClient();
    URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation +".json"
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD);
    client.get()
    code  = client.getResponseCode()
    response = client.getResponseData()
    client.disconnect();
    assertEquals(200,code)    */
  }


  void testAddAnnotationBadGeom() {
    def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()

    String jsonAnnotation = ([annotation : annotationToAdd]).encodeAsJSON()

    def updateAnnotation = JSON.parse(jsonAnnotation)
    updateAnnotation.annotation.location = 'POINT(BAD GEOMETRY)'
    jsonAnnotation = updateAnnotation.encodeAsJSON()

    println jsonAnnotation
    String URL = Infos.CYTOMINEURL+"api/annotation.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();
    assertEquals(400,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
    //check if object exist in DB

  }

  void testAddAnnotationScanNotExist() {
    def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    def jsonAnnotation = ([annotation : annotationToAdd]).encodeAsJSON()
    def updateAnnotation = JSON.parse(jsonAnnotation)
    updateAnnotation.annotation.image = -99
    jsonAnnotation = updateAnnotation.encodeAsJSON()

    println jsonAnnotation
    String URL = Infos.CYTOMINEURL+"api/annotation.json"
    HttpClient client = new HttpClient()
    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    client.post(jsonAnnotation)
    int code  = client.getResponseCode()
    String response = client.getResponseData()
    client.disconnect();
    assertEquals(400,code)
  }

  void testGetAnnotationWithCorrectScanId() {
    //test with and withouth credential

  }

  void testGetAnnotationWithBadScanId() {


  }

  void testGetAnnotationExist() {
    //test with and withouth credential



  }

  void testGetAnnotationNotExist() {


  }

  void testEditAnnotation() {
    //test with and withouth credential

  }

  void testEditAnnotationNotExist() {


  }

  void testEditAnnotationWithBadGeometry() {


  }

  void testDeleteAnnotation() {
    //test with and withouth credential


  }

  void testDeleteAnnotationNotExist() {



  }

}
