import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider

import be.cytomine.HttpClient
import net.sf.json.JSONObject
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
    String str = client.getResponseString()
    def json = JSON.parse(str)
    assert json instanceof JSONObject
    client.disconnect();
    assertEquals(200,client.getResponseCode())
  }

  void testGetAnnotationsWithoutCredential() {
    HttpClient client = new HttpClient("http://localhost:8080/cytomine-web/api/image.json","badlogin","badpassword");
    client.connect("GET");
    def json = JSON.parse(client.getResponseString())
    assert json instanceof JSONObject
    client.disconnect();
    assertEquals(401,client.getResponseCode())
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
