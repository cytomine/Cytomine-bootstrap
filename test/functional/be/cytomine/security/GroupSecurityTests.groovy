package be.cytomine.security

import be.cytomine.test.BasicInstance
import grails.converters.JSON
import be.cytomine.test.http.GroupAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class GroupSecurityTests extends SecurityTestsAbstract {


    void testGroupSecurityForCytomineAdmin() {
        //Get user 1
        User user1 = BasicInstance.createOrGetBasicUser(USERNAMEWITHOUTDATA,PASSWORDWITHOUTDATA)

        //Get user admin
        User admin = BasicInstance.createOrGetBasicAdmin(USERNAMEADMIN,PASSWORDADMIN)

        def group = BasicInstance.getBasicGroupNotExist()
        BasicInstance.saveDomain(group)

        //Check if admin can read/add/update/del
        assert (200 == GroupAPI.create(BasicInstance.getBasicGroupNotExist().encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
        assert (200 == GroupAPI.show(group.id,USERNAMEADMIN,PASSWORDADMIN).code)
        assert (true ==GroupAPI.containsInJSONList(group.id,JSON.parse(GroupAPI.list(USERNAMEADMIN,PASSWORDADMIN).data)))
        assert (200 == GroupAPI.update(group.id,group.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
        assert (200 == GroupAPI.delete(group.id,USERNAMEADMIN,PASSWORDADMIN).code)
    }

    void testGroupSecurityForUserFromGroup() {
        //Get user 1
        User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

        //Get group
        def group = BasicInstance.getBasicGroupNotExist()
        BasicInstance.saveDomain(group)
        def userGroup = new UserGroup(user:user1,group:group)
        BasicInstance.saveDomain(userGroup)

        //Check if a user from group can read/add/update/del
        assert (200 == GroupAPI.create(BasicInstance.getBasicGroupNotExist().encodeAsJSON(),USERNAME1,PASSWORD1).code)
        assert (200 == GroupAPI.show(group.id,USERNAME1,PASSWORD1).code)
        assert (true ==GroupAPI.containsInJSONList(group.id,JSON.parse(GroupAPI.list(USERNAME1,PASSWORD1).data)))
        assert (200 == GroupAPI.update(group.id,group.encodeAsJSON(),USERNAME1,PASSWORD1).code)
        assert (403 == GroupAPI.delete(group.id,USERNAME1,PASSWORD1).code)
    }


    void testGroupSecurityForSimpleUser() {
        //Get user 1
        User user1 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)

        //Get group
        def group = BasicInstance.getBasicGroupNotExist()
        BasicInstance.saveDomain(group)

        //Check if a user from group can read/add/update/del
        assert (200 == GroupAPI.create(BasicInstance.getBasicGroupNotExist().encodeAsJSON(),USERNAME2,PASSWORD2).code)
        assert (200 == GroupAPI.show(group.id,USERNAME2,PASSWORD2).code)
        assert (true ==GroupAPI.containsInJSONList(group.id,JSON.parse(GroupAPI.list(USERNAME2,PASSWORD2).data)))
        assert (403 == GroupAPI.update(group.id,group.encodeAsJSON(),USERNAME2,PASSWORD2).code)
        assert (403 == GroupAPI.delete(group.id,USERNAME2,PASSWORD2).code)
    }

    void testGroupSecurityForNotConnectedUser() {
        //Get user 1
        User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

        //Get group
        def group = BasicInstance.getBasicGroupNotExist()
        BasicInstance.saveDomain(group)

        //Check if a user from group can read/add/update/del
        assert (401 == GroupAPI.create(BasicInstance.getBasicGroupNotExist().encodeAsJSON(),USERNAMEBAD,PASSWORDWITHOUTDATA).code)
        assert (401 == GroupAPI.show(group.id,USERNAMEBAD,PASSWORDWITHOUTDATA).code)
        assert (401 == GroupAPI.update(group.id,group.encodeAsJSON(),USERNAMEBAD,PASSWORDWITHOUTDATA).code)
        assert (401 == GroupAPI.delete(group.id,USERNAMEBAD,PASSWORDWITHOUTDATA).code)
    }

}
