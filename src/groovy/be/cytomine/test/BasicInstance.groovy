package be.cytomine.test

import be.cytomine.warehouse.Mime
import be.cytomine.warehouse.Data
import be.cytomine.project.Annotation
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.project.Image
import be.cytomine.acquisition.Scanner
import be.cytomine.project.Term

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 9/02/11
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */
class BasicInstance {


  static Mime createOrGetBasicMime() {
    println "createOrGetBasicMime()"
    def mimeList = Mime.findAllByMimeType("mimeT");
    def mime
    if(mimeList==null || mimeList.size()==0)
    {
      mime = new Mime(extension:"ext",mimeType:"mimeT")
      mime.save(flush : true)
    }
    else
    {
      mime = mimeList[0]
    }
    mime
  }

  static Data createOrGetBasicData() {

    println "createOrGetBasicData()"
    Mime mime = BasicInstance.createOrGetBasicMime()
    def data = new Data(path:"pathpathpath",mime:mime)
    data.save(flush : true)
    data

  }

  static Annotation createOrGetBasicAnnotation() {
    println "createOrGetBasicAnnotation()"
    def annotation = new Annotation(location:new WKTReader().read("POINT(17573.5 21853.5)"), name:"test",image:createOrGetBasicImage())

    println "annotation.validate()=" + annotation.validate()
    annotation.save(flush : true)
    annotation
  }

  static Image createOrGetBasicImage() {
    println "createOrGetBasicImage()"
    def image = new Image(filename: "filename",data : BasicInstance.createOrGetBasicData(),scanner : createOrGetBasicScanner() ,slide : null)
    println image.validate()
    image.save(flush : true)
    image
  }

  static Scanner createOrGetBasicScanner() {

    println "createOrGetBasicScanner()"
    def scanner = new Scanner(maxResolution:"x40",brand:"brand", model:"model")
    scanner.save(flush : true)
    scanner

  }

  static Term createOrGetBasicTerm() {

    println "createOrGetBasicTerm()"
    def termchild = new Term(name:"basicTermChild",comment:"basicTermComment")
    printIfErrors(termchild)
    termchild.save(flush : true)
    def term = new Term(name:"basicTermName",comment:"basicTermComment")
    printIfErrors(term)
    term.addToChild(termchild)
    term.save(flush : true)
    term
  }

  static void printIfErrors(object) {

    if(object.validate()) {
        println "validate()==true"
    }
    else {
        object.errors.allErrors.each {
            println "error="+it
        }
    }

  }
}
