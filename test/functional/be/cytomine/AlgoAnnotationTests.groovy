package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotationTerm

import be.cytomine.ontology.Ontology
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.utils.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.DomainAPI
import be.cytomine.test.http.AlgoAnnotationAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.test.http.AnnotationDomainAPI
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.utils.UpdateData

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class AlgoAnnotationTests extends functionaltestplugin.FunctionalTestCase {

    void testGetAlgoAnnotationWithCredential() {
        def annotation = BasicInstance.createOrGetBasicAlgoAnnotation()
        def result = AlgoAnnotationAPI.show(annotation.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListAlgoAnnotationWithCredential() {
        BasicInstance.createOrGetBasicAlgoAnnotation()
        def result = AlgoAnnotationAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListAlgoAnnotationByImageWithCredential() {
        AlgoAnnotation annotation = BasicInstance.createOrGetBasicAlgoAnnotation()
        def result = AlgoAnnotationAPI.listByImage(annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListAlgoAnnotationByImageNotExistWithCredential() {
        def result = AlgoAnnotationAPI.listByImage(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListAlgoAnnotationByProjectWithCredential() {
        AlgoAnnotation annotation = BasicInstance.createOrGetBasicAlgoAnnotation()
        def result = AlgoAnnotationAPI.listByProject(annotation.project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListAlgoAnnotationByProjectWithOffset() {
        AlgoAnnotation annotation = BasicInstance.createOrGetBasicAlgoAnnotation()
        def result = AlgoAnnotationAPI.listByProject(annotation.project.id,true,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
    }

    void testListAlgoAnnotationByProjectNotExistWithCredential() {
        def result = AlgoAnnotationAPI.listByProject(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListAlgoAnnotationByProjecImageAndUsertWithCredential() {
        AlgoAnnotation annotation = BasicInstance.createOrGetBasicAlgoAnnotation()
        def result = AlgoAnnotationAPI.listByProject(annotation.project.id, annotation.user.id, annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = AlgoAnnotationAPI.listByProject(-99, annotation.user.id, annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListAlgoAnnotationByImageAndUserWithCredential() {
        AlgoAnnotation annotation = BasicInstance.createOrGetBasicAlgoAnnotation()
        AlgoAnnotation annotationWith2Term = BasicInstance.createOrGetBasicAlgoAnnotation()
        AlgoAnnotationTerm aat = BasicInstance.createAlgoAnnotationTerm(annotationWith2Term.user.job,annotationWith2Term,annotationWith2Term.user)


        def result = AlgoAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        String bbox = "1,1,10000,10000"

        result = AlgoAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, bbox, true,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = AlgoAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, bbox, false,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONArray

        result = AlgoAnnotationAPI.listByImageAndUser(-99, annotation.user.id, bbox, false,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
        result = AlgoAnnotationAPI.listByImageAndUser(annotation.image.id, -99, bbox, false,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }



    void testListAlgoAnnotationByProjectAndTermAndUserWithCredential() {
        AlgoAnnotationTerm annotationTerm = BasicInstance.createOrGetBasicAlgoAnnotationTermForAlgoAnnotation()
        Infos.addUserRight(Infos.GOODLOGIN,annotationTerm.retrieveAnnotationDomain().project)
        def result = AlgoAnnotationAPI.listByProjectAndTerm(annotationTerm.retrieveAnnotationDomain().project.id, annotationTerm.term.id, annotationTerm.retrieveAnnotationDomain().user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        //assert json instanceof JSONArray
        result = AlgoAnnotationAPI.listByProjectAndTerm(-99, annotationTerm.term.id, annotationTerm.retrieveAnnotationDomain().user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
        json = JSON.parse(result.data)
    }
    
    void testListAlgoAnnotationByProjectAndTermWithUserNullWithCredential() {
        AlgoAnnotationTerm annotationTerm = BasicInstance.createOrGetBasicAlgoAnnotationTermForAlgoAnnotation()
        def result = AlgoAnnotationAPI.listByProjectAndTerm(annotationTerm.retrieveAnnotationDomain().project.id, annotationTerm.term.id, -1, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        result = AlgoAnnotationAPI.listByProjectAndTerm(annotationTerm.retrieveAnnotationDomain().project.id, -99, -1, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(404, result.code)
    }

    void testListAlgoAnnotationByProjectAndTermAndUserAndImageWithCredential() {
        AlgoAnnotationTerm annotationTerm = BasicInstance.createOrGetBasicAlgoAnnotationTermForAlgoAnnotation()
        Infos.addUserRight(Infos.GOODLOGIN,annotationTerm.retrieveAnnotationDomain().project)
        def result = AlgoAnnotationAPI.listByProjectAndTerm(annotationTerm.retrieveAnnotationDomain().project.id, annotationTerm.term.id, annotationTerm.retrieveAnnotationDomain().user.id,annotationTerm.retrieveAnnotationDomain().image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        //assert json instanceof JSONArray
    }

    void testListAlgoAnnotationyProjectAndUsersWithCredential() {
        AlgoAnnotation annotation = BasicInstance.createOrGetBasicAlgoAnnotation()
        def result = AlgoAnnotationAPI.listByProjectAndUsers(annotation.project.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
    }
    
    void testDownloadAlgoAnnotationDocument() {
        AlgoAnnotationTerm annotationTerm = BasicInstance.createOrGetBasicAlgoAnnotationTermForAlgoAnnotation()
        def result = AlgoAnnotationAPI.downloadDocumentByProject(annotationTerm.retrieveAnnotationDomain().project.id,annotationTerm.retrieveAnnotationDomain().user.id,annotationTerm.term.id, annotationTerm.retrieveAnnotationDomain().image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddAlgoAnnotationCorrect() {
        def annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}
        def result = AlgoAnnotationAPI.create(annotationToAdd.encodeAsJSON(),user.username, 'PasswordUserJob')
        assertEquals(200, result.code)
        int idAnnotation = result.data.id

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assertEquals(200, result.code)

        result = AlgoAnnotationAPI.undo(user.username, 'PasswordUserJob')
        assertEquals(200, result.code)

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assertEquals(404, result.code)

        result = AlgoAnnotationAPI.redo(user.username, 'PasswordUserJob')
        assertEquals(200, result.code)

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assertEquals(200, result.code)
    }

    void testAddAlgoAnnotationMultipleCorrect() {
        def annotationToAdd1 = BasicInstance.createOrGetBasicAlgoAnnotation()
        def annotationToAdd2 = BasicInstance.createOrGetBasicAlgoAnnotation()
        annotationToAdd2.image =  annotationToAdd1.image
        annotationToAdd2.project =  annotationToAdd1.project
        annotationToAdd2.save(flush: true)

        UserJob user1 = annotationToAdd1.user
        try {Infos.addUserRight(user1.user.username,annotationToAdd1.project)} catch(Exception e) {println e}


        def annotations = []
        annotations << JSON.parse(annotationToAdd1.encodeAsJSON())
        annotations << JSON.parse(annotationToAdd2.encodeAsJSON())
        def result = AlgoAnnotationAPI.create(annotations.encodeAsJSON() , user1.username, 'PasswordUserJob')
        assertEquals(200, result.code)
    }

    void testAddAlgoAnnotationCorrectWithoutProject() {
        def annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.project = null
        def result = AlgoAnnotationAPI.create(updateAnnotation.encodeAsJSON(), user.username, 'PasswordUserJob')
        assertEquals(200, result.code)
    }

    void testAddAlgoAnnotationCorrectWithTerm() {
        def annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}


        Long idTerm1 = BasicInstance.createOrGetBasicTerm().id
        Long idTerm2 = BasicInstance.createOrGetAnotherBasicTerm().id

        def annotationWithTerm = JSON.parse((String)annotationToAdd.encodeAsJSON())
        annotationWithTerm.term = [idTerm1, idTerm2]

        def result = AlgoAnnotationAPI.create(annotationWithTerm.encodeAsJSON(), user.username, 'PasswordUserJob')
        assertEquals(200, result.code)
        int idAnnotation = result.data.id

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assertEquals(200, result.code)

        result = AlgoAnnotationAPI.undo(user.username, 'PasswordUserJob')
        assertEquals(200, result.code)

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assertEquals(404, result.code)

        result = AlgoAnnotationAPI.redo(user.username, 'PasswordUserJob')
        assertEquals(200, result.code)

        result = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        assertEquals(200, result.code)
    }

    void testAddAlgoAnnotationWithoutProject() {
        def annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.project = null

        def result = AlgoAnnotationAPI.create(updateAnnotation.encodeAsJSON(), user.username, 'PasswordUserJob')
        assertEquals(200, result.code)
    }

    void testAddAlgoAnnotationBadGeom() {
        def annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POINT(BAD GEOMETRY)'

        Long idTerm1 = BasicInstance.createOrGetBasicTerm().id
        Long idTerm2 = BasicInstance.createOrGetAnotherBasicTerm().id
        updateAnnotation.term = [idTerm1, idTerm2]

        def result = AlgoAnnotationAPI.create(updateAnnotation.encodeAsJSON(), user.username, 'PasswordUserJob')
        assertEquals(400, result.code)
    }

    void testAddAlgoAnnotationBadGeomEmpty() {
        def annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POLYGON EMPTY'
        def result = AlgoAnnotationAPI.create(updateAnnotation.encodeAsJSON(), user.username, 'PasswordUserJob')
        assertEquals(400, result.code)
    }

    void testAddAlgoAnnotationBadGeomNull() {
        def annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = null
        def result = AlgoAnnotationAPI.create(updateAnnotation.encodeAsJSON(), user.username, 'PasswordUserJob')
        assertEquals(400, result.code)
    }

    void testAddAlgoAnnotationImageNotExist() {
        def annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}

        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.image = -99
        def result = AlgoAnnotationAPI.create(updateAnnotation.encodeAsJSON(), user.username, 'PasswordUserJob')
        assertEquals(400, result.code)
    }

    void testEditAlgoAnnotation() {
        AlgoAnnotation annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}
        def data = UpdateData.createUpdateSet(annotationToAdd)
        def result = AlgoAnnotationAPI.update(data.oldData.id, data.newData,user.username, 'PasswordUserJob')
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAnnotation = json.annotation.id

        def showResult = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        json = JSON.parse(showResult.data)
        BasicInstance.compareAnnotation(data.mapNew, json)

        showResult = AlgoAnnotationAPI.undo(user.username, 'PasswordUserJob')
        assertEquals(200, result.code)
        showResult = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        BasicInstance.compareAnnotation(data.mapOld, JSON.parse(showResult.data))

        showResult = AlgoAnnotationAPI.redo(user.username, 'PasswordUserJob')
        assertEquals(200, result.code)
        showResult = AlgoAnnotationAPI.show(idAnnotation, user.username, 'PasswordUserJob')
        BasicInstance.compareAnnotation(data.mapNew, JSON.parse(showResult.data))
    }

    void testEditAlgoAnnotationNotExist() {
        AlgoAnnotation annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}

        AlgoAnnotation annotationToEdit = AlgoAnnotation.get(annotationToAdd.id)
        def jsonAnnotation = JSON.parse((String)annotationToEdit.encodeAsJSON())
        jsonAnnotation.id = "-99"
        def result = AlgoAnnotationAPI.update(annotationToAdd.id, jsonAnnotation.encodeAsJSON(), user.username,'PasswordUserJob')
        assertEquals(404, result.code)
    }

    void testEditAlgoAnnotationWithBadGeometry() {
        AlgoAnnotation annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
        UserJob user = annotationToAdd.user
        try {Infos.addUserRight(user.user.username,annotationToAdd.project)} catch(Exception e) {println e}

        def jsonAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        jsonAnnotation.location = "POINT (BAD GEOMETRY)"
        def result = AlgoAnnotationAPI.update(annotationToAdd.id, jsonAnnotation.encodeAsJSON(), user.username, 'PasswordUserJob')
        assertEquals(400, result.code)
    }

    void testDeleteAlgoAnnotation() {
        def annotationToDelete = BasicInstance.getBasicAlgoAnnotationNotExist()
        assert annotationToDelete.save(flush: true)  != null
        UserJob user = annotationToDelete.user
        try {Infos.addUserRight(user.user.username,annotationToDelete.project)} catch(Exception e) {println e}

        def id = annotationToDelete.id
        def result = AlgoAnnotationAPI.delete(id, user.username, 'PasswordUserJob')
        assertEquals(200, result.code)

        def showResult = AlgoAnnotationAPI.show(id, user.username,'PasswordUserJob')
        assertEquals(404, showResult.code)

        result = AlgoAnnotationAPI.undo(user.username, 'PasswordUserJob')
        assertEquals(200, result.code)

        result = AlgoAnnotationAPI.show(id, user.username,'PasswordUserJob')
        assertEquals(200, result.code)

        result = AlgoAnnotationAPI.redo(user.username, 'PasswordUserJob')
        assertEquals(200, result.code)

        result = AlgoAnnotationAPI.show(id, user.username,'PasswordUserJob')
        assertEquals(404, result.code)
    }

    void testDeleteAlgoAnnotationNotExist() {
        def result = AlgoAnnotationAPI.delete(-99, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteAlgoAnnotationWithData() {
        def annotTerm = BasicInstance.createOrGetBasicAlgoAnnotationTermForAlgoAnnotation()
        UserJob user = annotTerm.retrieveAnnotationDomain().user
        try {Infos.addUserRight(user.user.username,annotTerm.retrieveAnnotationDomain().project)} catch(Exception e) {println e}

        def annotationToDelete = annotTerm.retrieveAnnotationDomain()
        def result = AlgoAnnotationAPI.delete(annotationToDelete.id,user.username,'PasswordUserJob')
        assertEquals(200, result.code)
    }

    void testListingAlgoAnnotationWithoutTermAlgo() {
        //create annotation without term
        UserJob userJob = BasicInstance.createOrGetBasicUserJob()
        User user = User.findByUsername(Infos.GOODLOGIN)
        Project project = BasicInstance.getBasicProjectNotExist()
        Ontology ontology = BasicInstance.createOrGetBasicOntology()
        project.ontology = ontology
        project.save(flush: true)
        try {Infos.addUserRight(userJob.user,project) }catch(Exception e){println e}
        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.project = project
        image.save(flush: true)

        AlgoAnnotation annotationWithoutTerm = BasicInstance.getBasicAlgoAnnotationNotExist()
        annotationWithoutTerm.project = project
        annotationWithoutTerm.image = image
        annotationWithoutTerm.user = userJob
        assert annotationWithoutTerm.save(flush: true)

        AlgoAnnotationTerm at = BasicInstance.getBasicAlgoAnnotationTermNotExistForAlgoAnnotation()
        at.term.ontology = ontology
        at.term.save(flush: true)
        at.project = project
        at.userJob = userJob
        println at.validate()
        println at.annotationClassName
        println at.annotationIdent
        println "#######################"
        at.save(flush: true)
        println "***********************"
        AnnotationDomain annotationWithTerm = at.retrieveAnnotationDomain()
        annotationWithTerm.user = userJob
        annotationWithTerm.project = project
        annotationWithTerm.image = image
        assert annotationWithTerm.save(flush: true)

        //list annotation without term with this user
        def result = AlgoAnnotationAPI.listByProjectAndUsersWithoutTerm(project.id, userJob.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        assert DomainAPI.containsInJSONList(annotationWithoutTerm.id,json)
        assert !DomainAPI.containsInJSONList(annotationWithTerm.id,json)

        //list annotation without term with this user
        result = AnnotationDomainAPI.listByProjectAndUsersWithoutTerm(project.id, userJob.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONArray

        assert DomainAPI.containsInJSONList(annotationWithoutTerm.id,json)
        assert !DomainAPI.containsInJSONList(annotationWithTerm.id,json)
    }

    void testListingAlgoAnnotationWithSeveralTermAlgo() {
        //create annotation without term
        UserJob userJob = BasicInstance.createOrGetBasicUserJob()
        User user = User.findByUsername(Infos.GOODLOGIN)
        Project project = BasicInstance.getBasicProjectNotExist()
        Ontology ontology = BasicInstance.createOrGetBasicOntology()
        project.ontology = ontology
        project.save(flush: true)
        try {Infos.addUserRight(userJob.user,project) }catch(Exception e){println e}
        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.project = project
        image.save(flush: true)

        //annotation with no multiple term
        AlgoAnnotation annotationWithNoTerm = BasicInstance.getBasicAlgoAnnotationNotExist()
        annotationWithNoTerm.project = project
        annotationWithNoTerm.image = image
        annotationWithNoTerm.user = userJob
        assert annotationWithNoTerm.save(flush: true)

        //annotation with multiple term
        AlgoAnnotationTerm at = BasicInstance.getBasicAlgoAnnotationTermNotExistForAlgoAnnotation()
        at.term.ontology = ontology
        at.term.save(flush: true)
        at.userJob = userJob
        at.project = project
        assert at.save(flush: true)

        AnnotationDomain annotationWithMultipleTerm = at.retrieveAnnotationDomain()
        annotationWithMultipleTerm.user = userJob
        annotationWithMultipleTerm.project = project
        annotationWithMultipleTerm.image = image
        assert annotationWithMultipleTerm.save(flush: true)
        AlgoAnnotationTerm at2 = BasicInstance.getBasicAlgoAnnotationTermNotExistForAlgoAnnotation()
        at2.term.ontology = ontology
        at2.project = project
        assert at2.term.save(flush: true)
        at2.userJob = userJob
        at2.setAnnotation(annotationWithMultipleTerm)
        at2.save(flush: true)

        //list annotation without term with this user
        def result = AlgoAnnotationAPI.listByProjectAndUsersSeveralTerm(project.id, userJob.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray

        assert !DomainAPI.containsInJSONList(annotationWithNoTerm.id,json)
        assert DomainAPI.containsInJSONList(annotationWithMultipleTerm.id,json)

        //list annotation without term with this user
        result = AnnotationDomainAPI.listByProjectAndUsersSeveralTerm(project.id, userJob.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONArray

        assert !DomainAPI.containsInJSONList(annotationWithNoTerm.id,json)
        assert DomainAPI.containsInJSONList(annotationWithMultipleTerm.id,json)
    }

    void testUnionAlgoAnnotationWithNotFound() {
        def a1 = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        def result
        result = AlgoAnnotationAPI.union(-99,a1.retrieveAnnotationDomain().user.id,a1.term.id,10,20, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
        result = AlgoAnnotationAPI.union(a1.retrieveAnnotationDomain().image.id,-99,a1.term.id,10,20, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
        result = AlgoAnnotationAPI.union(a1.retrieveAnnotationDomain().image.id,a1.retrieveAnnotationDomain().user.id,-99,10,20, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testUnionAlgoAnnotationByProjectWithCredential() {
        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.save(flush: true)
        assert AlgoAnnotation.findAllByImage(image).size()==0

        def a1 = BasicInstance.getBasicAlgoAnnotationNotExist()
        try {Infos.addUserRight(a1.user.user,image.project) }catch(Exception e){println e}
        a1.location = new WKTReader().read("POLYGON ((0 0, 0 5000, 10000 5000, 10000 0, 0 0))")
        a1.image = image
        a1.project = image.project
        assert a1.save(flush: true)  != null

        def a2 = BasicInstance.getBasicAlgoAnnotationNotExist()
        a2.location = new WKTReader().read("POLYGON ((0 5000, 10000 5000, 10000 10000, 0 10000, 0 5000))")
        a2.image = image
        a2.project = image.project
        assert a2.save(flush: true)  != null

        def at1 = BasicInstance.createAlgoAnnotationTerm(a1.user.job,a1,a1.user)
        def at2 = BasicInstance.createAlgoAnnotationTerm(a2.user.job,a2,a2.user)
        at2.term = at1.term
        at2.save(flush:true)

        assert AlgoAnnotation.findAllByImage(a1.image).size()==2

        def result = AlgoAnnotationAPI.union(a1.image.id,a1.user.id,a1.terms().first().id,10,20, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        assert AlgoAnnotation.findAllByImage(a1.image).size()==1
    }

    void testUnionAlgoAnnotationByProjectWithCredentialBufferNull() {
        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.save(flush: true)
        assert AlgoAnnotation.findAllByImage(image).size()==0

        def a1 = BasicInstance.getBasicAlgoAnnotationNotExist()
        try {Infos.addUserRight(a1.user.user,image.project) }catch(Exception e){println e}
        a1.location = new WKTReader().read("POLYGON ((0 0, 0 5100, 10000 5100, 10000 0, 0 0))")
        a1.image = image
        a1.project = image.project
        assert a1.save(flush: true)  != null

        def a2 = BasicInstance.getBasicAlgoAnnotationNotExist()
        a2.location = new WKTReader().read("POLYGON ((0 5000, 10000 5000, 10000 10000, 0 10000, 0 5000))")
        a2.image = image
        a2.project = image.project
        assert a2.save(flush: true)  != null

        def at1 = BasicInstance.createAlgoAnnotationTerm(a1.user.job,a1,a1.user)
        def at2 = BasicInstance.createAlgoAnnotationTerm(a2.user.job,a2,a2.user)
        at2.term = at1.term
        at2.save(flush:true)

        assert AlgoAnnotation.findAllByImage(a1.image).size()==2

        def result = AlgoAnnotationAPI.union(a1.image.id,a1.user.id,a1.terms().first().id,10,null, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        assert AlgoAnnotation.findAllByImage(a1.image).size()==1
    }

}
