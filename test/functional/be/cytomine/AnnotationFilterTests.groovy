package be.cytomine

import be.cytomine.ontology.AnnotationFilter
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationFilterAPI
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
class AnnotationFilterTests  {
    

  void testListAnnotationFilterByProject() {
      def result = AnnotationFilterAPI.listByProject(BasicInstanceBuilder.getProject().id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray

      result = AnnotationFilterAPI.listByProject(-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

    void testListAnnotationFilterByOntology() {
        def result = AnnotationFilterAPI.listByOntology(BasicInstanceBuilder.getProject().ontology.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = AnnotationFilterAPI.listByOntology(-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }


  void testShowAnnotationFilterWithCredential() {
      def result = AnnotationFilterAPI.show(BasicInstanceBuilder.getAnnotationFilter().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testAddAnnotationFilterCorrect() {
      def annotationfilterToAdd = BasicInstanceBuilder.getAnnotationFilterNotExist()
      def result = AnnotationFilterAPI.create(annotationfilterToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      int idAnnotationFilter = result.data.id

      result = AnnotationFilterAPI.show(idAnnotationFilter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
  }

  void testUpdateAnnotationFilterCorrect() {
      def af =  BasicInstanceBuilder.getAnnotationFilter()
      def data = UpdateData.createUpdateSet(af,[name: ["OLDNAME","NEWNAME"]])
      def result = AnnotationFilterAPI.update(af.id, data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int idAnnotationFilter = json.annotationfilter.id

      def showResult = AnnotationFilterAPI.show(idAnnotationFilter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstanceBuilder.compare(data.mapNew, json)
  }

  void testUpdateAnnotationFilterNotExist() {
      AnnotationFilter annotationfilterWithOldName = BasicInstanceBuilder.getAnnotationFilter()
      AnnotationFilter annotationfilterWithNewName = BasicInstanceBuilder.getAnnotationFilterNotExist()
      annotationfilterWithNewName.save(flush: true)
      AnnotationFilter annotationfilterToEdit = AnnotationFilter.get(annotationfilterWithNewName.id)
      def jsonAnnotationFilter = annotationfilterToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonAnnotationFilter)
      jsonUpdate.name = annotationfilterWithOldName.name
      jsonUpdate.id = -99
      jsonAnnotationFilter = jsonUpdate.toString()
      def result = AnnotationFilterAPI.update(-99, jsonAnnotationFilter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

    void testEditAnnotationFilterWithBadName() {
        AnnotationFilter annotationfilterToAdd = BasicInstanceBuilder.getAnnotationFilter()
        AnnotationFilter annotationfilterToEdit = AnnotationFilter.get(annotationfilterToAdd.id)
        def jsonAnnotationFilter = annotationfilterToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotationFilter)
        jsonUpdate.name = null
        jsonAnnotationFilter = jsonUpdate.toString()
        def result = AnnotationFilterAPI.update(annotationfilterToAdd.id, jsonAnnotationFilter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

  void testDeleteAnnotationFilter() {
      def annotationfilterToDelete = BasicInstanceBuilder.getAnnotationFilterNotExist()
      assert annotationfilterToDelete.save(flush: true)!= null
      def id = annotationfilterToDelete.id
      def result = AnnotationFilterAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      def showResult = AnnotationFilterAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == showResult.code
  }

  void testDeleteAnnotationFilterNotExist() {
      def result = AnnotationFilterAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }
}
