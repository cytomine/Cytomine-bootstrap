package be.cytomine.security

import be.cytomine.ontology.Annotation
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.test.BasicInstance
import be.cytomine.test.http.AnnotationAPI
import be.cytomine.test.http.AnnotationTermAPI
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON

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
        Annotation annotation = AnnotationAPI.buildBasicAnnotation(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.annotation  = annotation
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assertEquals(200, result.code)
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assertEquals(200, AnnotationTermAPI.showAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.annotation.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assertEquals(200, result.code)
        assertTrue(AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assertEquals(200, AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)


    }

    void testAnnotationTermSecurityForAnnotationTermCreator() {
        //Get User 1
        User user = getUser1()

        //Add annotation 1 with cytomine admin
        Annotation annotation = AnnotationAPI.buildBasicAnnotation(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.annotation = annotation
        annotationTerm.user = User.findByUsername(SecurityTestsAbstract.USERNAME1)
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assertEquals(200, AnnotationTermAPI.showAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.annotation.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        assertTrue(AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assertEquals(200, AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
    }

    void testAnnotationTermSecurityForProjectUser() {
        //Get User 1
        User user = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        Annotation annotation = AnnotationAPI.buildBasicAnnotation(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)

        //Add project right for user 2
        def resAddUser = ProjectAPI.addUserProject(annotation.project.id, user2.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, resAddUser.code)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.annotation = annotation
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assertEquals(200, AnnotationTermAPI.showAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.annotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assertEquals(200, result.code)
        assertTrue(AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assertEquals(403, AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }

    void testAnnotationTermSecurityForUser() {
        //Get User 1
        User user = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        Annotation annotation = AnnotationAPI.buildBasicAnnotation(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.annotation = annotation
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assertEquals(403, AnnotationTermAPI.showAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.annotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assertEquals(403, result.code)
        //assertTrue(AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assertEquals(403, AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }

    void testAnnotationTermSecurityForAnonymous() {
        //Get User 1
        User user = getUser1()

        //Create project with user 1
        Annotation annotation = AnnotationAPI.buildBasicAnnotation(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.annotation = annotation
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assertEquals(200, result.code)
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assertEquals(401, AnnotationTermAPI.showAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assertEquals(401, AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.annotation.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assertEquals(401, AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.annotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
    }
}
