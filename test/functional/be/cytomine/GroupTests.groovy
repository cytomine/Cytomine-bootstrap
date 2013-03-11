package be.cytomine

import be.cytomine.test.BasicInstanceBuilder
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
class GroupTests  {

  void testListGroup() {
      def result = GroupAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testShowGroup() {
      def result = GroupAPI.show(BasicInstanceBuilder.getGroup().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject

      result = GroupAPI.show(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }

  void testAddGroupCorrect() {
      def groupToAdd = BasicInstanceBuilder.getGroupNotExist()
      def result = GroupAPI.create(groupToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      int idGroup = result.data.id

      result = GroupAPI.show(idGroup, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
  }

  void testAddGroupAlreadyExist() {
      def groupToAdd = BasicInstanceBuilder.getGroup()
      def result = GroupAPI.create(groupToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 409 == result.code
  }

  void testUpdateGroupCorrect() {
      Group group = BasicInstanceBuilder.getGroup()
      def data = UpdateData.createUpdateSet(group,[name: ["OLDNAME","NEWNAME"]])
      def result = GroupAPI.update(group.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testUpdateGroupNotExist() {
      Group groupWithOldName = BasicInstanceBuilder.getGroup()
      Group groupWithNewName = BasicInstanceBuilder.getGroupNotExist()
      groupWithNewName.save(flush: true)
      Group groupToEdit = Group.get(groupWithNewName.id)
      def jsonGroup = groupToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonGroup)
      jsonUpdate.name = groupWithOldName.name
      jsonUpdate.id = -99
      jsonGroup = jsonUpdate.encodeAsJSON()
      def result = GroupAPI.update(-99, jsonGroup, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }

  void testUpdateGroupWithNameAlreadyExist() {
      Group groupWithOldName = BasicInstanceBuilder.getGroup()
      Group groupWithNewName = BasicInstanceBuilder.getGroupNotExist()
      groupWithNewName.save(flush: true)
      Group groupToEdit = Group.get(groupWithNewName.id)
      def jsonGroup = groupToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonGroup)
      jsonUpdate.name = groupWithOldName.name
      jsonGroup = jsonUpdate.encodeAsJSON()
      def result = GroupAPI.update(groupToEdit.id, jsonGroup, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 409 == result.code
  }

  void testDeleteGroup() {
      def groupToDelete = BasicInstanceBuilder.getGroupNotExist()
      assert groupToDelete.save(flush: true)!= null
      def id = groupToDelete.id
      def result = GroupAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code

      def showResult = GroupAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == showResult.code
  }

  void testDeleteGroupNotExist() {
      def result = GroupAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code
  }
}
