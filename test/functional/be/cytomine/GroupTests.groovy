package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.GroupAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.security.Group
import be.cytomine.utils.UpdateData

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class GroupTests extends functionaltestplugin.FunctionalTestCase {

  void testListGroup() {
      def result = GroupAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONArray
  }

  void testShowGroup() {
      def result = GroupAPI.show(BasicInstance.createOrGetBasicGroup().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject

      result = GroupAPI.show(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testAddGroupCorrect() {
      def groupToAdd = BasicInstance.getBasicGroupNotExist()
      def result = GroupAPI.create(groupToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      int idGroup = result.data.id

      result = GroupAPI.show(idGroup, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
  }

  void testAddGroupAlreadyExist() {
      def groupToAdd = BasicInstance.createOrGetBasicGroup()
      def result = GroupAPI.create(groupToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(409, result.code)
  }

  void testUpdateGroupCorrect() {
      Group groupToAdd = BasicInstance.createOrGetBasicGroup()
      def data = UpdateData.createUpdateSet(groupToAdd)
      def result = GroupAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testUpdateGroupNotExist() {
      Group groupWithOldName = BasicInstance.createOrGetBasicGroup()
      Group groupWithNewName = BasicInstance.getBasicGroupNotExist()
      groupWithNewName.save(flush: true)
      Group groupToEdit = Group.get(groupWithNewName.id)
      def jsonGroup = groupToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonGroup)
      jsonUpdate.name = groupWithOldName.name
      jsonUpdate.id = -99
      jsonGroup = jsonUpdate.encodeAsJSON()
      def result = GroupAPI.update(-99, jsonGroup, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }

  void testUpdateGroupWithNameAlreadyExist() {
      Group groupWithOldName = BasicInstance.createOrGetBasicGroup()
      Group groupWithNewName = BasicInstance.getBasicGroupNotExist()
      groupWithNewName.save(flush: true)
      Group groupToEdit = Group.get(groupWithNewName.id)
      def jsonGroup = groupToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonGroup)
      jsonUpdate.name = groupWithOldName.name
      jsonGroup = jsonUpdate.encodeAsJSON()
      def result = GroupAPI.update(groupToEdit.id, jsonGroup, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(409, result.code)
  }

  void testDeleteGroup() {
      def groupToDelete = BasicInstance.getBasicGroupNotExist()
      assert groupToDelete.save(flush: true)!= null
      def id = groupToDelete.id
      def result = GroupAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(200, result.code)

      def showResult = GroupAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, showResult.code)
  }

  void testDeleteGroupNotExist() {
      def result = GroupAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assertEquals(404, result.code)
  }
}
