package be.cytomine.test

import be.cytomine.warehouse.Mime
import be.cytomine.project.Annotation
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.project.Image
import be.cytomine.acquisition.Scanner
import be.cytomine.project.Term
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import be.cytomine.security.User

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 9/02/11
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */
class BasicInstance {

  private static Log log = LogFactory.getLog(BasicInstance.class)


  static Mime createOrGetBasicMime() {

    log.debug "createOrGetBasicMime()"
    /*def mime = Mime.findByMimeType("mimeT");
    log.debug "mime="+ mime
    if(mime==null)
    {
      log.debug "mimeList is empty"
      mime = new Mime(extension:"ext",mimeType:"mimeT")
      mime.validate()
      log.debug("mime.errors="+mime.errors)
      mime.save(flush : true)
      log.debug("mime.errors="+mime.errors)
    }
    mime */
    Mime.findByExtension("jp2")
  }

  static Annotation createOrGetBasicAnnotation() {
    log.debug  "createOrGetBasicAnnotation()"
    def annotation = new Annotation(location:new WKTReader().read("POINT(17573.5 21853.5)"), name:"test",image:createOrGetBasicImage(), user:createOrGetBasicUser())
    annotation.validate()
    log.debug("annotation.errors="+annotation.errors)
    annotation.save(flush : true)
    log.debug("annotation.errors="+annotation.errors)
    annotation
  }

  static Image createOrGetBasicImage() {
    log.debug  "createOrGetBasicImage()"
    def image = new Image(filename: "filename",scanner : createOrGetBasicScanner() ,slide : null,mime:BasicInstance.createOrGetBasicMime(),path:"pathpathpath")
    image.validate()
    log.debug "image.errors="+image.errors
    image.save(flush : true)
    log.debug "image.errors="+image.errors
    image
  }

  static Scanner createOrGetBasicScanner() {

    log.debug  "createOrGetBasicScanner()"
    def scanner = new Scanner(maxResolution:"x40",brand:"brand", model:"model")
    scanner.validate()
    log.debug "scanner.errors="+scanner.errors
    scanner.save(flush : true)
    log.debug "scanner.errors="+scanner.errors
    scanner

  }

  static Term createOrGetBasicTerm() {

    log.debug  "createOrGetBasicTerm()"
    log.debug  "Create child term"
    def termchild = new Term(name:"basicTermChild",comment:"basicTermComment")
    termchild.validate()
    log.debug "termchild.errors="+termchild.errors
    termchild.save(flush : true)
    log.debug "termchild.errors="+termchild.errors

    log.debug  "Create main term"
    def term = new Term(name:"basicTermName",comment:"basicTermComment")
    term.validate()
    log.debug "term.errors="+term.errors
    term.addToChild(termchild)
    term.save(flush : true)
    log.debug "term.errors="+term.errors
    term
  }

  static User getBenjamin() {

    log.debug  "createOrGetBasicUser()"
    User.get(3)
  }

  static User getLoic() {

    log.debug  "createOrGetBasicUser()"
    User.get(2)
  }

  static User createOrGetBasicUser() {

    log.debug  "createOrGetBasicUser()"
    User.get(2)
  }

  //    def mapNew = ["newGeom":newGeom,"newZoomLevel":newZoomLevel,"newChannels":newChannels,"newUser":newUser]
  static void compareAnnotation(map, json)  {

    /*assertEquals("Annotation geom is not modified (annother request)",map.geom.replace(' ', ''),json.annotation.location.replace(' ',''))
    assertEquals("Zoom level is not modified (annother request)",map.zoomLevel,json.annotation.zoomLevel)
    assertEquals("Channels is not modified (annother request)",map.channels,json.annotation.channels)
    assertEquals("User is not modified (annother request)",map.user.id,json.annotation.user) */

    assert map.geom.replace(' ', '').equals(json.annotation.location.replace(' ',''))
    assert toDouble(map.zoomLevel).equals(toDouble(json.annotation.zoomLevel))
    assert map.channels.equals(json.annotation.channels)
    assert toLong(map.user.id).equals(toLong(json.annotation.user))
  }

  static Double toDouble(String s)
  {
     if(s==null && s.equals("null")) return null
     else return Double.parseDouble(s)
  }

  static Double toDouble(Integer s)
  {
     if(s==null) return null
     else return Double.parseDouble(s.toString())
  }

  static Double toDouble(Double s)
  {
     return s
  }

  static Integer toInteger(String s)
  {
     if(s==null && s.equals("null")) return null
     else return Integer.parseDouble(s)
  }

  static Integer toInteger(Integer s)
  {
     return s
  }

  static Integer toLong(String s)
  {
     if(s==null && s.equals("null")) return null
     else return Integer.parseLong(s)
  }

  static Integer toLong(Long s)
  {
     return s
  }
}
