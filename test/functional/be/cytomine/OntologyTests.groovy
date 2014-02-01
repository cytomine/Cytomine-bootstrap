package be.cytomine

import be.cytomine.ontology.Ontology
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.OntologyAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class OntologyTests  {

    void testListOntologyWithCredential() {
        def result = OntologyAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }
  
    void testListOntologyWithoutCredential() {
        def result = OntologyAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }

    void testListOntologyLightWithCredential() {
        def result = OntologyAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD,true)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }
  
    void testShowOntologyWithCredential() {
        def result = OntologyAPI.show(BasicInstanceBuilder.getOntology().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }
  
    void testAddOntologyCorrect() {
        def ontologyToAdd = BasicInstanceBuilder.getOntologyNotExist()
        def result = OntologyAPI.create(ontologyToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        int idOntology = result.data.id
  
        result = OntologyAPI.show(idOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
  
        result = OntologyAPI.undo()
        assert 200 == result.code
  
        result = OntologyAPI.show(idOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
  
        result = OntologyAPI.redo()
        assert 200 == result.code
  
        result = OntologyAPI.show(idOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }
  
    void testAddOntologyAlreadyExist() {
        def ontologyToAdd = BasicInstanceBuilder.getOntology()
        def result = OntologyAPI.create(ontologyToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }
  
    void testUpdateOntologyCorrect() {
        Ontology ontologyToAdd = BasicInstanceBuilder.getOntology()
        def data = UpdateData.createUpdateSet(ontologyToAdd,[name: ["OLDNAME","NEWNAME"], user:[BasicInstanceBuilder.user1,BasicInstanceBuilder.user2]])
        def result = OntologyAPI.update(ontologyToAdd.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idOntology = json.ontology.id
  
        def showResult = OntologyAPI.show(idOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)
  
        showResult = OntologyAPI.undo()
        assert 200 == result.code
        showResult = OntologyAPI.show(idOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))
  
        showResult = OntologyAPI.redo()
        assert 200 == result.code
        showResult = OntologyAPI.show(idOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
    }
  
    void testUpdateOntologyNotExist() {
        Ontology ontologyWithOldName = BasicInstanceBuilder.getOntology()
        Ontology ontologyWithNewName = BasicInstanceBuilder.getOntologyNotExist()
        ontologyWithNewName.save(flush: true)
        Ontology ontologyToEdit = Ontology.get(ontologyWithNewName.id)
        def jsonOntology = ontologyToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonOntology)
        jsonUpdate.name = ontologyWithOldName.name
        jsonUpdate.id = -99
        jsonOntology = jsonUpdate.toString()
        def result = OntologyAPI.update(-99, jsonOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
  
    void testUpdateOntologyWithNameAlreadyExist() {
        Ontology ontologyWithOldName = BasicInstanceBuilder.getOntology()
        Ontology ontologyWithNewName = BasicInstanceBuilder.getOntologyNotExist()
        ontologyWithNewName.save(flush: true)
        Ontology ontologyToEdit = Ontology.get(ontologyWithNewName.id)
        def jsonOntology = ontologyToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonOntology)
        jsonUpdate.name = ontologyWithOldName.name
        jsonOntology = jsonUpdate.toString()
        def result = OntologyAPI.update(ontologyToEdit.id, jsonOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }
      
      void testEditOntologyWithBadName() {
          Ontology ontologyToAdd = BasicInstanceBuilder.getOntology()
          Ontology ontologyToEdit = Ontology.get(ontologyToAdd.id)
          def jsonOntology = ontologyToEdit.encodeAsJSON()
          def jsonUpdate = JSON.parse(jsonOntology)
          jsonUpdate.name = null
          jsonOntology = jsonUpdate.toString()
          def result = OntologyAPI.update(ontologyToAdd.id, jsonOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 400 == result.code
      }
  
    void testDeleteOntology() {
        def ontologyToDelete = BasicInstanceBuilder.getOntologyNotExist()
        assert ontologyToDelete.save(flush: true)!= null
        def id = ontologyToDelete.id
        def result = OntologyAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
  
        def showResult = OntologyAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code
  
        result = OntologyAPI.undo()
        assert 200 == result.code
  
        result = OntologyAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
  
        result = OntologyAPI.redo()
        assert 200 == result.code
  
        result = OntologyAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
  
    void testDeleteOntologyNotExist() {
        def result = OntologyAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
  
    void testDeleteOntologyWithProject() {
        def project = BasicInstanceBuilder.getProject()
        def ontologyToDelete = project.ontology
        def result = OntologyAPI.delete(ontologyToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

  void testDeleteOntologyWithTerms() {

    log.info("create ontology")
    //create project and try to delete his ontology
    def relationTerm = BasicInstanceBuilder.getRelationTermNotExist()
    relationTerm.save(flush:true)
    def ontologyToDelete = relationTerm.term1.ontology
    assert ontologyToDelete.save(flush:true)!=null
    int idOntology = ontologyToDelete.id
      def result = OntologyAPI.delete(idOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code

      def showResult = OntologyAPI.show(idOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == showResult.code

      result = OntologyAPI.undo()
      assert 200 == result.code

      result = OntologyAPI.show(idOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code

      result = OntologyAPI.redo()
      assert 200 == result.code

      result = OntologyAPI.show(idOntology, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code

  }
}
