package be.cytomine.security

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

import be.cytomine.test.BasicInstanceBuilder

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class SecurityTestsAbstract  {

    /**
     * Security test
     */
    static String USERNAMEWITHOUTDATA = "USERNAMEWITHOUTDATA"
    static String PASSWORDWITHOUTDATA = "PASSWORDWITHOUTDATA"
    static String USERNAME1 = "USERNAME1"
    static String PASSWORD1 = "PASSWORD1"
    static String USERNAME2 = "USERNAME2"
    static String PASSWORD2 = "PASSWORD2"
    static String USERNAME3 = "USERNAME3"
    static String PASSWORD3 = "PASSWORD3"
    static String USERNAMEADMIN = "USERNAMEADMIN"
    static String PASSWORDADMIN = "PASSWORDADMIN"
    static String USERNAMEBAD = "BADUSER"
    static String PASSWORDBAD = "BADPASSWORD"

    static String GHEST1 = "USERNAME1"


    User getUser1() {
         BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
    }

    User getUser2() {
         BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)
    }

    User getUser3() {
         BasicInstanceBuilder.getUser(USERNAME3,PASSWORD3)
    }

    User getUserAdmin() {
         BasicInstanceBuilder.getSuperAdmin(USERNAMEADMIN,PASSWORDADMIN)
    }

    User getUserBad() {
         BasicInstanceBuilder.getUser(USERNAMEBAD,PASSWORDBAD)
    }

}
