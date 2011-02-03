package be.cytomine.api.project

import grails.test.*
import be.cytomine.acquisition.Scanner
import be.cytomine.project.Scan
import be.cytomine.warehouse.Data
import be.cytomine.warehouse.Mime
import be.cytomine.project.Annotation

import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class RestAnnotationControllerTests extends GrailsUnitTestCase {


  protected void setUp() {
    super.setUp()
    /* def mime = new Mime(extension:"ext",mimeType:"mimeT")
  assertTrue(mime.validate())
  mime.save(flush : true)
  def data = new Data(path : "path", mime : mime)
  assertTrue(data.validate())
  data.save(flush : true)
  def scanner = new Scanner(maxResolution:"40x",brand:"brand",model:"model")
  assertTrue(scanner.validate())
  scanner.save(flush : true)
  scan = new Scan(filename: "filename",data : data,scanner : scanner ,slide : null)
  assertTrue(scan.validate())
  scan.save(flush : true)*/
  }

  protected void tearDown() {
    super.tearDown()
    /*Annotation.list()*.delete()
    Scan.list()*.delete()
    Scanner.list()*.delete()
    Data.list()*.delete()
    Mime.list()*.delete() */
  }


  void testAddAnnotation() {
    /*  println "1"
  def ssvc = new grails.plugins.springsecurity.SpringSecurityService();
println "2"
  def u = new org.springframework.security.core.userdetails.User(
    "lrollus","password",true,true,true,true,
    [new org.springframework.security.core.authority.GrantedAuthorityImpl("TEST_ROLE")]);
println "3"
ssvc.metaClass.getPrincipal = { u }*/

    // c.springSecurityService = ssvc
    //
    //
    //
    // //assertFalse c.hasAuthority("TEST_ROLE_NO")
    //assertTrue c.hasAuthority("TEST_ROLE")
  }



  void testShowAnnotation() {
    String name = "name"
    String location = "POINT (1000 1000)"
    def scan = Scan.createBasicScan()
    def annotation = new Annotation(name:name,location:new WKTReader().read(location),scan: scan);
    assertTrue(annotation.validate())
    annotation.save(flush : true)

    RestAnnotationController c = new RestAnnotationController()
    c.params.idannotation = annotation.id
    c.show()
    def jsonAnnotation = c.response.contentAsString
    println  jsonAnnotation
    def json = JSON.parse(jsonAnnotation); // Parse a JSON String
    //{"annotation":{"id":4,"name":"name","location":"POINT (1000 1000)","scan":119}}
    assert json instanceof JSONObject // In this case, JSON.parse returns a JSONObject instance
    assert json instanceof Map // which implements the Map interface

    assertEquals(name,json.annotation.name)// access a property
    assertEquals(location, json.annotation.location)

    Annotation.list()*.delete()
    Scan.list()*.delete()
    Scanner.list()*.delete()
    Data.list()*.delete()
    Mime.list()*.delete()
  }

  void testShowAnnotationNotExist() {

    RestAnnotationController c = new RestAnnotationController()
    c.params.idannotation = -99
    c.show()
    def code = c.response.status
    println code
    assertEquals(404,code)
  }

  static boolean compareAnnotations(objectAnnotation,jsonAnnotation)
  {

  }





  void testGoogleAccess() {
    URL url =  new URL("http://www.google.com")
    HttpURLConnection con = (HttpURLConnection)url.openConnection();
    con.setRequestMethod("GET");
    con.connect();
    String reponse=con.getResponseCode();
    println reponse; // test affiche
    assertEquals("200",reponse)
  }

}
