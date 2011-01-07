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

    void testValidEmail() {
        mockDomain(User)
        def today = new Date()
        def user = new User(firstname:"John",lastname:"Doe",email:"johndoe@site.com", dateCreated:today,authority:"toto",password:"toto",username:"toto")
        println user.errors.firstname
        println user.errors.lastname
        println user.errors.email
        println user.errors.dateCreated
        println user.errors.authority

        if(user.validate()) {
           println "ok"
        }
        else {
            println "nok"
            user.errors.allErrors.each {
            println it
            }
        }

        assertFalse 'validation should be OK', user.validate()
    }

}
