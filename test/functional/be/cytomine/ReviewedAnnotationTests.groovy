package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationDomainAPI
import be.cytomine.test.http.DomainAPI
import be.cytomine.test.http.UserAnnotationAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.test.http.ReviewedAnnotationAPI
import be.cytomine.ontology.ReviewedAnnotation

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class ReviewedAnnotationTests extends functionaltestplugin.FunctionalTestCase {

    void testGetReviewedAnnotation() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.show(annotation.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testGetReviewedAnnotationNotExist() {
        def result = ReviewedAnnotationAPI.show(-99, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assertEquals(404, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListReviewedAnnotation() {
        BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListReviewedAnnotationByProject() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
    }

    void testListReviewedAnnotationByProjectWithProjectNotExist() {
        def result = ReviewedAnnotationAPI.listByProject(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListReviewedAnnotationByProjectAndUser() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def annotationNotCriteria = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotationNotCriteria.project = annotation.project
        annotationNotCriteria.user = BasicInstance.getBasicUserNotExist()
        BasicInstance.saveDomain(annotationNotCriteria.user)
        BasicInstance.checkDomain(annotationNotCriteria)
        BasicInstance.saveDomain(annotationNotCriteria)
        Infos.addUserRight(annotationNotCriteria.user,annotationNotCriteria.project)

        def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,annotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
        assert !ReviewedAnnotationAPI.containsInJSONList(annotationNotCriteria.id,json)
    }

    void testListReviewedAnnotationByProjectAndUserWithUserNotExist() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListReviewedAnnotationByProjectAndUserAndImage() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        println "annotation.term="+annotation.term
        println "annotation.term="+annotation.term.id
        println "project.term="+annotation.project.ontology.terms()
        def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,annotation.user.id,annotation.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
    }

    void testListReviewedAnnotationByProjectAndUserAndImageWithImageNotExist() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,annotation.user.id,-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListReviewedAnnotationByProjectAndUserAndImageWithUserNotExist() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,-99,annotation.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListReviewedAnnotationByProjectAndUserAndImageAndTerm() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()

        def annotationNotCriteria = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotationNotCriteria.project = annotation.project
        annotationNotCriteria.image = annotation.image
        annotationNotCriteria.user = annotation.user

        def anotherTerm = BasicInstance.getBasicTermNotExist()
        anotherTerm.ontology = BasicInstance.createOrGetBasicOntology()
        BasicInstance.checkDomain(anotherTerm)
        BasicInstance.saveDomain(anotherTerm)

        if(annotationNotCriteria.term) annotationNotCriteria.term.clear()
        annotationNotCriteria.addToTerm(anotherTerm)

        BasicInstance.checkDomain(annotationNotCriteria)
        BasicInstance.saveDomain(annotationNotCriteria)

        def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,annotation.user.id,annotation.image.id,annotation.termsId().first(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
        assert !ReviewedAnnotationAPI.containsInJSONList(annotationNotCriteria.id,json)
    }

    void testListReviewedAnnotationByProjectAndUserAndImageAndTermWithTermNotExist() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,annotation.user.id,annotation.image.id,-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }


    void testListReviewedAnnotationByImage() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.listByImage(annotation.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
    }

    void testListReviewedAnnotationByImageWithImageNotExist() {
        def result = ReviewedAnnotationAPI.listByImage(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListReviewedAnnotationByImageAndUser() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.listByImageAndUser(annotation.image.id,annotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
    }

    void testListReviewedAnnotationByImageAndUserWithImageNotExist() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.listByImageAndUser(-99,annotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListReviewedAnnotationByImageAndUserWithUserNotExist() {
        def annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.listByImageAndUser(annotation.image.id,-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }


    public def getReviewedAnnotationWithConflict(boolean conflict) {
        def basedAnnotation = BasicInstance.createOrGetBasicUserAnnotation()
        def goodTerm = BasicInstance.getBasicTermNotExist()
        goodTerm.ontology = basedAnnotation.project.ontology
        goodTerm.save(flush: true)
        def badTerm = BasicInstance.getBasicTermNotExist()
        badTerm.ontology = basedAnnotation.project.ontology
        badTerm.save(flush: true)

        def annotation1 = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotation1.putParentAnnotation(basedAnnotation)
        annotation1.image = basedAnnotation.image
        annotation1.project = basedAnnotation.project
        annotation1.save(flush: true)
        annotation1.addToTerm(goodTerm)
        annotation1.save(flush: true)

        def annotation2 = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotation2.putParentAnnotation(basedAnnotation)
        annotation2.image = basedAnnotation.image
        annotation2.project = basedAnnotation.project
        annotation2.save(flush: true)
        if(conflict) annotation2.addToTerm(badTerm)
        else annotation2.addToTerm(goodTerm)
        annotation2.save(flush: true)
        Infos.addUserRight(User.findByUsername(Infos.GOODLOGIN),basedAnnotation.project)
        return [based:basedAnnotation, review1:annotation1, review2: annotation2]
    }

    void testListReviewedAnnotationByProjectConflictWithNotConflict() {

        def data = getReviewedAnnotationWithConflict(false)
        def based = data.based
        def review1 = data.review1
        def review2 = data.review2
        def listConflict
        def json
        def result



        //annotation 1 and 2 have the same annotation parent but with equal term => no conflict!
        result = ReviewedAnnotationAPI.listByProjectConflict(based.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
        listConflict = json.get(based.id)
        assert listConflict == null

        result = ReviewedAnnotationAPI.listByProjectConflict(based.project.id,based.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
        listConflict = json.get(based.id)
        assert listConflict == null

        result = ReviewedAnnotationAPI.listByProjectConflict(based.project.id,based.user.id,based.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
        listConflict = json.get(based.id)
        assert listConflict == null

        result = ReviewedAnnotationAPI.listByProjectConflict(based.project.id,based.user.id,based.image.id,review1.termsId().first(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
        listConflict = json.get(based.id)
        assert listConflict == null
    }

    void testListReviewedAnnotationByProjectConflictWithConflict() {

        def data = getReviewedAnnotationWithConflict(true)
        def based = data.based
        def review1 = data.review1
        def review2 = data.review2

        println "review1="+ data.review1
        println "review2="+ data.review2
        println "review1="+ data.review1.id
        println "review2="+ data.review2.id

        def listConflict
        def json
        def result

        //annotation 1 and 2 have the same annotation parent but with different term => conflict!
        result = ReviewedAnnotationAPI.listByProjectConflict(based.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
        println json
        println "based.id="+based.id

        json.each {
            println it.key + "=>"+it.value
        }

        listConflict = json[based.id.toString()]
        assert listConflict!=null
        assert listConflict.size()==2
        assert ReviewedAnnotationAPI.containsInJSONList(review1.id,listConflict)
        assert ReviewedAnnotationAPI.containsInJSONList(review2.id,listConflict)

        result = ReviewedAnnotationAPI.listByProjectConflict(based.project.id,based.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
        listConflict = json[based.id.toString()]
        assert listConflict.size()==2
        assert ReviewedAnnotationAPI.containsInJSONList(review1.id,listConflict)
        assert ReviewedAnnotationAPI.containsInJSONList(review2.id,listConflict)

        result = ReviewedAnnotationAPI.listByProjectConflict(based.project.id,based.user.id,based.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
        listConflict = json[based.id.toString()]
        assert listConflict.size()==2
        assert ReviewedAnnotationAPI.containsInJSONList(review1.id,listConflict)
        assert ReviewedAnnotationAPI.containsInJSONList(review2.id,listConflict)

    }

    void testAddReviewedAnnotationCorrect() {
        def annotationToAdd = BasicInstance.getBasicReviewedAnnotationNotExist()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.term = BasicInstance.createOrGetBasicTerm().id
        println "json.encodeAsJSON()="+json.encodeAsJSON()
        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        int idAnnotation = result.data.id

        result = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.undo()
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)

        result = ReviewedAnnotationAPI.redo()
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddReviewedAnnotationCorrectWithoutTerm() {
        def annotationToAdd = BasicInstance.getBasicReviewedAnnotationNotExist()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.term = []

        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testAddReviewedAnnotationCorrectWithBadProject() {
        def annotationToAdd = BasicInstance.getBasicReviewedAnnotationNotExist()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.project = null

        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testAddReviewedAnnotationCorrectWithBadImage() {
        def annotationToAdd = BasicInstance.getBasicReviewedAnnotationNotExist()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.image = null

        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testAddReviewedAnnotationCorrectWithBadParent() {
        def annotationToAdd = BasicInstance.getBasicReviewedAnnotationNotExist()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.annotationIdent = null

        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testAddReviewedAnnotationCorrectWithBadStatus() {
        def annotationToAdd = BasicInstance.getBasicReviewedAnnotationNotExist()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.status = "toto"

        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }


    void testEditReviewedAnnotation() {
        ReviewedAnnotation annotationToAdd = BasicInstance.createOrGetBasicReviewedAnnotation()
        def result = ReviewedAnnotationAPI.update(annotationToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAnnotation = json.reviewedannotation.id

        def showResult = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareReviewedAnnotation(result.mapNew, json)

        showResult = ReviewedAnnotationAPI.undo()
        assertEquals(200, showResult.code)
        showResult = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstance.compareReviewedAnnotation(result.mapOld, JSON.parse(showResult.data))

        showResult = ReviewedAnnotationAPI.redo()
        assertEquals(200, showResult.code)
        showResult = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstance.compareReviewedAnnotation(result.mapNew, JSON.parse(showResult.data))
    }


//    void testDeleteReviewedAnnotation() {
//        def annotationToDelete = BasicInstance.getBasicReviewedAnnotationNotExist()
//        assert annotationToDelete.addToTerm(BasicInstance.createOrGetBasicTerm()).save(flush: true)  != null
//
//        def id = annotationToDelete.id
//
//        println annotationToDelete.encodeAsJSON()
//
//        println ReviewedAnnotation.read(id).encodeAsJSON()
//
//
//
//        def result = ReviewedAnnotationAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(200, result.code)
//
//        def showResult = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(404, showResult.code)
//
//        result = ReviewedAnnotationAPI.undo()
//        assertEquals(200, result.code)
//
//        result = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(200, result.code)
//
//        result = ReviewedAnnotationAPI.redo()
//        assertEquals(200, result.code)
//
//        result = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(404, result.code)
//    }
//
//    void testDeleteReviewedAnnotationWithTerm() {
//        def annotationToDelete = BasicInstance.getBasicReviewedAnnotationNotExist()
//        assert annotationToDelete.save(flush: true)  != null
//        annotationToDelete.addToTerm(BasicInstance.createOrGetBasicTerm())
//        //annotationToDelete.save(flush: true)
//        def id = annotationToDelete.id
//        println annotationToDelete.encodeAsJSON()
//        def result = ReviewedAnnotationAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(200, result.code)
//
//        def showResult = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(404, showResult.code)
//
//        result = ReviewedAnnotationAPI.undo()
//        assertEquals(200, result.code)
//
//        result = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(200, result.code)
//
//        result = ReviewedAnnotationAPI.redo()
//        assertEquals(200, result.code)
//
//        result = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(404, result.code)
//    }

    void testDeleteReviewedAnnotationNotExist() {
        def result = ReviewedAnnotationAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

}
