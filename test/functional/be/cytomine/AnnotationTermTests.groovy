package be.cytomine

import be.cytomine.ontology.AnnotationTerm

import be.cytomine.security.User
import be.cytomine.test.BasicInstanceBuilder
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
class AnnotationTermTests  {

  void testGetAnnotationTermWithCredential() {
    User currentUser = User.findByUsername(Infos.GOODLOGIN)
    def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTerm()
    def result = AnnotationTermAPI.showAnnotationTerm(annotationTermToAdd.userAnnotation.id,annotationTermToAdd.term.id,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 200 == result.code
    def json = JSON.parse(result.data)
    assert json instanceof JSONObject
  }

  void testListAnnotationTermByAnnotationWithCredential() {
    def result = AnnotationTermAPI.listAnnotationTermByAnnotation(BasicInstanceBuilder.getUserAnnotation().id,null,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 200 == result.code
    def json = JSON.parse(result.data)
    assert json.collection instanceof JSONArray

    result = AnnotationTermAPI.listAnnotationTermByAnnotation(-99,null,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 404 == result.code
  }

    void testListAnnotationTermByAnnotationWithCredentialWithUser() {
      def result = AnnotationTermAPI.listAnnotationTermByAnnotation(BasicInstanceBuilder.getUserAnnotation().id,BasicInstanceBuilder.user1.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray

      result = AnnotationTermAPI.listAnnotationTermByAnnotation(-99,BasicInstanceBuilder.user1.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 404 == result.code
    }

    void testListAnnotationTermByUserNotWithCredential() {
      def result = AnnotationTermAPI.listAnnotationTermByUserNot(BasicInstanceBuilder.getUserAnnotation().id,BasicInstanceBuilder.user1.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
    }

  void testAddAnnotationTermCorrect() {
     User currentUser = User.findByUsername(Infos.GOODLOGIN)
    def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
    annotationTermToAdd.discard()
    String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 200 == result.code

    AnnotationTerm annotationTerm = result.data
    Long idAnnotation = annotationTerm.userAnnotation.id
    Long idTerm = annotationTerm.term.id
    log.info("check if object "+ annotationTerm.userAnnotation.id +"/"+ annotationTerm.term.id +"exist in DB")

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 200 == result.code

    result = AnnotationTermAPI.undo()
    assert 200 == result.code

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 404 == result.code

    result = AnnotationTermAPI.redo()
    assert 200 == result.code

    log.info("check if object "+ idAnnotation +"/"+ idTerm +" exist in DB")
    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 200 == result.code

  }

    void testAddAnnotationTermWithTermFromOtherOntology() {
         User currentUser = User.findByUsername(Infos.GOODLOGIN)
        def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
        annotationTermToAdd.discard()
        annotationTermToAdd.term = BasicInstanceBuilder.getTermNotExist(true)
        String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
        def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testAddAnnotationTermCorrectDeletingOldTerm() {
       User currentUser = User.findByUsername(Infos.GOODLOGIN)
      def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
      annotationTermToAdd.discard()
      String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
      def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD,true)
      assert 200 == result.code

      AnnotationTerm annotationTerm = result.data
      Long idAnnotation = annotationTerm.userAnnotation.id
      Long idTerm = annotationTerm.term.id
      log.info("check if object "+ annotationTerm.userAnnotation.id +"/"+ annotationTerm.term.id +"exist in DB")

      result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 200 == result.code

      result = AnnotationTermAPI.undo()
      assert 200 == result.code

      result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 404 == result.code

      result = AnnotationTermAPI.redo()
      assert 200 == result.code

      log.info("check if object "+ idAnnotation +"/"+ idTerm +" exist in DB")
      result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
      assert 200 == result.code

    }

  void testAddAnnotationTermAlreadyExist() {
    def annotationTermToAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
    annotationTermToAdd.save(flush:true)
    //annotationTermToAdd is in database, we will try to add it twice
    String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 409 == result.code
  }

  void testAddAnnotationTermWithAnnotationNotExist() {
    def annotationTermAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
    String jsonAnnotationTerm = annotationTermAdd.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.userannotation = -99
    jsonAnnotationTerm = jsonUpdate.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 400 == result.code
  }

  void testAddAnnotationTermWithTermNotExist() {
    def annotationTermAdd = BasicInstanceBuilder.getAnnotationTermNotExist()
    String jsonAnnotationTerm = annotationTermAdd.encodeAsJSON()
    def jsonUpdate = JSON.parse(jsonAnnotationTerm)
    jsonUpdate.term = -99
    jsonAnnotationTerm = jsonUpdate.encodeAsJSON()
    def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 400 == result.code
  }

  void testDeleteAnnotationTerm() {
    User currentUser = User.findByUsername(Infos.GOODLOGIN)

    def annotationTermToDelete = BasicInstanceBuilder.getAnnotationTerm()
    int idAnnotation = annotationTermToDelete.userAnnotation.id
    int idTerm = annotationTermToDelete.term.id
    int idUser = currentUser.id
    assert annotationTermToDelete.userAnnotation.project.ontology==annotationTermToDelete.term.ontology

    def result = AnnotationTermAPI.deleteAnnotationTerm(idAnnotation,idTerm,idUser,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 200 == result.code

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,idUser,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 404 == result.code

    result = AnnotationTermAPI.undo()
    assert 200 == result.code

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 200 == result.code

    result = AnnotationTermAPI.redo()
    assert 200 == result.code

    result = AnnotationTermAPI.showAnnotationTerm(idAnnotation,idTerm,currentUser.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 404 == result.code

  }

  void testDeleteAnnotationTermNotExist() {
    def result = AnnotationTermAPI.deleteAnnotationTerm(-99,-99,-99,Infos.GOODLOGIN,Infos.GOODPASSWORD)
    assert 404 == result.code
  }
}
