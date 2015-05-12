package be.cytomine

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

        def result = UserGroupAPI.showUserGroupCurrent(user.id,group.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = UserGroupAPI.showUserGroupCurrent(-99,-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testListUserGroup() {
        def user = BasicInstanceBuilder.user1

        def result = UserGroupAPI.list(user.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testCreateUserGroup() {
        def user = BasicInstanceBuilder.user1
        def group =  BasicInstanceBuilder.getGroupNotExist()
        BasicInstanceBuilder.saveDomain(group)
        UserGroup userGroup =  new UserGroup(user: user,group : group)

        def result = UserGroupAPI.create(user.id,userGroup.encodeAsJSON(),Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testDeleteUserGroup() {
        def user = BasicInstanceBuilder.user1
        def group =  BasicInstanceBuilder.getGroupNotExist()
        BasicInstanceBuilder.saveDomain(group)
        UserGroup userGroup =  new UserGroup(user: user,group : group)
        BasicInstanceBuilder.saveDomain(userGroup)

        def result = UserGroupAPI.delete(user.id,group.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

}
