package be.cytomine

import be.cytomine.test.Infos

import be.cytomine.test.BasicInstance
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.test.http.AnnotationFilterAPI
import be.cytomine.ontology.AnnotationFilter

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class AnnotationFilterTests  {
    

  void testListAnnotationFilterByProject() {
      def result = AnnotationFilterAPI.listByProject(BasicInstance.createOrGetBasicProject().id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray

      result = AnnotationFilterAPI.listByProject(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }

    void testListAnnotationFilterByOntology() {
        def result = AnnotationFilterAPI.listByOntology(BasicInstance.createOrGetBasicProject().ontology.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = AnnotationFilterAPI.listByOntology(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


  void testShowAnnotationFilterWithCredential() {
      def result = AnnotationFilterAPI.show(BasicInstance.createOrGetBasicAnnotationFilter().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddAnnotationFilterCorrect() {
      def annotationfilterToAdd = BasicInstance.getBasicAnnotationFilterNotExist()
      def result = AnnotationFilterAPI.create(annotationfilterToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      int idAnnotationFilter = result.data.id

      result = AnnotationFilterAPI.show(idAnnotationFilter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
  }

  void testUpdateAnnotationFilterCorrect() {
      AnnotationFilter annotationfilterToAdd = BasicInstance.createOrGetBasicAnnotationFilter()

      def data = UpdateData.createUpdateSet(annotationfilterToAdd)
      def result = AnnotationFilterAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int idAnnotationFilter = json.annotationfilter.id

      def showResult = AnnotationFilterAPI.show(idAnnotationFilter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstance.compareAnnotationFilter(data.mapNew, json)
  }

  void testUpdateAnnotationFilterNotExist() {
      AnnotationFilter annotationfilterWithOldName = BasicInstance.createOrGetBasicAnnotationFilter()
      AnnotationFilter annotationfilterWithNewName = BasicInstance.getBasicAnnotationFilterNotExist()
      annotationfilterWithNewName.save(flush: true)
      AnnotationFilter annotationfilterToEdit = AnnotationFilter.get(annotationfilterWithNewName.id)
      def jsonAnnotationFilter = annotationfilterToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonAnnotationFilter)
      jsonUpdate.name = annotationfilterWithOldName.name
      jsonUpdate.id = -99
      jsonAnnotationFilter = jsonUpdate.encodeAsJSON()
      def result = AnnotationFilterAPI.update(-99, jsonAnnotationFilter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }

    void testEditAnnotationFilterWithBadName() {
        AnnotationFilter annotationfilterToAdd = BasicInstance.createOrGetBasicAnnotationFilter()
        AnnotationFilter annotationfilterToEdit = AnnotationFilter.get(annotationfilterToAdd.id)
        def jsonAnnotationFilter = annotationfilterToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotationFilter)
        jsonUpdate.name = null
        jsonAnnotationFilter = jsonUpdate.encodeAsJSON()
        def result = AnnotationFilterAPI.update(annotationfilterToAdd.id, jsonAnnotationFilter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

  void testDeleteAnnotationFilter() {
      def annotationfilterToDelete = BasicInstance.getBasicAnnotationFilterNotExist()
      assert annotationfilterToDelete.save(flush: true)!= null
      def id = annotationfilterToDelete.id
      def result = AnnotationFilterAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code

      def showResult = AnnotationFilterAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == showResult.code
  }

  void testDeleteAnnotationFilterNotExist() {
      def result = AnnotationFilterAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }
}
