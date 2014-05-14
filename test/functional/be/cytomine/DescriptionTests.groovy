package be.cytomine

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.DescriptionAPI
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
class DescriptionTests {

  void testListDescriptionWithCredential() {
      def result = DescriptionAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testShowDescriptionWithCredential() {
      def description = BasicInstanceBuilder.getDescriptionNotExist(BasicInstanceBuilder.getProjectNotExist(true),true)
      description.data = "<TEST>"
      description = BasicInstanceBuilder.saveDomain(description)
      println description.domainIdent
      println description.domainClassName
      def result = DescriptionAPI.show(description.domainIdent,description.domainClassName,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      assert json.data.equals(description.data)
  }

  void testAddDescriptionCorrect() {
      def descriptionToAdd = BasicInstanceBuilder.getDescriptionNotExist(BasicInstanceBuilder.getProjectNotExist(true),false)
      def result = DescriptionAPI.create(descriptionToAdd.domainIdent,descriptionToAdd.domainClassName,descriptionToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      int idDescription = result.data.id

      result =DescriptionAPI.show(descriptionToAdd.domainIdent,descriptionToAdd.domainClassName,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = DescriptionAPI.undo()
      assert 200 == result.code

      result = DescriptionAPI.show(descriptionToAdd.domainIdent,descriptionToAdd.domainClassName,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code

      result = DescriptionAPI.redo()
      assert 200 == result.code

      result =DescriptionAPI.show(descriptionToAdd.domainIdent,descriptionToAdd.domainClassName,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
  }

  void testAddDescriptionAlreadyExist() {
      def descriptionToAdd = BasicInstanceBuilder.getDescriptionNotExist(BasicInstanceBuilder.getProjectNotExist(true),true)
      def result = DescriptionAPI.create(descriptionToAdd.domainIdent,descriptionToAdd.domainClassName,descriptionToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 409 == result.code
  }

  void testUpdateDescriptionCorrect() {
      def description = BasicInstanceBuilder.getDescriptionNotExist(BasicInstanceBuilder.getProjectNotExist(true),true)
      def data = UpdateData.createUpdateSet(description,[data: ["OLDdata","NEWdata"]])
      def result = DescriptionAPI.update(description.domainIdent, description.domainClassName,data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
      int idDescription = json.description.id

      def showResult =  DescriptionAPI.show(description.domainIdent,description.domainClassName,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      json = JSON.parse(showResult.data)
      BasicInstanceBuilder.compare(data.mapNew, json)

      showResult = DescriptionAPI.undo()
      assert 200 == result.code
      showResult =  DescriptionAPI.show(description.domainIdent,description.domainClassName,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

      showResult = DescriptionAPI.redo()
      assert 200 == result.code
      showResult =  DescriptionAPI.show(description.domainIdent,description.domainClassName,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
  }


  void testDeleteDescription() {
      def descriptionToDelete = BasicInstanceBuilder.getDescriptionNotExist(BasicInstanceBuilder.getProjectNotExist(true),true)
      assert descriptionToDelete.save(flush: true)!= null
      def id = descriptionToDelete.id
      def result = DescriptionAPI.delete(descriptionToDelete.domainIdent,descriptionToDelete.domainClassName, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      def showResult = DescriptionAPI.show(descriptionToDelete.domainIdent,descriptionToDelete.domainClassName, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == showResult.code

      result = DescriptionAPI.undo()
      assert 200 == result.code

      result = DescriptionAPI.show(descriptionToDelete.domainIdent,descriptionToDelete.domainClassName, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = DescriptionAPI.redo()
      assert 200 == result.code

      result = DescriptionAPI.show(descriptionToDelete.domainIdent,descriptionToDelete.domainClassName, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

  void testDeleteDescriptionNotExist() {
      def result = DescriptionAPI.delete(-99, 'bad.class.name', Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }
}
