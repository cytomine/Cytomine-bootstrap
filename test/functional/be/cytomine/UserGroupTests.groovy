package be.cytomine

import be.cytomine.security.UserGroup
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.UserGroupAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class UserGroupTests  {

    void testShowUserGroup() {
        def user = BasicInstanceBuilder.user1
        def group =  BasicInstanceBuilder.getGroupNotExist()
        BasicInstanceBuilder.saveDomain(group)
        UserGroup userGroup =  new UserGroup(user: user,group : group)
        BasicInstanceBuilder.saveDomain(userGroup)

        def result = UserGroupAPI.showUserGroupCurrent(user.id,group.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = UserGroupAPI.showUserGroupCurrent(-99,-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListUserGroup() {
        def user = BasicInstanceBuilder.user1

        def result = UserGroupAPI.list(user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testCreateUserGroup() {
        def user = BasicInstanceBuilder.user1
        def group =  BasicInstanceBuilder.getGroupNotExist()
        BasicInstanceBuilder.saveDomain(group)
        UserGroup userGroup =  new UserGroup(user: user,group : group)

        def result = UserGroupAPI.create(user.id,userGroup.encodeAsJSON(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testDeleteUserGroup() {
        def user = BasicInstanceBuilder.user1
        def group =  BasicInstanceBuilder.getGroupNotExist()
        BasicInstanceBuilder.saveDomain(group)
        UserGroup userGroup =  new UserGroup(user: user,group : group)
        BasicInstanceBuilder.saveDomain(userGroup)

        def result = UserGroupAPI.delete(user.id,group.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

}
