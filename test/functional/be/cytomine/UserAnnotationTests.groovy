package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.User

import be.cytomine.utils.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.UserAnnotationAPI

import be.cytomine.test.http.DomainAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.ontology.*
import be.cytomine.test.http.AnnotationDomainAPI
import be.cytomine.utils.UpdateData

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class UserAnnotationTests extends functionaltestplugin.FunctionalTestCase {

    void testGetUserAnnotationWithCredential() {
        def annotation = BasicInstance.createOrGetBasicUserAnnotation()
        def result = UserAnnotationAPI.show(annotation.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListUserAnnotationWithCredential() {
        BasicInstance.createOrGetBasicUserAnnotation()
        def result = UserAnnotationAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListUserAnnotationByImageWithCredential() {
        UserAnnotation annotation = BasicInstance.createOrGetBasicUserAnnotation()
        def result = UserAnnotationAPI.listByImage(annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListUserAnnotationByImageNotExistWithCredential() {
        def result = UserAnnotationAPI.listByImage(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListUserAnnotationByProjectWithCredential() {
        UserAnnotation annotation = BasicInstance.createOrGetBasicUserAnnotation()
        def result = UserAnnotationAPI.listByProject(annotation.project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = UserAnnotationAPI.listByProject(annotation.project.id, true,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
       json = JSON.parse(result.data)
    }

    void testListUserAnnotationByProjectNotExistWithCredential() {
        def result = UserAnnotationAPI.listByProject(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListUserAnnotationByProjecImageAndUsertWithCredential() {
        UserAnnotation annotation = BasicInstance.createOrGetBasicUserAnnotation()
        def result = UserAnnotationAPI.listByProject(annotation.project.id, annotation.user.id, annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }




    void testListUserAnnotationByImageAndUserWithCredential() {
        UserAnnotation annotation = BasicInstance.createOrGetBasicUserAnnotation()
        def result = UserAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = UserAnnotationAPI.listByImageAndUser(-99, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
        result = UserAnnotationAPI.listByImageAndUser(annotation.image.id, -99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }






    void testListUserAnnotationByProjectAndTermAndUserWithCredential() {
        AnnotationTerm annotationTerm = BasicInstance.createOrGetBasicAnnotationTerm()

        def result = UserAnnotationAPI.listByProjectAndTerm(annotationTerm.userAnnotation.project.id, annotationTerm.term.id, annotationTerm.userAnnotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)

        result = UserAnnotationAPI.listByProjectAndTerm(-99, annotationTerm.term.id, annotationTerm.userAnnotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)

        result = UserAnnotationAPI.listByProjectAndTerm(annotationTerm.userAnnotation.project.id, -99, annotationTerm.userAnnotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }
    
    void testListUserAnnotationByProjectAndTermWithUserNullWithCredential() {
        AnnotationTerm annotationTerm = BasicInstance.createOrGetBasicAnnotationTerm()
        def result = UserAnnotationAPI.listByProjectAndTerm(annotationTerm.userAnnotation.project.id, annotationTerm.term.id, -1, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testListUserAnnotationByProjectAndTermAndUserAndImageWithCredential() {
        AnnotationTerm annotationTerm = BasicInstance.createOrGetBasicAnnotationTerm()

        def result = UserAnnotationAPI.listByProjectAndTerm(annotationTerm.userAnnotation.project.id, annotationTerm.term.id, annotationTerm.userAnnotation.user.id,annotationTerm.userAnnotation.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        //assert json instanceof JSONArray
    }

    void testListUserAnnotationyProjectAndUsersWithCredential() {
        UserAnnotation annotation = BasicInstance.createOrGetBasicUserAnnotation()
        def result = UserAnnotationAPI.listByProjectAndUsers(annotation.project.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        //assert json instanceof JSONArray
    }

    void testListUserAnnotationByTerm() {
        AnnotationTerm annotationTerm = BasicInstance.createOrGetBasicAnnotationTerm()

        def result = UserAnnotationAPI.listByTerm(annotationTerm.term.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = UserAnnotationAPI.listByTerm(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }
    
    void testDownloadUserAnnotationDocument() {
        AnnotationTerm annotationTerm = BasicInstance.createOrGetBasicAnnotationTerm()
        def result = UserAnnotationAPI.downloadDocumentByProject(annotationTerm.userAnnotation.project.id,annotationTerm.userAnnotation.user.id,annotationTerm.term.id, annotationTerm.userAnnotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddUserAnnotationCorrect() {
        def annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        def result = UserAnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        int idAnnotation = result.data.id

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = UserAnnotationAPI.undo()
        assertEquals(200, result.code)

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)

        result = UserAnnotationAPI.redo()
        assertEquals(200, result.code)

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddUserAnnotationMultipleCorrect() {
        def annotationToAdd1 = BasicInstance.createOrGetBasicUserAnnotation()
        def annotationToAdd2 = BasicInstance.createOrGetBasicUserAnnotation()
        def annotations = []
        annotations << JSON.parse(annotationToAdd1.encodeAsJSON())
        annotations << JSON.parse(annotationToAdd2.encodeAsJSON())
        def result = UserAnnotationAPI.create(annotations.encodeAsJSON() , Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddUserAnnotationCorrectWithoutProject() {
        def annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.project = null
        def result = UserAnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddUserAnnotationCorrectWithTerm() {
        def annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        Long idTerm1 = BasicInstance.createOrGetBasicTerm().id
        Long idTerm2 = BasicInstance.createOrGetAnotherBasicTerm().id

        def annotationWithTerm = JSON.parse((String)annotationToAdd.encodeAsJSON())
        annotationWithTerm.term = [idTerm1, idTerm2]

        def result = UserAnnotationAPI.create(annotationWithTerm.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        int idAnnotation = result.data.id

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = UserAnnotationAPI.undo()
        assertEquals(200, result.code)

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)

        result = UserAnnotationAPI.redo()
        assertEquals(200, result.code)

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddUserAnnotationBadGeom() {
        def annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POINT(BAD GEOMETRY)'

        Long idTerm1 = BasicInstance.createOrGetBasicTerm().id
        Long idTerm2 = BasicInstance.createOrGetAnotherBasicTerm().id
        updateAnnotation.term = [idTerm1, idTerm2]

        def result = UserAnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testAddUserAnnotationBadGeomEmpty() {
        def annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POLYGON EMPTY'
        def result = UserAnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testAddUserAnnotationBadGeomNull() {
        def annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = null
        def result = UserAnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testAddUserAnnotationImageNotExist() {
        def annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.image = -99
        def result = UserAnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testEditUserAnnotation() {
        UserAnnotation annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        def data = UpdateData.createUpdateSet(annotationToAdd)
        def result = UserAnnotationAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAnnotation = json.annotation.id

        def showResult = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareAnnotation(data.mapNew, json)

        showResult = UserAnnotationAPI.undo()
        assertEquals(200, result.code)
        showResult = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstance.compareAnnotation(data.mapOld, JSON.parse(showResult.data))

        showResult = UserAnnotationAPI.redo()
        assertEquals(200, result.code)
        showResult = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstance.compareAnnotation(data.mapNew, JSON.parse(showResult.data))
    }

    void testEditUserAnnotationNotExist() {
        UserAnnotation annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        UserAnnotation annotationToEdit = UserAnnotation.get(annotationToAdd.id)
        def jsonAnnotation = JSON.parse((String)annotationToEdit.encodeAsJSON())
        jsonAnnotation.id = "-99"
        def result = UserAnnotationAPI.update(annotationToAdd.id, jsonAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testEditUserAnnotationWithBadGeometry() {
        UserAnnotation annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        def jsonAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        jsonAnnotation.location = "POINT (BAD GEOMETRY)"
        def result = UserAnnotationAPI.update(annotationToAdd.id, jsonAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testDeleteUserAnnotation() {
        def annotationToDelete = BasicInstance.getBasicUserAnnotationNotExist()
        assert annotationToDelete.save(flush: true)  != null
        def id = annotationToDelete.id
        def result = UserAnnotationAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        def showResult = UserAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, showResult.code)

        result = UserAnnotationAPI.undo()
        assertEquals(200, result.code)

        result = UserAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = UserAnnotationAPI.redo()
        assertEquals(200, result.code)

        result = UserAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteUserAnnotationNotExist() {
        def result = UserAnnotationAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteUserAnnotationWithData() {
        def annotTerm = BasicInstance.createOrGetBasicAnnotationTerm()
        def annotationToDelete = annotTerm.userAnnotation
        def result = UserAnnotationAPI.delete(annotationToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testListingUserAnnotationWithoutTerm() {
        //create annotation without term
        User user = BasicInstance.getNewUser()
        Project project = BasicInstance.createOrGetBasicProjectWithRight()
        Ontology ontology = BasicInstance.createOrGetBasicOntology()
        project.ontology = ontology
        project.save(flush: true)

        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.project = project
        image.save(flush: true)


        UserAnnotation annotationWithoutTerm = BasicInstance.getBasicUserAnnotationNotExist()
        annotationWithoutTerm.project = project
        annotationWithoutTerm.image = image
        annotationWithoutTerm.user = user
        assert annotationWithoutTerm.save(flush: true)

        AnnotationTerm at = BasicInstance.getBasicAnnotationTermNotExist("")
        at.term.ontology = ontology
        at.term.save(flush: true)
        at.user = user
        at.save(flush: true)
        UserAnnotation annotationWithTerm = at.userAnnotation
        annotationWithTerm.user = user
        annotationWithTerm.project = project
        annotationWithTerm.image = image
        assert annotationWithTerm.save(flush: true)

        AnnotationTerm at2 = BasicInstance.getBasicAnnotationTermNotExist("")
        at2.term.ontology = ontology
        at2.term.save(flush: true)
        at2.user = BasicInstance.getOldUser()
        at2.save(flush: true)
        UserAnnotation annotationWithTermFromOtherUser = at.userAnnotation
        annotationWithTermFromOtherUser.user = user
        annotationWithTermFromOtherUser.project = project
        annotationWithTermFromOtherUser.image = image
        assert annotationWithTermFromOtherUser.save(flush: true)

        //list annotation without term with this user
        def result = UserAnnotationAPI.listByProjectAndUsersWithoutTerm(project.id, user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        assert DomainAPI.containsInJSONList(annotationWithoutTerm.id,json)
        assert !DomainAPI.containsInJSONList(annotationWithTerm.id,json)


        //list annotation without term with this user
        result = AnnotationDomainAPI.listByProjectAndUsersWithoutTerm(project.id, user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONArray

        assert DomainAPI.containsInJSONList(annotationWithoutTerm.id,json)
        assert !DomainAPI.containsInJSONList(annotationWithTerm.id,json)
    }



    void testListingUserAnnotationWithSeveralTerm() {
        //create annotation without term
        User user = BasicInstance.getNewUser()
        Project project = BasicInstance.createOrGetBasicProjectWithRight()
        Ontology ontology = BasicInstance.createOrGetBasicOntology()
        project.ontology = ontology
        project.save(flush: true)

        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.project = project
        image.save(flush: true)

        //annotation with no multiple term
        UserAnnotation annotationWithNoTerm = BasicInstance.getBasicUserAnnotationNotExist()
        annotationWithNoTerm.project = project
        annotationWithNoTerm.image = image
        annotationWithNoTerm.user = user
        assert annotationWithNoTerm.save(flush: true)

        //annotation with multiple term
        AnnotationTerm at = BasicInstance.getBasicAnnotationTermNotExist("")
        at.term.ontology = ontology
        at.term.save(flush: true)
        at.user = user
        at.save(flush: true)
        UserAnnotation annotationWithMultipleTerm = at.userAnnotation
        annotationWithMultipleTerm.user = user
        annotationWithMultipleTerm.project = project
        annotationWithMultipleTerm.image = image
        assert annotationWithMultipleTerm.save(flush: true)
        AnnotationTerm at2 = BasicInstance.getBasicAnnotationTermNotExist("")
        at2.term.ontology = ontology
        at2.term.save(flush: true)
        at2.user = user
        at2.userAnnotation=annotationWithMultipleTerm
        at2.save(flush: true)

        //list annotation without term with this user
        def result = UserAnnotationAPI.listByProjectAndUsersSeveralTerm(project.id, user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        assert !DomainAPI.containsInJSONList(annotationWithNoTerm.id,json)
        assert DomainAPI.containsInJSONList(annotationWithMultipleTerm.id,json)


        //list annotation without term with this user
        result = AnnotationDomainAPI.listByProjectAndUsersSeveralTerm(project.id, user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONArray

        assert !DomainAPI.containsInJSONList(annotationWithNoTerm.id,json)
        assert DomainAPI.containsInJSONList(annotationWithMultipleTerm.id,json)
    }


}
