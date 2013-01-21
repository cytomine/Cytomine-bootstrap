package be.cytomine.security

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.utils.BasicInstance
import be.cytomine.test.Infos

import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON
import be.cytomine.test.http.UserAnnotationAPI
import be.cytomine.ontology.UserAnnotation

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class UserAnnotationSecurityTests extends SecurityTestsAbstract {

    void testAnnotationSecurityForCytomineAdmin() {
        //Get User 1
        User user = getUser1()

        //Get cytomine admin
        User admin = getUserAdmin()

        //Create project with user 1
        ImageInstance image = BasicInstance.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with cytomine admin
        UserAnnotation annotation1 = BasicInstance.getBasicUserAnnotationNotExist()
        annotation1.image = image
        annotation1.project = project
        def result = UserAnnotationAPI.create(annotation1.encodeAsJSON(), SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assertEquals(200, result.code)
        annotation1 = result.data

        //Add annotation 2 with user 1
        UserAnnotation annotation2 = BasicInstance.getBasicUserAnnotationNotExist()
        annotation2.image = image
        annotation2.project = project
        Infos.printRight(annotation2.project)
        result = UserAnnotationAPI.create(annotation2.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotation2 = result.data

        //Get/List annotation with cytomine admin
        assertEquals(200, UserAnnotationAPI.show(annotation2.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)
        result = UserAnnotationAPI.listByProject(project.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assertEquals(200, result.code)
        log.info "JSON.parse(result.data)="+JSON.parse(result.data)
        assertTrue(UserAnnotationAPI.containsInJSONList(annotation2.id, JSON.parse(result.data)))

        //update annotation 2 with cytomine admin
        assertEquals(200, UserAnnotationAPI.update(annotation2.id,annotation2.encodeAsJSON(), SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)

        //Delete annotation 2 with cytomine admin
        assertEquals(200, UserAnnotationAPI.delete(annotation2.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)


    }

    void testAnnotationSecurityForProjectUserAndAnnotationCreator() {
        //Get User 1
        User user = getUser1()

        //Create project with user 1
        ImageInstance image = BasicInstance.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with user1
        UserAnnotation annotation = BasicInstance.getBasicUserAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = UserAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotation = result.data

        //Get/List annotation 1 with user 1
        assertEquals(200, UserAnnotationAPI.show(annotation.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
        result = UserAnnotationAPI.listByProject(project.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        assertTrue(UserAnnotationAPI.containsInJSONList(annotation.id, JSON.parse(result.data)))

        //update annotation 1 with user 1
        annotation.refresh()
        assertEquals(200, UserAnnotationAPI.update(annotation.id,annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)

        //Delete annotation 1 with user 1
        assertEquals(200, UserAnnotationAPI.delete(annotation.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
    }

    void testAnnotationSecurityForProjectUser() {
        //Get User 1
        User user1 = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        ImageInstance image = BasicInstance.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add project right for user 2
        def resAddUser = ProjectAPI.addUserProject(project.id, user2.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Infos.printRight(project)
        assertEquals(200, resAddUser.code)

        //Add annotation 1 with user 1
        UserAnnotation annotation = BasicInstance.getBasicUserAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = UserAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotation = result.data

        //Get/List annotation 1 with user 2
        assertEquals(200, UserAnnotationAPI.show(annotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
        result = UserAnnotationAPI.listByProject(project.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assertEquals(200, result.code)
        assertTrue(UserAnnotationAPI.containsInJSONList(annotation.id, JSON.parse(result.data)))

        //update annotation 1 with user 2
        assertEquals(403, UserAnnotationAPI.update(annotation.id,annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)

        //Delete annotation 1 with user 2
        assertEquals(403, UserAnnotationAPI.delete(annotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }


    void testAnnotationSecurityForUser() {
        //Get User 1
        User user1 = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        ImageInstance image = BasicInstance.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with user 1
        UserAnnotation annotation = BasicInstance.getBasicUserAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = UserAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotation = result.data

        //Get/List annotation 1 with user 2
        assertEquals(403, UserAnnotationAPI.show(annotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
        result = UserAnnotationAPI.listByProject(project.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assertEquals(403, result.code)

        //update annotation 1 with user 2
        assertEquals(403, UserAnnotationAPI.update(annotation.id,annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)

        //Delete annotation 1 with user 2
        assertEquals(403, UserAnnotationAPI.delete(annotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }



    void testAnnotationSecurityForAnonymous() {
        //Get User 1
        User user1 = getUser1()

        //Create project with user 1
        ImageInstance image = BasicInstance.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with user 1
        UserAnnotation annotation = BasicInstance.getBasicUserAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = UserAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotation = result.data

        //Get/List annotation 1 with user 2
        assertEquals(401, UserAnnotationAPI.show(annotation.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assertEquals(401, UserAnnotationAPI.listByProject(project.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assertEquals(401, UserAnnotationAPI.update(annotation.id,annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assertEquals(401, UserAnnotationAPI.delete(annotation.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
    }

}
