package be.cytomine.security

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON
import be.cytomine.ontology.Annotation
import be.cytomine.test.http.AnnotationAPI
import be.cytomine.SecurityTestsAbstract

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class AnnotationSecurityTests extends SecurityTestsAbstract {

    void testAnnotationSecurityForCytomineAdmin() {
        //Get User 1
        User user = getUser1()

        //Get cytomine admin
        User admin = getUserAdmin()

        //Create project with user 1
        ImageInstance image = AnnotationAPI.buildBasicImage(be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with cytomine admin
        Annotation annotation1 = BasicInstance.getBasicAnnotationNotExist()
        annotation1.image = image
        annotation1.project = image.project
        def result = AnnotationAPI.createAnnotation(annotation1, be.cytomine.SecurityTestsAbstract.USERNAMEADMIN, be.cytomine.SecurityTestsAbstract.PASSWORDADMIN)
        assertEquals(200, result.code)
        annotation1 = result.data

        //Add annotation 2 with user 1
        Annotation annotation2 = BasicInstance.getBasicAnnotationNotExist()
        annotation2.image = image
        annotation2.project = image.project
        Infos.printRight(annotation2.project)
        result = AnnotationAPI.createAnnotation(annotation2, be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotation2 = result.data

        //Get/List annotation with cytomine admin
        assertEquals(200, AnnotationAPI.showAnnotation(annotation2.id, be.cytomine.SecurityTestsAbstract.USERNAMEADMIN, be.cytomine.SecurityTestsAbstract.PASSWORDADMIN).code)
        result = AnnotationAPI.listAnnotationByProject(project.id, be.cytomine.SecurityTestsAbstract.USERNAMEADMIN, be.cytomine.SecurityTestsAbstract.PASSWORDADMIN)
        assertEquals(200, result.code)
        assertTrue(AnnotationAPI.containsInJSONList(annotation2.id, JSON.parse(result.data)))

        //update annotation 2 with cytomine admin
        assertEquals(200, AnnotationAPI.updateAnnotation(annotation2, be.cytomine.SecurityTestsAbstract.USERNAMEADMIN, be.cytomine.SecurityTestsAbstract.PASSWORDADMIN).code)

        //Delete annotation 2 with cytomine admin
        assertEquals(200, AnnotationAPI.deleteAnnotation(annotation2.id, be.cytomine.SecurityTestsAbstract.USERNAMEADMIN, be.cytomine.SecurityTestsAbstract.PASSWORDADMIN).code)


    }

    void testAnnotationSecurityForProjectUserAndAnnotationCreator() {
        //Get User 1
        User user = getUser1()

        //Create project with user 1
        ImageInstance image = AnnotationAPI.buildBasicImage(be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with user1
        Annotation annotation = BasicInstance.getBasicAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = AnnotationAPI.createAnnotation(annotation, be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotation = result.data
        log.info("### " + annotation.user.username + " " + annotation.user.id)
        log.info("### " + User.findByUsername(be.cytomine.SecurityTestsAbstract.USERNAME1).username + " " + User.findByUsername(be.cytomine.SecurityTestsAbstract.USERNAME1).id)

        //Get/List annotation 1 with user 1
        assertEquals(200, AnnotationAPI.showAnnotation(annotation.id, be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1).code)
        result = AnnotationAPI.listAnnotationByProject(project.id, be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        assertTrue(AnnotationAPI.containsInJSONList(annotation.id, JSON.parse(result.data)))

        //update annotation 1 with user 1
        log.info("### " + annotation.user.username + " " + annotation.user.id)
        annotation.refresh()
        log.info("### " + annotation.user.username + " " + annotation.user.id)
        assertEquals(200, AnnotationAPI.updateAnnotation(annotation, be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1).code)

        //Delete annotation 1 with user 1
        assertEquals(200, AnnotationAPI.deleteAnnotation(annotation.id, be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1).code)

    }

    void testAnnotationSecurityForProjectUser() {
        //Get User 1
        User user1 = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        ImageInstance image = AnnotationAPI.buildBasicImage(be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add project right for user 2
        def resAddUser = ProjectAPI.addUserProject(project.id, user2.id, be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        Infos.printRight(project)
        assertEquals(200, resAddUser.code)

        //Add annotation 1 with user 1
        Annotation annotation = BasicInstance.getBasicAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = AnnotationAPI.createAnnotation(annotation, be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotation = result.data

        //Get/List annotation 1 with user 2
        assertEquals(200, AnnotationAPI.showAnnotation(annotation.id, be.cytomine.SecurityTestsAbstract.USERNAME2, be.cytomine.SecurityTestsAbstract.PASSWORD2).code)
        result = AnnotationAPI.listAnnotationByProject(project.id, be.cytomine.SecurityTestsAbstract.USERNAME2, be.cytomine.SecurityTestsAbstract.PASSWORD2)
        assertEquals(200, result.code)
        assertTrue(AnnotationAPI.containsInJSONList(annotation.id, JSON.parse(result.data)))

        //update annotation 1 with user 2
        assertEquals(403, AnnotationAPI.updateAnnotation(annotation, be.cytomine.SecurityTestsAbstract.USERNAME2, be.cytomine.SecurityTestsAbstract.PASSWORD2).code)

        //Delete annotation 1 with user 2
        assertEquals(403, AnnotationAPI.deleteAnnotation(annotation.id, be.cytomine.SecurityTestsAbstract.USERNAME2, be.cytomine.SecurityTestsAbstract.PASSWORD2).code)
    }


    void testAnnotationSecurityForUser() {
        //Get User 1
        User user1 = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        ImageInstance image = AnnotationAPI.buildBasicImage(be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with user 1
        Annotation annotation = BasicInstance.getBasicAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = AnnotationAPI.createAnnotation(annotation, be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotation = result.data

        //Get/List annotation 1 with user 2
        assertEquals(403, AnnotationAPI.showAnnotation(annotation.id, be.cytomine.SecurityTestsAbstract.USERNAME2, be.cytomine.SecurityTestsAbstract.PASSWORD2).code)
        result = AnnotationAPI.listAnnotationByProject(project.id, be.cytomine.SecurityTestsAbstract.USERNAME2, be.cytomine.SecurityTestsAbstract.PASSWORD2)
        assertEquals(403, result.code)
        //assertTrue(AnnotationAPI.containsInJSONList(annotation.id, JSON.parse(result.data)))

        //update annotation 1 with user 2
        assertEquals(403, AnnotationAPI.updateAnnotation(annotation, be.cytomine.SecurityTestsAbstract.USERNAME2, be.cytomine.SecurityTestsAbstract.PASSWORD2).code)

        //Delete annotation 1 with user 2
        assertEquals(403, AnnotationAPI.deleteAnnotation(annotation.id, be.cytomine.SecurityTestsAbstract.USERNAME2, be.cytomine.SecurityTestsAbstract.PASSWORD2).code)
    }



    void testAnnotationSecurityForAnonymous() {
        //Get User 1
        User user1 = getUser1()

        //Create project with user 1
        ImageInstance image = AnnotationAPI.buildBasicImage(be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with user 1
        Annotation annotation = BasicInstance.getBasicAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = AnnotationAPI.createAnnotation(annotation, be.cytomine.SecurityTestsAbstract.USERNAME1, be.cytomine.SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotation = result.data

        //Get/List annotation 1 with user 2
        assertEquals(401, AnnotationAPI.showAnnotation(annotation.id, be.cytomine.SecurityTestsAbstract.USERNAMEBAD, be.cytomine.SecurityTestsAbstract.PASSWORDBAD).code)
        assertEquals(401, AnnotationAPI.listAnnotationByProject(project.id, be.cytomine.SecurityTestsAbstract.USERNAMEBAD, be.cytomine.SecurityTestsAbstract.PASSWORDBAD).code)
        assertEquals(401, AnnotationAPI.updateAnnotation(annotation, be.cytomine.SecurityTestsAbstract.USERNAMEBAD, be.cytomine.SecurityTestsAbstract.PASSWORDBAD).code)
        assertEquals(401, AnnotationAPI.deleteAnnotation(annotation.id, be.cytomine.SecurityTestsAbstract.USERNAMEBAD, be.cytomine.SecurityTestsAbstract.PASSWORDBAD).code)
    }

}
