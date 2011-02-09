import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider

import be.cytomine.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTests extends functionaltestplugin.FunctionalTestCase {

  void testGetAnnotationsWithCredential() {
    HttpClient client = new HttpClient("http://localhost:8080/cytomine-web/api/image.json","lrollus","password");
    client.connect("GET");
    //TODO: test if it is a JSON
    int code  = client.getResponseCode()
    String response = client.getResponseString()
    client.disconnect();
    assertEquals(200,code)
    def json = JSON.parse(response)
    assert json instanceof JSONObject

    //assertEquals(200,client.getResponseCode())
  }

  void testGetAnnotationsWithoutCredential() {
    HttpClient client = new HttpClient("http://localhost:8080/cytomine-web/api/image.json","badlogin","badpassword");
    client.connect("GET");
    assertEquals(401,client.getResponseCode())
    client.disconnect();
    //assertEquals(401,client.getResponseCode())
  }


  void testAddAnnotationCorrect() {
    //test with and withouth credential

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
