package be.cytomine.security

import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.DomainAPI
import be.cytomine.test.http.OntologyAPI
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.PropertyAPI
import be.cytomine.test.http.TermAPI
import be.cytomine.test.http.UserRoleAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class LoginWithTokenSecurityTests extends SecurityTestsAbstract {

    void testBuildTokenForAdmin() {
        User user2 = BasicInstanceBuilder.getUser2()
        def result = UserRoleAPI.buildToken(user2.username,60,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testBuildTokenForSimpleUser() {
        User user2 = BasicInstanceBuilder.getUser2()
        def result = UserRoleAPI.buildToken(user2.username,60,"user2", "password")
        assert 403 == result.code
    }

    void testLogWithGoodToken() {
        User user1 = BasicInstanceBuilder.getUser1()
        User user2 = BasicInstanceBuilder.getUser2()
        def result = UserRoleAPI.buildToken(user2.username,60.5,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        String tokenKey = JSON.parse(result.data).token.tokenKey
        Thread.sleep(1000)
        def result2 = UserRoleAPI.showCurrentUserWithToken(user2.username,tokenKey,"user1","bad")
        assert user2.id==JSON.parse(result2.data).id
    }

    void testLogWithBadToken() {
        String tokenKey = "blablabla"
        User user2 = BasicInstanceBuilder.getUser2()
        def result2 = UserRoleAPI.showCurrentUserWithToken(user2.username,tokenKey,"user1","bad")
        assert 403 == result2.code  || 401 == result2.code
    }

    void testLogWithBadUser() {
        User user2 = BasicInstanceBuilder.getUser2()
        def result = UserRoleAPI.buildToken(user2.username,60,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        String tokenKey = JSON.parse(result.data).token.tokenKey
        //put the user 1 username
        def result2 = UserRoleAPI.showCurrentUserWithToken(user1.username,tokenKey,"user1","bad")
        assert 403 == result2.code || 401 == result2.code

        //try with an invalid user
        result2 = UserRoleAPI.showCurrentUserWithToken("fgerfrefreaag",tokenKey,"user1","bad")
        assert 403 == result2.code || 401 == result2.code
    }

    void testLogWithExpireToken() {
        User user2 = BasicInstanceBuilder.getUser2()
        def result = UserRoleAPI.buildToken(user2.username,0,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        String tokenKey = JSON.parse(result.data).token.tokenKey
        result = UserRoleAPI.showCurrentUserWithToken(user2.username,tokenKey,"user1","bad")
        assert 403 == result.code || 401 == result.code

        result = UserRoleAPI.buildToken(user2.username,0.01,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        tokenKey = JSON.parse(result.data).token.tokenKey
        Thread.sleep(1000) // 0.01 min = 0.6 sec
        result = UserRoleAPI.showCurrentUserWithToken(user2.username,tokenKey,"user1","bad")
        assert 403 == result.code || 401 == result.code
    }
}
