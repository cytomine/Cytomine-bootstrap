package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.User

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.UserAnnotationAPI

import be.cytomine.test.http.DomainAPI
import com.vividsolutions.jts.io.WKTReader
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
class UserAnnotationTests  {

    void testGetUserAnnotationWithCredential() {
        def annotation = BasicInstanceBuilder.getUserAnnotation()
        def result = UserAnnotationAPI.show(annotation.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListUserAnnotationWithCredential() {
        BasicInstanceBuilder.getUserAnnotation()
        def result = UserAnnotationAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListUserAnnotationByImageWithCredential() {
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotation()
        def result = UserAnnotationAPI.listByImage(annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListUserAnnotationByImageNotExistWithCredential() {
        def result = UserAnnotationAPI.listByImage(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListUserAnnotationByProjectWithCredential() {
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotation()
        def result = UserAnnotationAPI.listByProject(annotation.project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = UserAnnotationAPI.listByProject(annotation.project.id, true,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
       json = JSON.parse(result.data)
    }

    void testListUserAnnotationByProjectNotExistWithCredential() {
        def result = UserAnnotationAPI.listByProject(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListUserAnnotationByProjecImageAndUsertWithCredential() {
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotation()
        def result = UserAnnotationAPI.listByProject(annotation.project.id, annotation.user.id, annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }




    void testListUserAnnotationByImageAndUserWithCredential() {
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotation()
        def result = UserAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = UserAnnotationAPI.listByImageAndUser(-99, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
        result = UserAnnotationAPI.listByImageAndUser(annotation.image.id, -99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }






    void testListUserAnnotationByProjectAndTermAndUserWithCredential() {
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTerm()

        def result = UserAnnotationAPI.listByProjectAndTerm(annotationTerm.userAnnotation.project.id, annotationTerm.term.id, annotationTerm.userAnnotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)

        result = UserAnnotationAPI.listByProjectAndTerm(-99, annotationTerm.term.id, annotationTerm.userAnnotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = UserAnnotationAPI.listByProjectAndTerm(annotationTerm.userAnnotation.project.id, -99, annotationTerm.userAnnotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
    
    void testListUserAnnotationByProjectAndTermWithUserNullWithCredential() {
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTerm()
        def result = UserAnnotationAPI.listByProjectAndTerm(annotationTerm.userAnnotation.project.id, annotationTerm.term.id, -1, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testListUserAnnotationByProjectAndTermAndUserAndImageWithCredential() {
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTerm()

        def result = UserAnnotationAPI.listByProjectAndTerm(annotationTerm.userAnnotation.project.id, annotationTerm.term.id, annotationTerm.userAnnotation.user.id,annotationTerm.userAnnotation.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        //assert json.collection instanceof JSONArray
    }

    void testListUserAnnotationyProjectAndUsersWithCredential() {
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotation()
        def result = UserAnnotationAPI.listByProjectAndUsers(annotation.project.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        //assert json.collection instanceof JSONArray
    }
    
    void testDownloadUserAnnotationDocument() {
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTerm()
        def result = UserAnnotationAPI.downloadDocumentByProject(annotationTerm.userAnnotation.project.id,annotationTerm.userAnnotation.user.id,annotationTerm.term.id, annotationTerm.userAnnotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testAddUserAnnotationCorrect() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def result = UserAnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        int idAnnotation = result.data.id

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = UserAnnotationAPI.undo()
        assert 200 == result.code

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = UserAnnotationAPI.redo()
        assert 200 == result.code

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testAddUserAnnotationMultipleCorrect() {
        def annotationToAdd1 = BasicInstanceBuilder.getUserAnnotation()
        def annotationToAdd2 = BasicInstanceBuilder.getUserAnnotation()
        def annotations = []
        annotations << JSON.parse(annotationToAdd1.encodeAsJSON())
        annotations << JSON.parse(annotationToAdd2.encodeAsJSON())
        def result = UserAnnotationAPI.create(annotations.encodeAsJSON() , Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testAddUserAnnotationCorrectWithoutProject() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.project = null
        def result = UserAnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testAddUserAnnotationCorrectWithTerm() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        Long idTerm1 = BasicInstanceBuilder.getTerm().id
        Long idTerm2 = BasicInstanceBuilder.getAnotherBasicTerm().id

        def annotationWithTerm = JSON.parse((String)annotationToAdd.encodeAsJSON())
        annotationWithTerm.term = [idTerm1, idTerm2]

        def result = UserAnnotationAPI.create(annotationWithTerm.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        int idAnnotation = result.data.id

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = UserAnnotationAPI.undo()
        assert 200 == result.code

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = UserAnnotationAPI.redo()
        assert 200 == result.code

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testAddUserAnnotationBadGeom() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POINT(BAD GEOMETRY)'

        Long idTerm1 = BasicInstanceBuilder.getTerm().id
        Long idTerm2 = BasicInstanceBuilder.getAnotherBasicTerm().id
        updateAnnotation.term = [idTerm1, idTerm2]

        def result = UserAnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testAddUserAnnotationBadGeomEmpty() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POLYGON EMPTY'
        def result = UserAnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testAddUserAnnotationBadGeomNull() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = null
        def result = UserAnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testAddUserAnnotationImageNotExist() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.image = -99
        def result = UserAnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testEditUserAnnotation() {
        UserAnnotation annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def data = UpdateData.createUpdateSet(
                BasicInstanceBuilder.getUserAnnotation(),
                [location: [new WKTReader().read("POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"),new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))")]]
        )

        def result = UserAnnotationAPI.update(annotationToAdd.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAnnotation = json.annotation.id

        def showResult = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = UserAnnotationAPI.undo()
        assert 200 == result.code
        showResult = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

        showResult = UserAnnotationAPI.redo()
        assert 200 == result.code
        showResult = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
    }

    void testEditUserAnnotationNotExist() {
        UserAnnotation annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        UserAnnotation annotationToEdit = UserAnnotation.get(annotationToAdd.id)
        def jsonAnnotation = JSON.parse((String)annotationToEdit.encodeAsJSON())
        jsonAnnotation.id = "-99"
        def result = UserAnnotationAPI.update(annotationToAdd.id, jsonAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testEditUserAnnotationWithBadGeometry() {
        UserAnnotation annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def jsonAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        jsonAnnotation.location = "POINT (BAD GEOMETRY)"
        def result = UserAnnotationAPI.update(annotationToAdd.id, jsonAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testDeleteUserAnnotation() {
        def annotationToDelete = BasicInstanceBuilder.getUserAnnotationNotExist()
        assert annotationToDelete.save(flush: true)  != null
        def id = annotationToDelete.id
        def result = UserAnnotationAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def showResult = UserAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code

        result = UserAnnotationAPI.undo()
        assert 200 == result.code

        result = UserAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = UserAnnotationAPI.redo()
        assert 200 == result.code

        result = UserAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteUserAnnotationNotExist() {
        def result = UserAnnotationAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteUserAnnotationWithData() {
        def annotTerm = BasicInstanceBuilder.getAnnotationTerm()
        def annotationToDelete = annotTerm.userAnnotation
        def result = UserAnnotationAPI.delete(annotationToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testListingUserAnnotationWithoutTerm() {
        //create annotation without term
        User user = BasicInstanceBuilder.getUser()
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        Infos.addUserRight(user.username,project)
        Ontology ontology = BasicInstanceBuilder.getOntology()
        project.ontology = ontology
        project.save(flush: true)

        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.project = project
        image.save(flush: true)


        UserAnnotation annotationWithoutTerm = BasicInstanceBuilder.getUserAnnotationNotExist()
        annotationWithoutTerm.project = project
        annotationWithoutTerm.image = image
        annotationWithoutTerm.user = user
        assert annotationWithoutTerm.save(flush: true)

        AnnotationTerm at = BasicInstanceBuilder.getAnnotationTermNotExist()
        at.term.ontology = ontology
        at.term.save(flush: true)
        at.user = user
        at.save(flush: true)
        UserAnnotation annotationWithTerm = at.userAnnotation
        annotationWithTerm.user = user
        annotationWithTerm.project = project
        annotationWithTerm.image = image
        assert annotationWithTerm.save(flush: true)

        AnnotationTerm at2 = BasicInstanceBuilder.getAnnotationTermNotExist()
        at2.term.ontology = ontology
        at2.term.save(flush: true)
        at2.user = BasicInstanceBuilder.getUser()
        at2.save(flush: true)
        UserAnnotation annotationWithTermFromOtherUser = at.userAnnotation
        annotationWithTermFromOtherUser.user = user
        annotationWithTermFromOtherUser.project = project
        annotationWithTermFromOtherUser.image = image
        assert annotationWithTermFromOtherUser.save(flush: true)

        //list annotation without term with this user
        def result = UserAnnotationAPI.listByProjectAndUsersWithoutTerm(project.id, user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(annotationWithoutTerm.id,json)
        assert !DomainAPI.containsInJSONList(annotationWithTerm.id,json)


        //list annotation without term with this user
        result = AnnotationDomainAPI.listByProjectAndUsersWithoutTerm(project.id, user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(annotationWithoutTerm.id,json)
        assert !DomainAPI.containsInJSONList(annotationWithTerm.id,json)
    }



    void testListingUserAnnotationWithSeveralTerm() {
        //create annotation without term
        User user = BasicInstanceBuilder.getUser()
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        Infos.addUserRight(user.username,project)
        Ontology ontology = BasicInstanceBuilder.getOntology()
        project.ontology = ontology
        project.save(flush: true)

        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.project = project
        image.save(flush: true)

        //annotation with no multiple term
        UserAnnotation annotationWithNoTerm = BasicInstanceBuilder.getUserAnnotationNotExist()
        annotationWithNoTerm.project = project
        annotationWithNoTerm.image = image
        annotationWithNoTerm.user = user
        assert annotationWithNoTerm.save(flush: true)

        //annotation with multiple term
        AnnotationTerm at = BasicInstanceBuilder.getAnnotationTermNotExist()
        at.term.ontology = ontology
        at.term.save(flush: true)
        at.user = user
        at.save(flush: true)
        UserAnnotation annotationWithMultipleTerm = at.userAnnotation
        annotationWithMultipleTerm.user = user
        annotationWithMultipleTerm.project = project
        annotationWithMultipleTerm.image = image
        assert annotationWithMultipleTerm.save(flush: true)
        AnnotationTerm at2 = BasicInstanceBuilder.getAnnotationTermNotExist()
        at2.term.ontology = ontology
        at2.term.save(flush: true)
        at2.user = user
        at2.userAnnotation=annotationWithMultipleTerm
        at2.save(flush: true)

        //list annotation without term with this user
        def result = UserAnnotationAPI.listByProjectAndUsersSeveralTerm(project.id, user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)

        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert !DomainAPI.containsInJSONList(annotationWithNoTerm.id,json)
        assert DomainAPI.containsInJSONList(annotationWithMultipleTerm.id,json)


        //list annotation without term with this user
        result = AnnotationDomainAPI.listByProjectAndUsersSeveralTerm(project.id, user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert !DomainAPI.containsInJSONList(annotationWithNoTerm.id,json)
        assert DomainAPI.containsInJSONList(annotationWithMultipleTerm.id,json)
    }


}
