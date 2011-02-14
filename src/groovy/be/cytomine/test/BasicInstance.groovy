package be.cytomine.test

import be.cytomine.warehouse.Mime
import be.cytomine.warehouse.Data
import be.cytomine.project.Annotation
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.project.Image
import be.cytomine.acquisition.Scanner
import be.cytomine.project.Term
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

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
    def mime = Mime.findByMimeType("mimeT");
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


   /* if(mimeList==null || mimeList.size()==0)
    {
      log.debug "mimeList is empty"
      mime = new Mime(extension:"ext",mimeType:"mimeT")
      mime.validate()
      log.debug("mime.errors="+mime.errors)
      mime.save(flush : true)
      log.debug("mime.errors="+mime.errors)
    }
    else
    {
      log.debug "mimeList is not empty"
      mime = mimeList[0]
    }  */
    mime
  }

  static Data createOrGetBasicData() {

    log.debug  "createOrGetBasicData()"
    Mime mime = BasicInstance.createOrGetBasicMime()
    def data = new Data(path:"pathpathpath",mime:mime)
    data.validate()
    log.debug("data.errors="+data.errors)
    data.save(flush : true)
    log.debug("data.errors="+data.errors)
    data

  }

  static Annotation createOrGetBasicAnnotation() {
    log.debug  "createOrGetBasicAnnotation()"
    def annotation = new Annotation(location:new WKTReader().read("POINT(17573.5 21853.5)"), name:"test",image:createOrGetBasicImage())
    annotation.validate()
    log.debug("annotation.errors="+annotation.errors)
    annotation.save(flush : true)
    log.debug("annotation.errors="+annotation.errors)
    annotation
  }

  static Image createOrGetBasicImage() {
    log.debug  "createOrGetBasicImage()"
    def image = new Image(filename: "filename",data : BasicInstance.createOrGetBasicData(),scanner : createOrGetBasicScanner() ,slide : null)
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

}
