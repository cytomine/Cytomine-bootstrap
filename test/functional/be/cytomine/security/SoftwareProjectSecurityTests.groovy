package be.cytomine.security

import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI

import be.cytomine.test.BasicInstance
import grails.converters.JSON
import be.cytomine.processing.SoftwareProject
import be.cytomine.test.http.SoftwareProjectAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class SoftwareProjectSecurityTests extends SecurityTestsAbstract {

    void testSoftwareProjectSecurityForCytomineAdmin() {
        //Get User 1
        User user = getUser1()

        //Get cytomine admin
        User admin = getUserAdmin()

        //Add softwareProject 2 with user 1
        SoftwareProject softwareProject2 = BasicInstance.getBasicSoftwareProjectNotExist()
        Infos.addUserRight(user,softwareProject2.project)
        def result = SoftwareProjectAPI.create(softwareProject2.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        softwareProject2 = result.data

        //Get/List softwareProject with cytomine admin
        assert (200 == SoftwareProjectAPI.show(softwareProject2.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)
        result = SoftwareProjectAPI.listByProject(softwareProject2.project.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assert 200 == result.code
        log.info "JSON.parse(result.data)="+JSON.parse(result.data)
        assert (true ==SoftwareProjectAPI.containsInJSONList(softwareProject2.software.id, JSON.parse(result.data)))

        //Delete softwareProject 2 with cytomine admin
        assert (200 == SoftwareProjectAPI.delete(softwareProject2.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)


    }

    void testsoftwareProjectSecurityForProjectCreator() {
        //Get User 1
        User user = getUser1()

        //Create project with user 1
        SoftwareProject softwareProject2 = BasicInstance.getBasicSoftwareProjectNotExist()
        Infos.addUserRight(user,softwareProject2.project)

        //Add softwareProject 1 with user1
        def result = SoftwareProjectAPI.create(softwareProject2.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        softwareProject2 = result.data

        //Get/List softwareProject 1 with user 1
        assert (200 == SoftwareProjectAPI.show(softwareProject2.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
        result = SoftwareProjectAPI.listByProject(softwareProject2.project.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        assert (true ==SoftwareProjectAPI.containsInJSONList(softwareProject2.software.id, JSON.parse(result.data)))

        //Delete softwareProject 1 with user 1
        assert (200 == SoftwareProjectAPI.delete(softwareProject2.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
    }

    void testsoftwareProjectSecurityForProjectUser() {

        //Get User 1
        User user = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        SoftwareProject softwareProject2 = BasicInstance.getBasicSoftwareProjectNotExist()
        Infos.addUserRight(user,softwareProject2.project)

        //Add project right for user 2
        println "--- " + softwareProject2.project.id
        println "--- " + user2.id
        def resAddUser = ProjectAPI.addUserProject(softwareProject2.project.id, user2.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assert 200 == resAddUser.code


        //Add softwareProject 1 with user1
        def result = SoftwareProjectAPI.create(softwareProject2.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        softwareProject2 = result.data

        //Get/List softwareProject 1 with user 1
        assert (200 == SoftwareProjectAPI.show(softwareProject2.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
        result = SoftwareProjectAPI.listByProject(softwareProject2.project.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        assert (false==SoftwareProjectAPI.containsInJSONList(softwareProject2.id, JSON.parse(result.data)))

        //Delete softwareProject 1 with user 1
        assert (200 == SoftwareProjectAPI.delete(softwareProject2.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
    }


    void testsoftwareProjectSecurityForUser() {
        //Get User 1
        User user1 = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        SoftwareProject softwareProject = BasicInstance.getBasicSoftwareProjectNotExist()
        Infos.addUserRight(user1,softwareProject.project)

        //Add softwareProject 1 with user 1
        def result = SoftwareProjectAPI.create(softwareProject.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        softwareProject = result.data

        //Get/List softwareProject 1 with user 2
        assert (403 == SoftwareProjectAPI.show(softwareProject.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
        result = SoftwareProjectAPI.listByProject(softwareProject.project.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assert (403 == result.code)

        //Delete softwareProject 1 with user 2
        assert (403 == SoftwareProjectAPI.delete(softwareProject.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }



    void testsoftwareProjectSecurityForAnonymous() {
        //Get User 1
        User user1 = getUser1()

        //Create project with user 1
        SoftwareProject softwareProject = BasicInstance.getBasicSoftwareProjectNotExist()
        Infos.addUserRight(user1,softwareProject.project)

        //Add softwareProject 1 with user 1
        def result = SoftwareProjectAPI.create(softwareProject.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        softwareProject = result.data

        //Get/List softwareProject 1 with user 2
        assert (401 == SoftwareProjectAPI.show(softwareProject.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assert (401 == SoftwareProjectAPI.listByProject(softwareProject.project.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assert (401 == SoftwareProjectAPI.delete(softwareProject.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
    }

}
