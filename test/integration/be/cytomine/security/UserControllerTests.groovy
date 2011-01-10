package be.cytomine.security;

import grails.test.ControllerUnitTestCase;


/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 8/01/11
 * Time: 01:10
 */
 class UserControllerTests extends ControllerUnitTestCase {


    void testIndex() {
      def uc = new UserController()
      uc.index()
      assertEquals "/user/list", uc.response.redirectedUrl
	}

	void testList() {
        assertTrue true
	}

   void testCreate() {
        assertTrue true
	}

   void testSave() {
        assertTrue true
	}

   void testShow() {
        assertTrue true
	}

   void testEdit() {
        assertTrue true
	}

   void testUpdate() {
        assertTrue true
	}

   void testDelete() {
        assertTrue true
	}
}
