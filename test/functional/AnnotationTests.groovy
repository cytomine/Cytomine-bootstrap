import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider

import be.cytomine.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.project.Annotation
import org.codehaus.groovy.grails.commons.*
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTests extends functionaltestplugin.FunctionalTestCase {
  def config = ConfigurationHolder.config.grails
  void testGetAnnotationsWithCredential() {
    Annotation annotation =  BasicInstance.createOrGetBasicAnnotation()
    HttpClient client = new HttpClient(config.serverURL,"lrollus","password");
    client.connect("GET");
    int code  = client.getResponseCode()
    String response = client.getResponseString()
    client.disconnect();
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject
  }

  void testGetAnnotationsWithoutCredential() {
    HttpClient client = new HttpClient(config.serverURL,"badlogin","badpassword");
    client.connect("GET");
    int code  = client.getResponseCode()
    assertEquals(401,code)
    client.disconnect();
  }

  void testAddAnnotationCorrect() {
    /*def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
    String jsonAnnotation = ([annotation : annotationToAdd]).encodeAsJSON()
    println jsonAnnotation
    HttpClient client = new HttpClient("http://localhost:8080/cytomine-web/api/annotation.json","lrollus","password");
    client.connect("POST");
    client.post(jsonAnnotation.toString())
    int code  = client.getResponseCode()
    println "code="+code
    String response = client.getResponseString()
    client.disconnect();
    assertEquals(201,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject  */
  }

  void testAddAnnotationBadGeom() {

  }

  void testAddAnnotationScanNotExist() {

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
