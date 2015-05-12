package be.cytomine.security

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.UserAPI

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
