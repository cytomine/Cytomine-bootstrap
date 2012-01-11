package be.cytomine.security
import grails.test.*
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
class UserTests extends GrailsUnitTestCase {
   protected void setUp() {
       super.setUp()
   }

   protected void tearDown() {
       super.tearDown()
   }

   void testSomething() {

   }

     void testAddUser() {
       mockDomain(User)
       def today = new Date()
       def user = new User(firstname:"John",lastname:"Doe",email:"johndoe@site.com",password:"totototot",username:"toto")

       assertEquals 'John', user.firstname
       assertEquals 'Doe', user.lastname
       assertEquals 'johndoe@site.com', user.email
   }

   void testInValidEmail() {
       mockDomain(User)
       def today = new Date()
       def user = new User(firstname:"John",lastname:"Doe",email:"johndoe@",password:"totototo",username:"toto")

       assertFalse 'validation should be NOK', user.validate()
   }
}

