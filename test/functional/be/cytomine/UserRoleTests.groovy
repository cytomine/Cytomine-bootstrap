package be.cytomine

import be.cytomine.security.SecRole
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.UserRoleAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class UserRoleTests  {

  void testListSecRole() {
      def result = UserRoleAPI.listRole(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

    void testListRoleUser() {
        def result = UserRoleAPI.listByUser(BasicInstanceBuilder.user1.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testShowRoleUser() {
        def result = UserRoleAPI.show(BasicInstanceBuilder.user1.id,SecRole.findByAuthority("ROLE_USER").id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = UserRoleAPI.show(BasicInstanceBuilder.user1.id,-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testShowRole() {
        def result = UserRoleAPI.show(BasicInstanceBuilder.user1.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }


  void testAddUserRoleCorrect() {
      def idUser = BasicInstanceBuilder.saveDomain(BasicInstanceBuilder.getUserNotExist(false)).id
      def idRole = SecRole.findByAuthority("ROLE_USER").id
      def json = "{user : $idUser, role: $idRole}"

      def result = UserRoleAPI.create(idUser,idRole,json, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
  }

    void testAddAdminRoleCorrect() {
        def idUser = BasicInstanceBuilder.saveDomain(BasicInstanceBuilder.getUserNotExist(false)).id
        def idRole = SecRole.findByAuthority("ROLE_ADMIN").id

        assert !hasRole(idUser,idRole)

        def json = "{user : $idUser, role: $idRole}"

        def result = UserRoleAPI.create(idUser,idRole,json, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        assert hasRole(idUser,idRole)
    }

    void testAddSuperAdminRoleCorrect() {
        def idUser = BasicInstanceBuilder.saveDomain(BasicInstanceBuilder.getUserNotExist(false)).id
        def idRole = SecRole.findByAuthority("ROLE_SUPER_ADMIN").id

        assert !hasRole(idUser,idRole)

        def json = "{user : $idUser, role: $idRole}"

        def result = UserRoleAPI.create(idUser,idRole,json, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        assert hasRole(idUser,idRole)
    }

    void testAddUseRoleAlreadyExist() {
        def idUser = BasicInstanceBuilder.saveDomain(BasicInstanceBuilder.getUserNotExist(false)).id
        def idRole = SecRole.findByAuthority("ROLE_USER").id
        def json = "{user : $idUser, role: $idRole}"

        def result = UserRoleAPI.create(idUser,idRole,json, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        result = UserRoleAPI.create(idUser,idRole,json, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }

  void testDeleteUserRole() {
      def user = BasicInstanceBuilder.getUserNotExist(true)
      def role = SecRole.findByAuthority("ROLE_USER")

      def idUser = user.id
      def idRole = role.id

      def result = UserRoleAPI.delete(idUser,idRole,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = UserRoleAPI.show(idUser,idRole,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code


  }

    void testDeleteAdminRole() {
        def user = BasicInstanceBuilder.getSuperAdmin("testDeleteAdminRole","password")
        def role = SecRole.findByAuthority("ROLE_ADMIN")

        def idUser = user.id
        def idRole = role.id

        def result = UserRoleAPI.show(idUser,idRole,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code


        result = UserRoleAPI.delete(idUser,idRole,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = UserRoleAPI.show(idUser,idRole,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }



    void testDefineUserRole() {

        def roleGuest = SecRole.findByAuthority("ROLE_GUEST")
        def roleUser = SecRole.findByAuthority("ROLE_USER")
        def roleAdmin = SecRole.findByAuthority("ROLE_ADMIN")
        def roleSuperAdmin = SecRole.findByAuthority("ROLE_SUPER_ADMIN")

        def user = BasicInstanceBuilder.getGhestNotExist(true)

        assert hasRole(user.id,roleGuest.id)
        assert !hasRole(user.id,roleUser.id)
        assert !hasRole(user.id,roleAdmin.id)
        assert !hasRole(user.id,roleSuperAdmin.id)

        def result = UserRoleAPI.define(user.id,roleAdmin.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 200

        assert hasRole(user.id,roleGuest.id)
        assert hasRole(user.id,roleUser.id)
        assert hasRole(user.id,roleAdmin.id)
        assert !hasRole(user.id,roleSuperAdmin.id)

        result = UserRoleAPI.define(user.id,roleGuest.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 200

        assert hasRole(user.id,roleGuest.id)
        assert !hasRole(user.id,roleUser.id)
        assert !hasRole(user.id,roleAdmin.id)
        assert !hasRole(user.id,roleSuperAdmin.id)

        result = UserRoleAPI.define(user.id,roleUser.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 200

        assert hasRole(user.id,roleGuest.id)
        assert hasRole(user.id,roleUser.id)
        assert !hasRole(user.id,roleAdmin.id)
        assert !hasRole(user.id,roleSuperAdmin.id)

        result = UserRoleAPI.define(user.id,roleSuperAdmin.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 200

        assert hasRole(user.id,roleGuest.id)
        assert hasRole(user.id,roleUser.id)
        assert hasRole(user.id,roleAdmin.id)
        assert hasRole(user.id,roleSuperAdmin.id)

        result = UserRoleAPI.define(-99,roleUser.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 404
        result = UserRoleAPI.define(user.id,-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 404
    }

    private boolean hasRole(def idUser,idRole) {
        def result = UserRoleAPI.show(idUser,idRole,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert result.code == 200 || result.code == 404
        return (200 == result.code)
    }


}
