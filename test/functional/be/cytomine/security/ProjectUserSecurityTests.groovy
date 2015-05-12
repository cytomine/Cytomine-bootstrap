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

import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.http.ProjectAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class ProjectUserSecurityTests extends SecurityTestsAbstract {


  void testUserProjectSecurityForCytomineAdmin() {

      //Get user1
      User user1 = getUser1()

      //Get user2
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //check if admin can add user 2 in project
      assert (200 ==ProjectAPI.addUserProject(project.id, user2.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)

      //check if admin can delete user 2 in project
      assert (200 ==ProjectAPI.deleteUserProject(project.id, user2.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)

  }

  void testUserProjectSecurityForProjectCreator() {

      //Get user1
      User user1 = getUser1()

      //Get user2
      User user2 = getUser2()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //check if user1 can add user 2 in project
      assert (200 ==ProjectAPI.addUserProject(project.id, user2.id,SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1).code)

      //check if user1 can delete user 2 in project
      assert (200 ==ProjectAPI.deleteUserProject(project.id, user2.id,SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1).code)

  }

  void testUserProjectSecurityForProjectUser() {

      //Get user1
      User user1 = getUser1()

      //Get user2
      User user2 = getUser2()

      //Get user3
      User user3 = getUser3()

      //Get admin user
      User admin = getUserAdmin()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //check if user1 can add user 2 in project
      assert (200 ==ProjectAPI.addUserProject(project.id, user2.id,SecurityTestsAbstract.USERNAMEADMIN,SecurityTestsAbstract.PASSWORDADMIN).code)

      //check if user2 cannot add user 3 in project
      assert (403 ==ProjectAPI.addUserProject(project.id, user3.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)

      //check if user2 cannot delete user 3 in project
      assert (403 ==ProjectAPI.deleteUserProject(project.id, user3.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)

      //check if user2 can delete himself project
      assert (200 ==ProjectAPI.deleteUserProject(project.id, user2.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)

  }

  void testUserProjectSecurityForSimpleUser() {

      //Get user1
      User user1 = getUser1()

      //Get user2
      User user2 = getUser2()

      //Get user3
      User user3 = getUser3()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //check if user2 cannot add user 3 in project
      assert (403 ==ProjectAPI.addUserProject(project.id, user2.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)

      //check if user2 cannot delete user 3 in project
      assert (403 ==ProjectAPI.deleteUserProject(project.id, user3.id,SecurityTestsAbstract.USERNAME2,SecurityTestsAbstract.PASSWORD2).code)

  }


  void testUserProjectSecurityForAnonymous() {

      //Get user1
      User user1 = getUser1()

      //Get user2
      User user2 = getUser2()

      //Create new project (user1)
      def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),SecurityTestsAbstract.USERNAME1,SecurityTestsAbstract.PASSWORD1)
      assert 200 == result.code
      Project project = result.data

      //check if user2 cannot add user 3 in project
      assert (401 ==ProjectAPI.addUserProject(project.id, user2.id,SecurityTestsAbstract.USERNAMEBAD,SecurityTestsAbstract.PASSWORDBAD).code)

      //check if user2 cannot delete user 3 in project
      assert (401 ==ProjectAPI.deleteUserProject(project.id, user2.id,SecurityTestsAbstract.USERNAMEBAD,SecurityTestsAbstract.PASSWORDBAD).code)

  }
}
