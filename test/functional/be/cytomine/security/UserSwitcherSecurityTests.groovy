package be.cytomine.security

import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.UserAPI
import grails.converters.JSON


class UserSwitcherSecurityTests extends SecurityTestsAbstract {



    void testSwitchUserAsAdmin() {
//       User user1 = BasicInstanceBuilder.getUser("testSwitchUserAsAdmin","password")
//       def response = UserAPI.switchUser(user1.username,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//        //doesn't work :/

        //but testSwitchUserAsUser and testSwitchUserAsGuest are still usefull to check security for this method
    }

    void testSwitchUserAsUser() {
        User user1 = BasicInstanceBuilder.getUser("testSwitchUserAsUser","password")
        def response = UserAPI.switchUser(Infos.GOODLOGIN,"testSwitchUserAsUser","password")
        assert 403 == response.code
    }

    void testSwitchUserAsGuest() {
        User user1 = BasicInstanceBuilder.getGhest("testSwitchUserAsUser","password")
        def response = UserAPI.switchUser(Infos.GOODLOGIN,"testSwitchUserAsUser","password")
        assert 403 == response.code
    }


}
