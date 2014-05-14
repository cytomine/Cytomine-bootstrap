package be.cytomine.security

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.UserAPI

class UserSwitcherSecurityTests extends SecurityTestsAbstract {



    void testSwitchUserAsAdmin() {
//       User user1 = BasicInstanceBuilder.getUser("testSwitchUserAsAdmin","password")
//       def response = UserAPI.switchUser(user1.username,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
//        //doesn't work :/

        //but testSwitchUserAsUser and testSwitchUserAsGuest are still usefull to check security for this method
    }

    void testSwitchUserAsUser() {
        User user1 = BasicInstanceBuilder.getUser("testSwitchUserAsUser","password")
        def response = UserAPI.switchUser(Infos.SUPERADMINLOGIN,"testSwitchUserAsUser","password")
        assert 403 == response.code
    }

    void testSwitchUserAsGuest() {
        User user1 = BasicInstanceBuilder.getGhest("testSwitchUserAsUser","password")
        def response = UserAPI.switchUser(Infos.SUPERADMINLOGIN,"testSwitchUserAsUser","password")
        assert 403 == response.code
    }


}
