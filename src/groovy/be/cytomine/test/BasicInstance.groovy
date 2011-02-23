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
import be.cytomine.project.Slide
import be.cytomine.project.Project
import be.cytomine.project.Relation
import be.cytomine.project.RelationTerm
import be.cytomine.project.AnnotationTerm

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 9/02/11
 * Time: 13:37
 * To change this template use File | Settings | File Templates.
 */
class BasicInstance {

  private static Log log = LogFactory.getLog(BasicInstance.class)

  static Relation createOrGetBasicRelation() {

    log.debug "createOrGetBasicRelation()"
    def relation = Relation.findByName("BasicRelation")
    if(!relation) {
      relation =  new Relation(name:"BasicRelation")
      relation.validate()
      log.debug("relation.errors="+relation.errors)
      relation.save(flush : true)
      log.debug("relation.errors="+relation.errors)
    }
    assert relation!=null
    relation
  }

  static Relation getBasicRelationNotExist() {

    log.debug "createOrGetBasicRelationNotExist()"
    def random = new Random()
    def randomInt = random.nextInt()
    def relation = Relation.findByName(randomInt+"")

    while(relation){
      randomInt = random.nextInt()
      relation = Relation.findByName(randomInt+"")
   }

    relation =  new Relation(name:randomInt+"")
    relation.validate()
    log.debug("relation.errors="+relation.errors)

    assert relation!=null
    relation
  }

  static Mime createOrGetBasicMime() {

    log.debug "createOrGetBasicMime1()"
    def jp2mime = Mime.findByExtension("jp2")
    jp2mime.refresh()
    jp2mime.imageServers()
    jp2mime
  }

  static Mime createNewMime() {
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
    assert mime!=null
    mime
  }

  static Annotation createOrGetBasicAnnotation() {
    log.debug  "createOrGetBasicAnnotation()"
    def annotation = new Annotation(location:new WKTReader().read("POINT(17573.5 21853.5)"), name:"test",image:createOrGetBasicImage(), user:createOrGetBasicUser())
    annotation.validate()
    log.debug("annotation.errors="+annotation.errors)
    annotation.save(flush : true)
    log.debug("annotation.errors="+annotation.errors)
    assert annotation!=null
    annotation
  }

  static Image createOrGetBasicImage() {
    log.debug  "createOrGetBasicImage()"
    def image = new Image(filename: "filename",scanner : createOrGetBasicScanner() ,slide : null,mime:BasicInstance.createOrGetBasicMime(),path:"pathpathpath")
    image.validate()
    log.debug "image.errors="+image.errors
    image.save(flush : true)
    log.debug "image.errors="+image.errors
    assert image!=null
    image
  }




  static Scanner createOrGetBasicScanner() {

    log.debug  "createOrGetBasicScanner()"
    def scanner = new Scanner(maxResolution:"x40",brand:"brand", model:"model")
    scanner.validate()
    log.debug "scanner.errors="+scanner.errors
    scanner.save(flush : true)
    log.debug "scanner.errors="+scanner.errors
    assert scanner!=null
    scanner

  }
  static Scanner createNewScanner() {

    log.debug  "createNewScanner()"
    def scanner = new Scanner(maxResolution:"x60",brand:"newBrand", model:"newModel")
    scanner.validate()
    log.debug "scanner.errors="+scanner.errors
    scanner.save(flush : true)
    log.debug "scanner.errors="+scanner.errors
    assert scanner!=null
    scanner

  }


  static Slide createOrGetBasicSlide() {

    log.debug  "createOrGetBasicSlide()"
    def slide = new Slide(name:"slide",order:1)
    slide.validate()
    log.debug "slide.errors="+slide.errors
    slide.save(flush : true)
    log.debug "slide.errors="+slide.errors
    assert slide!=null
    slide

  }

  static Slide createNewSlide() {

    log.debug  "createNewSlide()"
    def slide = new Slide(name:"newSlide",order:2)
    slide.validate()
    log.debug "slide.errors="+slide.errors
    slide.save(flush : true)
    log.debug "slide.errors="+slide.errors
    assert slide!=null
    slide

  }



  static User getOldUser() {

    log.debug  "createOrGetBasicUser()"
    User user = User.findByUsername("stevben")
    assert user!=null
    user
  }

  static User getNewUser() {

    log.debug  "createOrGetBasicUser()"
    User user = User.findByUsername("lrollus")
    assert user!=null
    user
  }

  static User createOrGetBasicUser() {

    log.debug  "createOrGetBasicUser()"
    User user = User.findByUsername("lrollus")
    assert user!=null
    user
  }

  static Project createOrGetBasicProject() {
    log.debug  "createOrGetBasicProject()"
    def project = Project.findByName("BasicProject")
    if(!project) {

      project = new Project(name:"BasicProject")
      project.validate()
      log.debug "project.errors="+project.errors
      project.save(flush : true)
      log.debug "project.errors="+project.errors
    }
    assert project!=null
    project
  }

  static Project getBasicProjectNotExist() {

    log.debug "getBasicProjectNotExist()"
    def random = new Random()
    def randomInt = random.nextInt()
    def project = Project.findByName(randomInt+"")

    while(project){
      randomInt = random.nextInt()
      project = Project.findByName(randomInt+"")
   }

    project =  new Project(name:randomInt+"")
    project.validate()
    project
  }

  static Term createOrGetBasicTerm() {
    log.debug  "createOrGetBasicTerm()"
    def term = Term.findByName("BasicTerm")
    if(!term) {

      term = new Term(name:"BasicTerm")
      term.validate()
      log.debug "term.errors="+term.errors
      term.save(flush : true)
      log.debug "term.errors="+term.errors
    }
    assert term!=null
    term
  }

  static Term createOrGetAnotherBasicTerm() {
    log.debug  "createOrGetBasicTerm()"
    def term = Term.findByName("AnotherBasicTerm")
    if(!term) {

      term = new Term(name:"AnotherBasicTerm")
      term.validate()
      log.debug "term.errors="+term.errors
      term.save(flush : true)
      log.debug "term.errors="+term.errors
    }
    assert term!=null
    term
  }

  static Term getBasicTermNotExist() {

    log.debug "getBasicTermNotExist()"
    def random = new Random()
    def randomInt = random.nextInt()
    def term = Term.findByName(randomInt+"")

    while(term){
      randomInt = random.nextInt()
      term = Term.findByName(randomInt+"")
   }

    term =  new Term(name:randomInt+"")
    term.validate()
    term
  }


  static RelationTerm createOrGetBasicRelationTerm() {
    log.debug  "createOrGetBasicRelationTerm()"
    def relation = createOrGetBasicRelation()
    def term1 = createOrGetBasicTerm()
    def term2 = createOrGetAnotherBasicTerm()

    def relationTerm = RelationTerm.findWhere('relation':relation,'term1':term1,'term2':term2)
    log.debug "relationTerm=" + relationTerm
    if(!relationTerm) {
      log.debug "relationTerm link"
      relationTerm = RelationTerm.link(relation,term1,term2)
      log.debug "relationTerm.errors="+relationTerm.errors
    }
    assert relationTerm!=null
    relationTerm
  }

  static RelationTerm getBasicRelationTermNotExist() {

    log.debug "getBasicRelationTermNotExist()"
    def random = new Random()
    def randomInt = random.nextInt()

    def relation = getBasicRelationNotExist()
    def term1 = getBasicTermNotExist()
    def term2 = getBasicTermNotExist()
    relation.save(flush:true)
    term1.save(flush:true)
    term2.save(flush:true)

    def relationTerm = RelationTerm.link(relation,term1,term2)
    log.debug "relationTerm.errors="+relationTerm.errors
    relationTerm
  }


  static AnnotationTerm createOrGetBasicAnnotationTerm() {
    log.debug  "createOrGetBasicAnnotationTerm()"
    def annotation = createOrGetBasicAnnotation()
    def term = getBasicTermNotExist()
    term.save(flush:true)
    assert term!=null
    def annotationTerm =  AnnotationTerm.findByAnnotationAndTerm(annotation,term)

    if(!annotationTerm) {
      log.debug "annotationTerm link"
      annotationTerm = AnnotationTerm.link(annotation,term)
      log.debug "AnnotationTerm.errors="+annotationTerm.errors
    }
    assert annotationTerm!=null
    annotationTerm
  }

  static AnnotationTerm getBasicAnnotationTermNotExist() {

    log.debug "getBasicAnnotationTermNotExist()"
    def random = new Random()
    def randomInt = random.nextInt()

    def annotation = createOrGetBasicAnnotation()
    def term = getBasicTermNotExist()
    term.save(flush:true)
    assert term!=null
    def annotationTerm = AnnotationTerm.link(annotation,term)
    log.debug "annotationTerm.errors="+annotationTerm.errors
    annotationTerm
  }


  static void compareAnnotation(map, json)  {

    assert map.geom.replace(' ', '').equals(json.annotation.location.replace(' ',''))
    assert toDouble(map.zoomLevel).equals(toDouble(json.annotation.zoomLevel))
    assert map.channels.equals(json.annotation.channels)
    assert toLong(map.user.id).equals(toLong(json.annotation.user))
  }

  static void compareImage(map, json)  {

    assert map.filename.equals(json.image.filename)
    assert map.geom.replace(' ', '').equals(json.image.roi.replace(' ',''))
    assert toLong(map.user.id).equals(toLong(json.image.user))
    assert toLong(map.scanner.id).equals(toLong(json.image.scanner))
    assert toLong(map.slide.id).equals(toLong(json.image.slide))
    assert map.path.equals(json.image.path)
    assert toLong(map.mime.id).equals(toLong(json.image.mime))
    assert toInteger(map.width).equals(toInteger(json.image.width))
    assert toInteger(map.height).equals(toInteger(json.image.height))
    assert toDouble(map.scale).equals(toDouble(json.image.scale))

  }

  static void compareProject(map, json)  {

    assert map.name.equals(json.project.name)

  }

    static void compareRelation(map, json)  {

    assert map.name.equals(json.relation.name)

  }

     static void compareTerm(map, json)  {

    assert map.name.equals(json.term.name)
    assert map.comment.equals(json.term.comment)

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
