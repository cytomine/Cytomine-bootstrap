package be.cytomine

import be.cytomine.ontology.Annotation

import be.cytomine.security.User
import be.cytomine.test.BasicInstance

import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.test.http.AnnotationTermAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTermSecurityTests extends SecurityTestsAbstract {

    void testAnnotationTermSecurityForCytomineAdmin() {
        //Get User 1
        User user = getUser1()

        //Get cytomine admin
        User admin = getUserAdmin()

        //Create Annotation with user 1
        Annotation annotation = AnnotationTermAPI.buildBasicAnnotation(USERNAME1, PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.annotation  = annotation
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm, USERNAMEADMIN, PASSWORDADMIN)
        assertEquals(200, result.code)
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assertEquals(200, AnnotationTermAPI.showAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, USERNAMEADMIN, PASSWORDADMIN).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.annotation.id, USERNAMEADMIN, PASSWORDADMIN)
        assertEquals(200, result.code)
        assertTrue(AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assertEquals(200, AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, USERNAMEADMIN, PASSWORDADMIN).code)


    }

    void testAnnotationTermSecurityForAnnotationTermCreator() {
        //Get User 1
        User user = getUser1()

        //Add annotation 1 with cytomine admin
        Annotation annotation = AnnotationTermAPI.buildBasicAnnotation(USERNAME1, PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.annotation = annotation
        annotationTerm.user = User.findByUsername(USERNAME1)
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm, USERNAME1, PASSWORD1)
        assertEquals(200, result.code)
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assertEquals(200, AnnotationTermAPI.showAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, USERNAME1, PASSWORD1).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.annotation.id, USERNAME1, PASSWORD1)
        assertEquals(200, result.code)
        assertTrue(AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assertEquals(200, AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, USERNAME1, PASSWORD1).code)
    }

    void testAnnotationTermSecurityForProjectUser() {
        //Get User 1
        User user = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        Annotation annotation = AnnotationTermAPI.buildBasicAnnotation(USERNAME1, PASSWORD1)

        //Add project right for user 2
        def resAddUser = ProjectAPI.addUserProject(annotation.project.id, user2.id, USERNAME1, PASSWORD1)
        assertEquals(200, resAddUser.code)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.annotation = annotation
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm, USERNAME1, PASSWORD1)
        assertEquals(200, result.code)
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assertEquals(200, AnnotationTermAPI.showAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, USERNAME2, PASSWORD2).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.annotation.id, USERNAME2, PASSWORD2)
        assertEquals(200, result.code)
        assertTrue(AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assertEquals(403, AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, USERNAME2, PASSWORD2).code)
    }

    void testAnnotationTermSecurityForUser() {
        //Get User 1
        User user = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        Annotation annotation = AnnotationTermAPI.buildBasicAnnotation(USERNAME1, PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.annotation = annotation
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm, USERNAME1, PASSWORD1)
        assertEquals(200, result.code)
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assertEquals(403, AnnotationTermAPI.showAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, USERNAME2, PASSWORD2).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.annotation.id, USERNAME2, PASSWORD2)
        assertEquals(403, result.code)
        //assertTrue(AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assertEquals(403, AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, USERNAME2, PASSWORD2).code)
    }

    void testAnnotationTermSecurityForAnonymous() {
        //Get User 1
        User user = getUser1()

        //Create project with user 1
        Annotation annotation = AnnotationTermAPI.buildBasicAnnotation(USERNAME1, PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.annotation = annotation
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm, USERNAME1, PASSWORD1)
        assertEquals(200, result.code)
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assertEquals(401, AnnotationTermAPI.showAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, USERNAMEBAD, PASSWORDBAD).code)
        assertEquals(401, AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.annotation.id, USERNAMEBAD, PASSWORDBAD).code)
        assertEquals(401, AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, USERNAMEBAD, PASSWORDBAD).code)
    }
}
