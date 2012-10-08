package be.cytomine

import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.TermAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:31
 * To change this template use File | Settings | File Templates.
 */
class TermTests extends functionaltestplugin.FunctionalTestCase {


  void testListOntologyTermByOntologyWithCredential() {
      Ontology ontology = BasicInstance.createOrGetBasicOntology()
      def result = TermAPI.listByOntology(ontology.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testListTermOntologyByOntologyWithOntologyNotExist() {
      def result = TermAPI.listByOntology(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testListTermByImageWithCredential() {
      AnnotationTerm annotationTerm = BasicInstance.createOrGetBasicAnnotationTerm()
      def result = TermAPI.listByImage(annotationTerm.annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testListTermByImageWithImageNotExist() {
      def result = TermAPI.listByImage(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testListTermWithCredential() {
      def result = TermAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testShowTermWithCredential() {
      def result = TermAPI.show(BasicInstance.createOrGetBasicTerm().id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddTermCorrect() {
      def termToAdd = BasicInstance.getBasicTermNotExist()
      def result = TermAPI.create(termToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      int idTerm = result.data.id

      result = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)

      result = TermAPI.undo()
      assertEquals(200, result.code)

      result = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)

      result = TermAPI.redo()
      assertEquals(200, result.code)

      result = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
  }
    
    void testAddTermMultipleCorrect() {
        def termToAdd1 = BasicInstance.getBasicTermNotExist()
        def termToAdd2 = BasicInstance.getBasicTermNotExist()
        def terms = []
        terms << JSON.parse(termToAdd1.encodeAsJSON())
        terms << JSON.parse(termToAdd2.encodeAsJSON())
        def result = TermAPI.create(terms.encodeAsJSON() , Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }    

    void testAddTermAlreadyExist() {
       def termToAdd = BasicInstance.createOrGetBasicTerm()
       def result = TermAPI.create(termToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assertEquals(409, result.code)
   }
 
   void testUpdateTermCorrect() {
       Term termToAdd = BasicInstance.createOrGetBasicTerm()
       def result = TermAPI.update(termToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assertEquals(200, result.code)
       def json = JSON.parse(result.data)
       assert json instanceof JSONObject
       int idTerm = json.term.id
 
       def showResult = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       json = JSON.parse(showResult.data)
       BasicInstance.compareTerm(result.mapNew, json)
 
       showResult = TermAPI.undo()
       assertEquals(200, result.code)
       showResult = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       BasicInstance.compareTerm(result.mapOld, JSON.parse(showResult.data))
 
       showResult = TermAPI.redo()
       assertEquals(200, result.code)
       showResult = TermAPI.show(idTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       BasicInstance.compareTerm(result.mapNew, JSON.parse(showResult.data))
   }
 
   void testUpdateTermNotExist() {
       Term termWithOldName = BasicInstance.createOrGetBasicTerm()
       Term termWithNewName = BasicInstance.getBasicTermNotExist()
       termWithNewName.save(flush: true)
       Term termToEdit = Term.get(termWithNewName.id)
       def jsonTerm = termToEdit.encodeAsJSON()
       def jsonUpdate = JSON.parse(jsonTerm)
       jsonUpdate.name = termWithOldName.name
       jsonUpdate.id = -99
       jsonTerm = jsonUpdate.encodeAsJSON()
       def result = TermAPI.update(-99, jsonTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assertEquals(404, result.code)
   }
 
   void testUpdateTermWithNameAlreadyExist() {
       Term termWithOldName = BasicInstance.createOrGetBasicTerm()
       Term termWithNewName = BasicInstance.getBasicTermNotExist()
       termWithNewName.ontology = termWithOldName.ontology
       termWithNewName.save(flush: true)
       Term termToEdit = Term.get(termWithNewName.id)
       def jsonTerm = termToEdit.encodeAsJSON()
       def jsonUpdate = JSON.parse(jsonTerm)
       jsonUpdate.name = termWithOldName.name
       jsonTerm = jsonUpdate.encodeAsJSON()
       def result = TermAPI.update(termToEdit.id, jsonTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assertEquals(409, result.code)
   }
     
     void testEditTermWithBadName() {
         Term termToAdd = BasicInstance.createOrGetBasicTerm()
         Term termToEdit = Term.get(termToAdd.id)
         def jsonTerm = termToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonTerm)
         jsonUpdate.name = null
         jsonTerm = jsonUpdate.encodeAsJSON()
         def result = TermAPI.update(termToAdd.id, jsonTerm, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(400, result.code)
     }
 
   void testDeleteTerm() {
       def termToDelete = BasicInstance.getBasicTermNotExist()
       assert termToDelete.save(flush: true)!= null
       def id = termToDelete.id
       def result = TermAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assertEquals(200, result.code)
 
       def showResult = TermAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assertEquals(404, showResult.code)
 
       result = TermAPI.undo()
       assertEquals(200, result.code)
 
       result = TermAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assertEquals(200, result.code)
 
       result = TermAPI.redo()
       assertEquals(200, result.code)
 
       result = TermAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assertEquals(404, result.code)
   }
 
   void testDeleteTermNotExist() {
       def result = TermAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assertEquals(404, result.code)
   }
}
