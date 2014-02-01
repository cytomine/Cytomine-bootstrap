package be.cytomine

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ReviewedAnnotationAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class ReviewedAnnotationListingTests {

    void testListReviewedAnnotation() {
         BasicInstanceBuilder.getReviewedAnnotation()
         def result = ReviewedAnnotationAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray
     }

     void testListReviewedAnnotationByProject() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray
         assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
     }

     void testListReviewedAnnotationByProjectWithProjectNotExist() {
         def result = ReviewedAnnotationAPI.listByProject(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 404 == result.code
     }

     void testListReviewedAnnotationByProjectAndUser() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def annotationNotCriteria = BasicInstanceBuilder.getReviewedAnnotationNotExist()
         annotationNotCriteria.project = annotation.project
         annotationNotCriteria.user = BasicInstanceBuilder.getUserNotExist()
         BasicInstanceBuilder.saveDomain(annotationNotCriteria.user)
         BasicInstanceBuilder.saveDomain(annotationNotCriteria)

         def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,annotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray
         assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
         assert !ReviewedAnnotationAPI.containsInJSONList(annotationNotCriteria.id,json)
     }

     void testListReviewedAnnotationByProjectAndUserWithUserNotExist() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 404 == result.code
     }

     void testListReviewedAnnotationByProjectAndUserAndImage() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         println "annotation.term="+annotation.terms
         println "annotation.term="+annotation.terms.id
         println "project.term="+annotation.project.ontology.terms()
         def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,annotation.user.id,annotation.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray
         assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
     }

//     void testListReviewedAnnotationByProjectAndUserAndImageWithImageNotExist() {
//         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
//         def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,annotation.user.id,-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
//         assert 404 == result.code
//     }

//     void testListReviewedAnnotationByProjectAndUserAndImageWithUserNotExist() {
//         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
//         def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,-99,annotation.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
//         assert 404 == result.code
//     }

     void testListReviewedAnnotationByProjectAndUserAndImageAndTerm() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()

         def annotationNotCriteria = BasicInstanceBuilder.getReviewedAnnotationNotExist()
         annotationNotCriteria.project = annotation.project
         annotationNotCriteria.image = annotation.image
         annotationNotCriteria.user = annotation.user

         def anotherTerm = BasicInstanceBuilder.getTermNotExist()
         anotherTerm.ontology = BasicInstanceBuilder.getOntology()
         BasicInstanceBuilder.saveDomain(anotherTerm)

         if(annotationNotCriteria.terms) annotationNotCriteria.terms.clear()
         annotationNotCriteria.addToTerms(anotherTerm)

         BasicInstanceBuilder.saveDomain(annotationNotCriteria)

         def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,annotation.user.id,annotation.image.id,annotation.termsId().first(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray
         assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
         assert !ReviewedAnnotationAPI.containsInJSONList(annotationNotCriteria.id,json)
     }

     void testListReviewedAnnotationByProjectAndUserAndImageAndTermWithTermNotExist() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def result = ReviewedAnnotationAPI.listByProject(annotation.project.id,annotation.user.id,annotation.image.id,-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 404 == result.code
     }


     void testListReviewedAnnotationByImage() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def result = ReviewedAnnotationAPI.listByImage(annotation.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray
         assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
     }

     void testListReviewedAnnotationByImageBBOX() {
         String bbox = "1,1,10000,10000"
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def result = ReviewedAnnotationAPI.listByImage(annotation.image.id,bbox,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code
     }

     void testListReviewedAnnotationByTermImage() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def term = BasicInstanceBuilder.getTerm()
         def result = ReviewedAnnotationAPI.listByImageAndTerm(annotation.image.id,term.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code

         result = ReviewedAnnotationAPI.listByImageAndTerm(-99,term.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 404 == result.code
     }

     void testListReviewedAnnotationByImageWithImageNotExist() {
         def result = ReviewedAnnotationAPI.listByImage(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 404 == result.code
     }

     void testListReviewedAnnotationByImageAndUser() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def result = ReviewedAnnotationAPI.listByImageAndUser(annotation.image.id,annotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray
         assert ReviewedAnnotationAPI.containsInJSONList(annotation.id,json)
     }

     void testListReviewedAnnotationByImageAndUserAndBBOX() {
         String bbox = "1,1,10000,10000"
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def result = ReviewedAnnotationAPI.listByImageAndUserAndBBOX(annotation.image.id,annotation.user.id,bbox,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code
     }

     void testListReviewedAnnotationByImageAndUserWithImageNotExist() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def result = ReviewedAnnotationAPI.listByImageAndUser(-99,annotation.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 404 == result.code
     }

     void testListReviewedAnnotationByImageAndUserWithUserNotExist() {
         def annotation = BasicInstanceBuilder.getReviewedAnnotation()
         def result = ReviewedAnnotationAPI.listByImageAndUser(annotation.image.id,-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 404 == result.code
     }
}
