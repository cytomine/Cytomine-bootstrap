import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIUtils
import be.cytomine.HttpClient
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTests extends functionaltestplugin.FunctionalTestCase {

  void testGetAnnotationsWithCredential() {
    HttpClient client = new HttpClient("http://localhost:8080/cytomine-web/api/scan.json","lrollus","password");
    client.connect("GET");
    //TODO: test if it is a JSON
    client.disconnect();
    assertEquals(200,client.getResponseCode())
  }

  void testGetAnnotationsWithoutCredential() {
    HttpClient client = new HttpClient("http://localhost:8080/cytomine-web/api/scan.json","badlogin","badpassword");
    client.connect("GET");
    //TODO: test if it is NOT a JSON
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
