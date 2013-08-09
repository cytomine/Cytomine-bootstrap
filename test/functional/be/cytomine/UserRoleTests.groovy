package be.cytomine

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

import be.cytomine.test.http.UserRoleAPI
import be.cytomine.security.SecRole
import be.cytomine.security.SecUserSecRole

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class UserRoleTests  {

  void testListSecRole() {
      def result = UserRoleAPI.listRole(Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

    void testListRoleUser() {
        def result = UserRoleAPI.listByUser(BasicInstanceBuilder.user1.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testShowRoleUser() {
        def result = UserRoleAPI.show(BasicInstanceBuilder.user1.id,SecRole.findByAuthority("ROLE_USER").id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = UserRoleAPI.show(BasicInstanceBuilder.user1.id,-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


  void testAddUseRoleCorrect() {
      def idUser = BasicInstanceBuilder.user1.id
      def idRole = SecRole.findByAuthority("ROLE_USER").id
      def json = "{user : $idUser, role: $idRole}"

      def result = UserRoleAPI.create(idUser,idRole,json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
  }

  void testDeleteUserRole() {
      def user = BasicInstanceBuilder.getUserNotExist(true)
      def role = SecRole.findByAuthority("ROLE_USER")
      def userole = SecUserSecRole.create(user,role,true)

      def idUser = user.id
      def idRole = role.id

      def result = UserRoleAPI.delete(idUser,idRole,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code

      result = UserRoleAPI.show(idUser,idRole,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 404 == result.code


  }


}
