package be.cytomine

import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationTermAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

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
    def result = AnnotationTermAPI.showAnnotationTerm(annotationTermToAdd.userAnnotation.id,annotationTermToAdd.term.id,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)
    def json = JSON.parse(result.data)
    assert json instanceof JSONObject
  }

  void testListAnnotationTermByAnnotationWithCredential() {
    def result = AnnotationTermAPI.listAnnotationTermByAnnotation(BasicInstance.createOrGetBasicUserAnnotation().id,null,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)
    def json = JSON.parse(result.data)
    assert json instanceof JSONArray

    result = AnnotationTermAPI.listAnnotationTermByAnnotation(-99,null,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(404,result.code)
  }

    void testListAnnotationTermByAnnotationWithCredentialWithUser() {
      def result = AnnotationTermAPI.listAnnotationTermByAnnotation(BasicInstance.createOrGetBasicUserAnnotation().id,BasicInstance.newUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200,result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray

      result = AnnotationTermAPI.listAnnotationTermByAnnotation(-99,BasicInstance.newUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(404,result.code)
    }

    void testListAnnotationTermByUserNotWithCredential() {
      def result = AnnotationTermAPI.listAnnotationTermByUserNot(BasicInstance.createOrGetBasicUserAnnotation().id,BasicInstance.newUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200,result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
    }

  void testAddAnnotationTermCorrect() {
     User currentUser = User.findByUsername(Infos.GOODLOGIN)
    def annotationTermToAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermCorrect")
    annotationTermToAdd.discard()
    String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)

    AnnotationTerm annotationTerm = result.data
    Long idAnnotation = annotationTerm.userAnnotation.id
    Long idTerm = annotationTerm.term.id
    log.info("check if object "+ annotationTerm.userAnnotation.id +"/"+ annotationTerm.term.id +"exist in DB")

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)

    result = AnnotationTermAPI.undo()
    assertEquals(200,result.code)

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(404,result.code)

    result = AnnotationTermAPI.redo()
    assertEquals(200,result.code)

    log.info("check if object "+ idAnnotation +"/"+ idTerm +" exist in DB")
    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)

  }

    void testAddAnnotationTermCorrectDeletingOldTerm() {
       User currentUser = User.findByUsername(Infos.GOODLOGIN)
      def annotationTermToAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermCorrect")
      annotationTermToAdd.discard()
      String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
      def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD,true)
      assertEquals(200,result.code)

      AnnotationTerm annotationTerm = result.data
      Long idAnnotation = annotationTerm.userAnnotation.id
      Long idTerm = annotationTerm.term.id
      log.info("check if object "+ annotationTerm.userAnnotation.id +"/"+ annotationTerm.term.id +"exist in DB")

      result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200,result.code)

      result = AnnotationTermAPI.undo()
      assertEquals(200,result.code)

      result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(404,result.code)

      result = AnnotationTermAPI.redo()
      assertEquals(200,result.code)

      log.info("check if object "+ idAnnotation +"/"+ idTerm +" exist in DB")
      result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assertEquals(200,result.code)

    }

  void testAddAnnotationTermAlreadyExist() {
    def annotationTermToAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermAlreadyExist")
    annotationTermToAdd.save(flush:true)
    //annotationTermToAdd is in database, we will try to add it twice
    String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(409,result.code)
  }

  void testAddAnnotationTermWithAnnotationNotExist() {
    def annotationTermAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermWithAnnotationNotExist")
    String jsonAnnotationTerm = annotationTermAdd.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.userannotation = -99
    jsonAnnotationTerm = jsonUpdate.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(400,result.code)
  }

  void testAddAnnotationTermWithTermNotExist() {
    def annotationTermAdd = BasicInstance.getBasicAnnotationTermNotExist("testAddAnnotationTermWithTermNotExist")
    String jsonAnnotationTerm = annotationTermAdd.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.term = -99
    jsonAnnotationTerm = jsonUpdate.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(400,result.code)
  }

  void testDeleteAnnotationTerm() {
    User currentUser = User.findByUsername(Infos.GOODLOGIN)

    def annotationTermToDelete = BasicInstance.createOrGetBasicAnnotationTerm()
    int idAnnotation = annotationTermToDelete.userAnnotation.id
    int idTerm = annotationTermToDelete.term.id
    int idUser = currentUser.id

    def result = AnnotationTermAPI.deleteAnnotationTerm(idAnnotation,idTerm,idUser,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,idUser,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(404,result.code)

    result = AnnotationTermAPI.undo()
    assertEquals(200,result.code)

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(200,result.code)

    result = AnnotationTermAPI.redo()
    assertEquals(200,result.code)

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(404,result.code)

  }

  void testDeleteAnnotationTermNotExist() {
    def result = AnnotationTermAPI.deleteAnnotationTerm(-99,-99,-99,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assertEquals(404,result.code)
  }
}
