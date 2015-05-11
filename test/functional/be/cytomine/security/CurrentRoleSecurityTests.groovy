package be.cytomine.security

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.UserRoleAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class CurrentRoleSecurityTests extends SecurityTestsAbstract {


    void testOpenAdminSessionForAdmin() {

        //HttpClient client = HttpClient.getClientWithCookie(Infos.CYTOMINEURL,Infos.ADMINLOGIN,Infos.ADMINPASSWORD)
        //teh session scope is lost (not the case in the web client)

        //admin should be false
        def result = UserRoleAPI.openAdminSession(Infos.ADMINLOGIN,Infos.ADMINPASSWORD) //client)
        assert 200 == result.code
        assert JSON.parse(result.data).admin
        assert JSON.parse(result.data).adminByNow

        //teh session scope is lost (not the case in the web client)
        result = UserRoleAPI.infoAdminSession(Infos.ADMINLOGIN,Infos.ADMINPASSWORD) //client)
        assert 200 == result.code
        assert JSON.parse(result.data).admin
        assert !JSON.parse(result.data).adminByNow
    }

    void testOpenAdminSessionForUserAndGuest() {
        User user = BasicInstanceBuilder.getUser()
        def result = UserRoleAPI.openAdminSession(user.username,"password")
        assert 403 == result.code

        user = BasicInstanceBuilder.getGhest("testOpenAdminSessionForUserAndGuest","password")
        result = UserRoleAPI.openAdminSession(user.username,"password")
        assert 403 == result.code
    }

    void testCloseAdminSessionForAdmin() {
        //admin should be false
        def result = UserRoleAPI.closeAdminSession(Infos.ADMINLOGIN,Infos.ADMINPASSWORD)
        assert 200 == result.code
        assert JSON.parse(result.data).admin
        assert !JSON.parse(result.data).adminByNow
    }

    void testCloseAdminSessionForUserAndGuest() {
        User user = BasicInstanceBuilder.getUser()
        def result = UserRoleAPI.closeAdminSession(user.username,"password")
        assert 403 == result.code

        user = BasicInstanceBuilder.getGhest("testOpenAdminSessionForUserAndGuest","password")
        result = UserRoleAPI.closeAdminSession(user.username,"password")
        assert 403 == result.code
    }

    void testGetSessionInfo() {
        //admin should be false
        def result = UserRoleAPI.infoAdminSession(Infos.ADMINLOGIN,Infos.ADMINPASSWORD)
        assert 200 == result.code
        assert JSON.parse(result.data).admin
        assert !JSON.parse(result.data).adminByNow
    }

    void testGetSessionInfoForSuperAdmin() {
        //admin should be true

    }

    void testAdminHasNotAdminRight() {
        //an admin has no admin right by default

    }

    void testUserHasNotAdminRight() {

    }

    void testGuestHasNotAdminRight() {

    }


}
