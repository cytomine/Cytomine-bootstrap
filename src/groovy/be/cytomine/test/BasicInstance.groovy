package be.cytomine.test

import be.cytomine.image.Mime
import be.cytomine.ontology.Annotation
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.image.AbstractImage
import be.cytomine.image.acquisition.Scanner
import be.cytomine.ontology.Term
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import be.cytomine.security.User
import be.cytomine.project.Slide
import be.cytomine.project.Project
import be.cytomine.ontology.Relation
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Ontology
import be.cytomine.image.ImageInstance

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

  static Mime getBasicMimeNotExist() {
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
    def annotation = new Annotation(location:new WKTReader().read("POINT(17573.5 21853.5)"), name:"test",image:createOrGetBasicImageInstance(), user:createOrGetBasicUser())
    annotation.validate()
    log.debug("annotation.errors="+annotation.errors)
    annotation.save(flush : true)
    log.debug("annotation.errors="+annotation.errors)
    assert annotation!=null
    annotation
  }

  static Annotation getBasicAnnotationNotExist() {

    log.debug "getBasicAnnotationNotExist()"
    def random = new Random()
    def randomInt = random.nextInt()
    def annotation = Annotation.findByName(randomInt+"")

    while(annotation){
      randomInt = random.nextInt()
      annotation = Annotation.findByName(randomInt+"")
   }

    annotation =  new Annotation(location:new WKTReader().read("POINT(17573.5 21853.5)"), name:randomInt,image:createOrGetBasicImageInstance(), user:createOrGetBasicUser())
    annotation.validate()
    annotation
  }

  static ImageInstance getBasicImageInstanceNotExist() {

    log.debug "getBasicImageNotExist()"

    ImageInstance image =  new ImageInstance(
            baseImage:BasicInstance.createOrGetBasicAbstractImage(),
            project:BasicInstance.createOrGetBasicProject(),
            user:BasicInstance.createOrGetBasicUser())
    image.validate()
    log.debug "ImageInstance.errors="+image.errors
    image
  }

  static ImageInstance createOrGetBasicImageInstance() {
    log.debug  "createOrGetBasicImage()"
    ImageInstance image =  new ImageInstance(
            baseImage:BasicInstance.createOrGetBasicAbstractImage(),
            project:BasicInstance.createOrGetBasicProject(),
            user:BasicInstance.createOrGetBasicUser())
    image.validate()
    log.debug "ImageInstance.errors="+image.errors
    image.save(flush : true)
    log.debug "ImageInstance.errors="+image.errors
    assert image!=null
    image
  }

  static AbstractImage getBasicAbstractImageNotExist() {

    log.debug "getBasicImageNotExist()"
    def random = new Random()
    def randomInt = random.nextInt()
    def image = AbstractImage.findByFilename(randomInt+"")

    while(image){
      randomInt = random.nextInt()
      image = AbstractImage.findByFilename(randomInt+"")
   }

    image =  new AbstractImage(filename: randomInt,scanner : createOrGetBasicScanner() ,slide : null,mime:BasicInstance.createOrGetBasicMime(),path:"pathpathpath")
    image.validate()
    log.debug "AbstractImage.errors="+image.errors
    image
  }

  static AbstractImage createOrGetBasicAbstractImage() {
    log.debug  "createOrGetBasicImage()"
    def image = new AbstractImage(filename: "filename",scanner : createOrGetBasicScanner() ,slide : null,mime:BasicInstance.createOrGetBasicMime(),path:"pathpathpath")
    image.validate()
    log.debug "image.errors="+image.errors
    image.save(flush : true)
    log.debug "image.errors="+image.errors
    assert image!=null
    image
  }

  static Scanner createOrGetBasicScanner() {

    log.debug  "createOrGetBasicScanner()"
    Scanner scanner = new Scanner(maxResolution:"x40",brand:"brand", model:"model")
    scanner.validate()
    log.debug "scanner.errors="+scanner.errors
    scanner.save(flush : true)
    log.debug "scanner.errors="+scanner.errors
    assert scanner!=null
    scanner

  }

  static Scanner getNewScannerNotExist() {

    log.debug  "getNewScannerNotExist()"
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

  static Slide getBasicSlideNotExist() {

    log.debug  "getBasicSlideNotExist()"
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

  /* User user = new User(
                username : item.username,
                firstname : item.firstname,
                lastname : item.lastname,
                email : item.email,
                password : springSecurityService.encodePassword("password"),
                dateCreated : new Date(),
                enabled : true)*/


  static User createOrGetBasicUser() {

log.debug  "createOrGetBasicUser()"
    def user = User.findByUsername("BasicUser")
    if(!user) {
       user = new User(
                username : "BasicUser",
                firstname : "Basic",
                lastname : "User",
                email : "Basic@User.be",
                password : "password",
                enabled : true)
      user.validate()
      log.debug "user.errors="+user.errors
      user.save(flush : true)
      log.debug "user.errors="+user.errors
    }
    assert user!=null
    user
  }

  static User getBasicUserNotExist() {

    log.debug "getBasicUserNotExist()"
    def random = new Random()
    def randomInt = random.nextInt()
    def user = User.findByUsername(randomInt+"")

    while(user){
      randomInt = random.nextInt()
      user = User.findByUsername(randomInt+"")
   }

    user = new User(
                username : randomInt+"",
                firstname : "BasicNotExist",
                lastname : "UserNotExist",
                email : "BasicNotExist@User.be",
                password : "password",
                enabled : true)
    assert user.validate()==true
    log.debug "user.errors="+user.errors
    user
  }

  static Project createOrGetBasicProject() {
    log.debug  "createOrGetBasicProject()"
    def name = "BasicProject".toUpperCase()
    def project = Project.findByName(name)
    if(!project) {

      project = new Project(name:name, ontology:createOrGetBasicOntology())
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

    project =  new Project(name:randomInt+"",ontology:createOrGetBasicOntology())

    log.debug "getBasicProjectNotExist() validate="+project.validate()
    log.debug "getBasicProjectNotExist() project="+project
    project
  }

  static Ontology createOrGetBasicOntology() {
    log.debug  "createOrGetBasicOntology()"
    def ontology = Ontology.findByName("BasicOntology")
    if(!ontology) {

      ontology = new Ontology(name:"BasicOntology")
      ontology.validate()
      log.debug "ontology.errors="+ontology.errors
      ontology.save(flush : true)
      log.debug "ontology.errors="+ontology.errors
    }
    assert ontology!=null
    ontology
  }

  static Ontology getBasicOntologyNotExist() {

    log.debug "getBasicOntologyNsotExist()"
    def random = new Random()
    def randomInt = random.nextInt()
    def ontology = Ontology.findByName(randomInt+"")

    while(ontology){
      randomInt = random.nextInt()
      ontology = Ontology.findByName(randomInt+"")
   }

    ontology =  new Ontology(name:randomInt+"")
    ontology.validate()
   ontology
  }

  static Term createOrGetBasicTerm() {
    log.debug  "createOrGetBasicTerm()"
    def term = Term.findByName("BasicTerm")
    if(!term) {

      term = new Term(name:"BasicTerm", ontology:createOrGetBasicOntology(),color:"FF0000")
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

      term = new Term(name:"AnotherBasicTerm",ontology:createOrGetBasicOntology(),color:"F0000F")
      term.validate()
      log.debug "term.errors="+term.errors
      term.save(flush : true)
      log.debug "term.errors="+term.errors
    }
    assert term!=null
    term
  }

  static Term getBasicTermNotExist() {

    log.debug "getBasicTermNotExist() start"
    def random = new Random()
    def randomInt = random.nextInt()
    def term = Term.findByName(randomInt+"")

    while(term){
      randomInt = random.nextInt()
      term = Term.findByName(randomInt+"")
   }

    term =  new Term(name:randomInt+"",ontology:createOrGetBasicOntology(),color:"0F00F0")
    term.validate()
    log.debug "getBasicTermNotExist() end"
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

    def relationTerm = new RelationTerm(relation:relation,term1:term1,term2:term2)
    log.debug "relationTerm.errors="+relationTerm.errors
    assert relationTerm!=null
    relationTerm
  }

  static AnnotationTerm createOrGetBasicAnnotationTerm() {
    log.debug  "createOrGetBasicAnnotationTerm()"

    def annotation = getBasicAnnotationNotExist()
    annotation.save(flush:true)
    assert annotation!=null
    def term = getBasicTermNotExist()
    term.save(flush:true)
    assert term!=null
    def annotationTerm =  AnnotationTerm.findByAnnotationAndTerm(annotation,term)
    assert annotationTerm==null

    log.debug "annotation.id:" + annotation.id + " term.id:" + term.id
    if(!annotationTerm) {
      log.debug "annotationTerm link"

      annotationTerm = AnnotationTerm.link(annotation,term)
      log.debug "AnnotationTerm.errors="+annotationTerm.errors
    }
    assert annotationTerm!=null
    annotationTerm
  }

  static AnnotationTerm getBasicAnnotationTermNotExist(String method) {

    log.debug "getBasicAnnotationTermNotExist()"
    def random = new Random()
    def randomInt = random.nextInt()

    def term = getBasicTermNotExist()

    log.debug "term:" + term.id
    log.debug "term.name" + term.name
    log.debug "term.created:" + term.created
     log.debug "term.attached:" + term.attached
     log.debug "term.dirty:" + term.dirty

    term.save(flush:true)
    assert term!=null

    def annotation = getBasicAnnotationNotExist()

    log.debug "annotation:" + annotation.id
    annotation.save(flush:true)
    assert annotation!=null
    def annotationTerm =  new AnnotationTerm(annotation:annotation,term:term)

    log.debug "annotationTerm.errors="+annotationTerm.errors
    annotationTerm
  }

  static void compareAnnotation(map, json)  {

    assert map.geom.replace(' ', '').equals(json.location.replace(' ',''))
    assert toDouble(map.zoomLevel).equals(toDouble(json.zoomLevel))
    assert map.channels.equals(json.channels)
    assert toLong(map.user.id).equals(toLong(json.user))
  }

  static void compareAbstractImage(map, json)  {

    assert map.filename.equals(json.filename)
    assert map.geom.replace(' ', '').equals(json.roi.replace(' ',''))
    assert toLong(map.scanner.id).equals(toLong(json.scanner))
    assert toLong(map.slide.id).equals(toLong(json.slide))
    assert toLong(map.user.id).equals(toLong(json.user))
    assert map.path.equals(json.path)
    assert map.mime.extension.equals(json.mime)
    assert toInteger(map.width).equals(toInteger(json.width))
    assert toInteger(map.height).equals(toInteger(json.height))
    assert toDouble(map.scale).equals(toDouble(json.scale))

  }

  static void compareImageInstance(map, json)  {

    assert toLong(map.baseImage.id).equals(toLong(json.baseImage))
    assert toLong(map.project.id).equals(toLong(json.project))
    assert toLong(map.user.id).equals(toLong(json.user))

  }

  static void compareProject(map, json)  {

    assert map.name.toUpperCase().equals(json.name)
    assert toLong(map.ontology.id).equals(toLong(json.ontology))

  }


    static void compareRelation(map, json)  {

    assert map.name.equals(json.name)

  }

  static void compareTerm(map, json)  {

    assert map.name.equals(json.name)
    assert map.comment.equals(json.comment)
    assert map.color.equals(json.color)
    assert toLong(map.ontology.id).equals(toLong(json.ontology))

  }

  static void compareUser(map, json)  {

    assert map.firstname.equals(json.firstname)
    assert map.lastname.equals(json.lastname)
    assert map.email.equals(json.email)
    assert map.username.equals(json.username)

  }

  static void compareOntology(map, json)  {

    assert map.name.equals(json.name)

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
