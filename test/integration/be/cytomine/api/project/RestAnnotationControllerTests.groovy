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
  }

  protected void tearDown() {
    super.tearDown()
  }


  void testAddAnnotation() {
    /*println "1"
    def ssvc = new grails.plugins.springsecurity.SpringSecurityService();
    println "2"
    def u = new org.springframework.security.core.userdetails.User(
            "lrollus","password",true,true,true,true,
            [new org.springframework.security.core.authority.GrantedAuthorityImpl("TEST_ROLE")]);

    println "3"
    ssvc.metaClass.getPrincipal = { u }



    String name = "name"
    String location = "POINT (1000 1000)"
     println "4"
    def scan = Scan.createOrGetBasicScan()
    println "5"
    def annotation = new Annotation(name:name,location:new WKTReader().read(location),scan: scan);
    println "6"
    assertTrue(annotation.validate())
    println "7"
    annotation.save(flush : true)
    println "8"
    RestAnnotationController c = new RestAnnotationController()
    println "9"
   // c.request.setAttribute("JSON","{\"annotation\":{\"location\":\"POINT(17573.5 21853.5)\",\"name\":\"test\",\"class\":\"be.cytomine.project.Annotation\",\"scan\":37}}" )
    c.request = "{\"annotation\":{\"location\":\"POINT(17573.5 21853.5)\",\"name\":\"test\",\"class\":\"be.cytomine.project.Annotation\",\"scan\":37}}"
    println "10"
    c.springSecurityService = ssvc
    println "11"
    c.add()
    println "12"
    def jsonAnnotation = c.response.contentAsString
    println "13"
    println  jsonAnnotation
    // c.springSecurityService = ssvc
    //
    //
    //
    // //assertFalse c.hasAuthority("TEST_ROLE_NO")
    //assertTrue c.hasAuthority("TEST_ROLE")   */
  }



  void testShowAnnotation() {
    String name = "name"
    String location = "POINT (1000 1000)"

    def scan = Scan.createOrGetBasicScan()
    def annotation = new Annotation(name:name,location:new WKTReader().read(location),scan: scan);
    assertTrue(annotation.validate())
    annotation.save(flush : true)

    RestAnnotationController c = new RestAnnotationController()
    c.params.idannotation = annotation.id
    c.show()

    def jsonAnnotation = c.response.contentAsString
    println  jsonAnnotation
    def json = JSON.parse(jsonAnnotation);
    //{"annotation":{"id":4,"name":"name","location":"POINT (1000 1000)","scan":119}}

    assert json instanceof JSONObject // In this case, JSON.parse returns a JSONObject instance
    assert json instanceof Map // which implements the Map interface

    assertEquals(name,json.annotation.name)// access a property
    assertEquals(location, json.annotation.location)

  }

  void testShowAnnotationNotExist() {

    RestAnnotationController c = new RestAnnotationController()
    c.params.idannotation = -99
    c.show()
    def code = c.response.status
    println code
    assertEquals(404,code)
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
