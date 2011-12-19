package be.cytomine

import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class SecurityTestsAbstract extends functionaltestplugin.FunctionalTestCase {

    /**
     * Security test
     */
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


    User getUser1() {
         BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
    }

    User getUser2() {
         BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)
    }

    User getUser3() {
         BasicInstance.createOrGetBasicUser(USERNAME3,PASSWORD3)
    }

    User getUserAdmin() {
         BasicInstance.createOrGetBasicAdmin(USERNAMEADMIN,PASSWORDADMIN)
    }

    User getUserBad() {
         BasicInstance.createOrGetBasicUser(USERNAMEBAD,PASSWORDBAD)
    }

}