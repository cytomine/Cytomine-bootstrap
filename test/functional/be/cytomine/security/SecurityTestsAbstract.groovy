package be.cytomine.security

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
         BasicInstanceBuilder.getAdmin(USERNAMEADMIN,PASSWORDADMIN)
    }

    User getUserBad() {
         BasicInstanceBuilder.getUser(USERNAMEBAD,PASSWORDBAD)
    }

}
