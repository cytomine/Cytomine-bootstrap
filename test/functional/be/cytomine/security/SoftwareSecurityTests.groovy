package be.cytomine.security

import be.cytomine.test.Infos

import be.cytomine.test.BasicInstance
import grails.converters.JSON
import be.cytomine.test.http.SoftwareAPI
import be.cytomine.processing.Software

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class SoftwareSecurityTests extends SecurityTestsAbstract {
    
  void testSoftwareSecurityForCytomineAdmin() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Get admin user
      User admin = BasicInstance.createOrGetBasicAdmin(USERNAMEADMIN,PASSWORDADMIN)

      //Create new software (user1)
      def result = SoftwareAPI.create(BasicInstance.getBasicSoftwareNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Software software = result.data

      //check if admin user can access/update/delete
      assert (200 == SoftwareAPI.show(software.id,USERNAMEADMIN,PASSWORDADMIN).code)
      assert (true ==SoftwareAPI.containsInJSONList(software.id,JSON.parse(SoftwareAPI.list(USERNAMEADMIN,PASSWORDADMIN).data)))
      assert (200 == SoftwareAPI.update(software.id,software.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
      assert (200 == SoftwareAPI.delete(software.id,USERNAMEADMIN,PASSWORDADMIN).code)
  }

  void testSoftwareSecurityForSoftwareCreator() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Create new Software (user1)
      def result = SoftwareAPI.create(BasicInstance.getBasicSoftwareNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Software software = result.data

      //check if user 1 can access/update/delete
      assert (200 == SoftwareAPI.show(software.id,USERNAME1,PASSWORD1).code)
      assert (true ==SoftwareAPI.containsInJSONList(software.id,JSON.parse(SoftwareAPI.list(USERNAME1,PASSWORD1).data)))
      assert (200 == SoftwareAPI.update(software.id,software.encodeAsJSON(),USERNAME1,PASSWORD1).code)
      assert (200 == SoftwareAPI.delete(software.id,USERNAME1,PASSWORD1).code)
  }

  void testSoftwareSecurityForSimpleUser() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstance.createOrGetBasicUser(USERNAME2,PASSWORD2)

      //Create new Software (user1)
      def result = SoftwareAPI.create(BasicInstance.getBasicSoftwareNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Software software = result.data
      Infos.printRight(software)
      //check if user 2 cannot access/update/delete
      assert (200 == SoftwareAPI.show(software.id,USERNAME2,PASSWORD2).code)
      assert (403 == SoftwareAPI.update(software.id,software.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (403 == SoftwareAPI.delete(software.id,USERNAME2,PASSWORD2).code)

  }

  void testSoftwareSecurityForAnonymous() {

      //Get user1
      User user1 = BasicInstance.createOrGetBasicUser(USERNAME1,PASSWORD1)

      //Create new Software (user1)
      def result = SoftwareAPI.create(BasicInstance.getBasicSoftwareNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Software software = result.data
      Infos.printRight(software)
      //check if user 2 cannot access/update/delete
      assert (401 == SoftwareAPI.show(software.id,USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == SoftwareAPI.list(USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == SoftwareAPI.update(software.id,software.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == SoftwareAPI.delete(software.id,USERNAMEBAD,PASSWORDBAD).code)
  }
}
