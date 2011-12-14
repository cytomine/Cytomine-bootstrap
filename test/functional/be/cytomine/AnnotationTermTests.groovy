package be.cytomine

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.ontology.Annotation
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import be.cytomine.ontology.Term
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.security.User
import be.cytomine.test.http.AnnotationTermAPI
import be.cytomine.ontology.AnnotationTerm

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 22/02/11
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTermTests extends functionaltestplugin.FunctionalTestCase {

  void testGetAnnotationTermWithCredential() {
    User currentUser = User.findByUsername(Infos.GOODLOGIN)
    def annotationTermToAdd = BasicInstance.createOrGetBasicAnnotationTerm()
    log.info("get annotation")
    def result = AnnotationTermAPI.showAnnotationTerm(annotationTermToAdd.annotation.id,annotationTermToAdd.term.id,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    log.info("check response")
    assertEquals(200,result.code)
    def json = JSON.parse(result.data)
    assert json instanceof JSONObject
  }

  void testListAnnotationTermByAnnotationWithCredential() {
    Annotation annotation = BasicInstance.createOrGetBasicAnnotation()
    def result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotation.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    log.info("check response")
    assertEquals(200,result.code)
    def json = JSON.parse(result.data)
    assert json instanceof JSONArray
  }

  void testListAnnotationTermByTermWithCredential() {
    Term term = BasicInstance.createOrGetBasicTerm()
    def result = AnnotationTermAPI.listAnnotationTermByTerm(term.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    log.info("check response")
    assertEquals(200,result.code)
    def json = JSON.parse(result.data)
    assert json instanceof JSONArray
  }

  void testAddAnnotationTermCorrect() {
     User currentUser = User.findByUsername(Infos.GOODLOGIN)

    log.info("create AnnotationTerm")
    def annotationTermToAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermCorrect")
    annotationTermToAdd.discard()
    String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    log.info("check response")
    assertEquals(200,result.code)
    AnnotationTerm annotationTerm = result.data
    Long idAnnotation = annotationTerm.annotation.id
    Long idTerm = annotationTerm.term.id
    log.info("check if object "+ annotationTerm.annotation.id +"/"+ annotationTerm.term.id +"exist in DB")

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)

    log.info("test undo")
    result = AnnotationTermAPI.undo()
    assertEquals(200,result.code)

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(404,result.code)

    log.info("test redo")
    result = AnnotationTermAPI.redo()
    assertEquals(200,result.code)

    log.info("check if object "+ idAnnotation +"/"+ idTerm +" exist in DB")
    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)

  }

   void testAddAnnotationTermAlreadyExist() {

    log.info("create AnnotationTerm")
    def annotationTermToAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermAlreadyExist")
    annotationTermToAdd.save(flush:true)
    //annotationTermToAdd is in database, we will try to add it twice
    String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(400,result.code)
  }

  void testAddAnnotationTermWithAnnotationNotExist() {

    log.info("create annotationterm")
    def annotationTermAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermWithAnnotationNotExist")
    String jsonAnnotationTerm = annotationTermAdd.encodeAsJSON()
    log.info("jsonAnnotationTerm="+jsonAnnotationTerm)
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.annotation = -99
    jsonAnnotationTerm = jsonUpdate.encodeAsJSON()

    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(400,result.code)
  }

  void testAddAnnotationTermWithTermNotExist() {

    log.info("create annotationterm")
    def annotationTermAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermWithTermNotExist")
    String jsonAnnotationTerm = annotationTermAdd.encodeAsJSON()
    log.info("jsonAnnotationTerm="+jsonAnnotationTerm)
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.term = -99
    jsonAnnotationTerm = jsonUpdate.encodeAsJSON()

    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(400,result.code)

  }

  void testDeleteAnnotationTerm() {
    User currentUser = User.findByUsername(Infos.GOODLOGIN)

    log.info("create annotationTerm")
    def annotationTermToDelete = BasicInstance.createOrGetBasicAnnotationTerm()
    String jsonAnnotationTerm = annotationTermToDelete.encodeAsJSON()

    int idAnnotation = annotationTermToDelete.annotation.id
    int idTerm = annotationTermToDelete.term.id
    int idUser = currentUser.id
    log.info("delete annotationTerm:"+jsonAnnotationTerm.replace("\n",""))
    def result = AnnotationTermAPI.deleteAnnotationTerm(idAnnotation,idTerm,idUser,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,idUser,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(404,result.code)

    result = AnnotationTermAPI.undo()
    assertEquals(200,result.code)

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)

    log.info("test redo")
    result = AnnotationTermAPI.redo()
    assertEquals(200,result.code)

    log.info("check if object "+ idAnnotation +"/"+ idTerm +" exist in DB")
    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(404,result.code)

  }

  void testDeleteAnnotationTermNotExist() {
    def result = AnnotationTermAPI.deleteAnnotationTerm(-99,-99,-99,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(404,result.code)
  }
}
