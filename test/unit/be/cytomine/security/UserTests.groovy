package be.cytomine.security

import grails.test.*

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
        def user = new User(firstname:"John",lastname:"Doe",email:"johndoe@site.com", dateCreated:today,password:"toto",username:"toto")

        assertEquals 'John', user.firstname
        assertEquals 'Doe', user.lastname
        assertEquals 'johndoe@site.com', user.email
    }


    void testValidEmail() {
        mockDomain(User)
        def today = new Date()
        def user = new User(firstname:"John",lastname:"Doe",email:"johndoe@site.com", dateCreated:today,password:"toto",username:"toto")

        /*if(user.validate()) {
           println "ok"
        }
        else {
            println "nok"
            user.errors.allErrors.each {
            println it
            }
        }    */

        assertTrue 'validation should be OK', user.validate()
    }

    void testInValidEmail() {
        mockDomain(User)
        def today = new Date()
        def user = new User(firstname:"John",lastname:"Doe",email:"johndoe@", dateCreated:today,password:"toto",username:"toto")

        assertFalse 'validation should be NOK', user.validate()
    }
}
