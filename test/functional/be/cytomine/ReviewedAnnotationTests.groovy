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
import be.cytomine.processing.Job
import be.cytomine.security.UserJob
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.security.SecUser

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

//    void testAddReviewedAnnotationCorrectWithoutTerm() {
//        def annotationToAdd = BasicInstance.getBasicReviewedAnnotationNotExist()
//        def json = JSON.parse(annotationToAdd.encodeAsJSON())
//        json.term = []
//
//        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(400, result.code)
//    }

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

//    void testAddReviewedAnnotationCorrectWithBadParent() {
//        def annotationToAdd = BasicInstance.getBasicReviewedAnnotationNotExist()
//        def json = JSON.parse(annotationToAdd.encodeAsJSON())
//        json.annotationIdent = null
//
//        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(400, result.code)
//    }

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



//    void testAddReviewedAnnotationForAllJobData() {
//        Job job = BasicInstance.getBasicJobNotExist()
//        BasicInstance.checkDomain(job)
//        BasicInstance.saveDomain(job)
//        BasicInstance.createSoftwareProject(job.software,job.project)
//
//        UserJob userJob = BasicInstance.getBasicUserJobNotExist()
//        userJob.job = job
//        userJob.user = BasicInstance.getNewUser()
//        BasicInstance.checkDomain(userJob)
//        BasicInstance.saveDomain(userJob)
//
//        //add algo-annotation for this job
//        AlgoAnnotation a1 = createAlgoAnnotation(job, userJob)
//
//        //add algo-annotation-term for this job
//        AlgoAnnotationTerm at1 = createAlgoAnnotationTerm(job, a1, userJob)
//
//        //add user-annotation for this job
//        UserAnnotation a2 = createUserAnnotation(job)
//
//        //add algo-annotation-term for this job
//        AlgoAnnotationTerm at2 = createAlgoAnnotationTerm(job, a2, userJob)
//
//        //add algo-annotation for this job without term!
//        AlgoAnnotation a3 = createAlgoAnnotation(job,userJob)
//
//
//        Infos.addUserRight(userJob.user,job.project)
//
//
//        assert ReviewedAnnotation.findAllByParentIdent(a1.id).size() == 0
//        assert ReviewedAnnotation.findAllByParentIdent(a2.id).size() == 0
//        assert ReviewedAnnotation.findAllByParentIdent(a3.id).size() == 0
//
//        def result = ReviewedAnnotationAPI.addForJob(job.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(200, result.code)
//        def json = JSON.parse(result.data)
//        assert json instanceof JSONArray
//        assert json.size()==1
//        //a3 shouldn't be reviewed (because no term), and should be in response with all annotation not reviewed
//        assert json.get(0)["id"]==a3.id
//
//        assert ReviewedAnnotation.findAllByParentIdent(a1.id).size() == 1
//        assert ReviewedAnnotation.findAllByParentIdent(a2.id).size() == 1
//        assert ReviewedAnnotation.findAllByParentIdent(a3.id).size() == 0
//
//    }




//    void testAddReviewedAnnotationForDataFromImageUser() {
//
//    }



    void testStartImageReviewing() {
         //check image review flag true AND false AND true (no rev => rev => stop rev)

          //create image
          ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())

          //check image attributes
          def result = ImageInstanceAPI.show(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)
          def json = JSON.parse(result.data)
          json = JSON.parse(result.data)
          assert json instanceof JSONObject
          assert json.id == image.id
          assert json.isNull('reviewStart')
          assert json.isNull('reviewStop')
          assert json.isNull('reviewUser')

          //mark start review + check attr
          result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)
          json = JSON.parse(result.data)
          assert json instanceof JSONObject
          assert json.imageinstance.id == image.id
          assert !json.imageinstance.isNull('reviewStart')
          assert json.imageinstance.isNull('reviewStop')
          assert !json.imageinstance.isNull('reviewUser')

          //mark stop review + check attr
          result = ReviewedAnnotationAPI.markStopReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)
          json = JSON.parse(result.data)
          assert json instanceof JSONObject
          assert json.imageinstance.id == image.id
        assert !json.imageinstance.isNull('reviewStart')
        assert !json.imageinstance.isNull('reviewStop')
        assert !json.imageinstance.isNull('reviewUser')
          assert Long.parseLong(json.imageinstance.reviewStart.toString())<Long.parseLong(json.imageinstance.reviewStop.toString())
      }

      void testLockImageReviewing() {
          //check image lock, only review if image is mark as review star
          //create image
          ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())

          //add review
          UserAnnotation annotation = BasicInstance.createUserAnnotation(image.project,image)

          def result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(ConstraintException.CODE, result.code)

          //mark start review + check attr
          result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)

          //add review
          result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)

          //mark stop review + check attr
          result = ReviewedAnnotationAPI.markStopReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)

          //create image
          ImageInstance image2 = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
          UserAnnotation annotation2 = BasicInstance.createUserAnnotation(image2.project,image2)
          result = ReviewedAnnotationAPI.addReviewAnnotation(annotation2.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(ConstraintException.CODE, result.code)

      }

     void testLockImageReviewingForOtherUser() {
          //create image
          ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
          try {Infos.addUserRight(Infos.ANOTHERLOGIN,image.project)} catch(Exception e) {}

          //mark start review + check attr
          def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)

          //add review with another login/user
          UserAnnotation annotation = BasicInstance.createUserAnnotation(image.project,image)
          result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.ANOTHERLOGIN, Infos.ANOTHERPASSWORD)
          assertEquals(ConstraintException.CODE, result.code)

     }

      void testUnReviewing() {
          //review image => add review => check image is not reviewed
          //create image
          ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
          UserAnnotation annotation = BasicInstance.createUserAnnotation(image.project,image)

          //mark start review + check attr
          def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)

          result = ReviewedAnnotationAPI.markStopReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)

          result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)

          result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assertEquals(200, result.code)
      }

    void testAddReviewForAnnotationTerm() {
        ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
        UserAnnotation annotation = BasicInstance.createUserAnnotation(image.project,image)

        def result = ReviewedAnnotationAPI.markStartReview(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert Long.parseLong(json.reviewedannotation.parentIdent.toString()) == annotation.id
        assert json.reviewedannotation.term !=null
        assert json.reviewedannotation.term.size()==1

        def idReviewAnnotation = json.reviewedannotation.id
        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 1
        result = ReviewedAnnotationAPI.undo()
        assertEquals(200, result.code)

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 0
        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)


        result = ReviewedAnnotationAPI.redo()
        assertEquals(200, result.code)

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 1
        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddReviewForAlgoAnnotationTerm() {
        ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
        UserJob user = BasicInstance.createUserJob(image.project)
        AlgoAnnotation annotation = BasicInstance.createAlgoAnnotation(user.job,user)
        annotation.image = image
        BasicInstance.saveDomain(annotation)
        BasicInstance.createAlgoAnnotationTerm(user.job,annotation,user)


        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert Long.parseLong(json.reviewedannotation.parentIdent.toString()) == annotation.id
        assert json.reviewedannotation.term !=null
        assert json.reviewedannotation.term.size()==1
    }







    void testRemoveReviewForAlgoAnnotationTerm() {
        ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
        UserJob user = BasicInstance.createUserJob(image.project)
        AlgoAnnotation annotation = BasicInstance.createAlgoAnnotation(user.job,user)
        annotation.image = image
        BasicInstance.saveDomain(annotation)
        BasicInstance.createAlgoAnnotationTerm(user.job,annotation,user)


        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
//        def json = JSON.parse(result.data)
//        def idReviewAnnotation = json.reviewedannotation.i

        result = ReviewedAnnotationAPI.removeReviewAnnotation(annotation.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }


    void testRemoveReviewForAnnotationTerm() {
        ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
        UserAnnotation annotation = BasicInstance.createUserAnnotation(image.project,image)

        def result = ReviewedAnnotationAPI.markStartReview(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        def json = JSON.parse(result.data)
        def idReviewAnnotation = json.reviewedannotation.id
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.removeReviewAnnotation(annotation.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)


        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 0
        result = ReviewedAnnotationAPI.undo()
        assertEquals(200, result.code)

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 1
        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)


        result = ReviewedAnnotationAPI.redo()
        assertEquals(200, result.code)

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 0
        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

























    void testAddReviewAndUpdateGeometry() {
        ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
        UserAnnotation annotation = BasicInstance.createUserAnnotation(image.project,image)

        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        String newLocation = "POLYGON ((19830 21680, 21070 21600, 20470 20740, 19830 21680))"
        json.reviewedannotation.location = newLocation

        result = ReviewedAnnotationAPI.update(json.reviewedannotation.id,json.reviewedannotation.toString(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        println result.data
        assert JSON.parse(result.data).reviewedannotation.location.trim().equals("POLYGON ((19830 21680, 21070 21600, 20470 20740, 19830 21680))")
    }

    void testaddConflictReview() {
        ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
        UserAnnotation annotation = BasicInstance.createUserAnnotation(image.project,image)

        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(AlreadyExistException.CODE, result.code)
    }


    void testReviewAllUserLayer() {
        ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
        UserAnnotation annotation = BasicInstance.createUserAnnotation(image.project,image)
        List<Long> users = [annotation.user.id, SecUser.findByUsername(Infos.ANOTHERLOGIN).id]

        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)

        result =  ReviewedAnnotationAPI.addReviewAll(image.id,users,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert json.size()==1
    }

    void testReviewAllJobLayer() {
        ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
        UserJob userJob = BasicInstance.createUserJob(image.project)
        AlgoAnnotation annotation = BasicInstance.createAlgoAnnotation(userJob.job,userJob)
        annotation.image = image
        annotation.project = image.project
        BasicInstance.saveDomain(annotation)
        List<Long> users = [annotation.user.id, SecUser.findByUsername(Infos.ANOTHERLOGIN).id]

        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)

        result =  ReviewedAnnotationAPI.addReviewAll(image.id,users,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert json.size()==1
    }


    void testAnnotationReviewedCounterForAnnotationAlgo() {
        ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
        UserJob userJob = BasicInstance.createUserJob(image.project)
        AlgoAnnotation annotation = BasicInstance.createAlgoAnnotation(userJob.job,userJob)
        annotation.image = image
        BasicInstance.checkDomain(annotation)
        BasicInstance.saveDomain(annotation)

        image.refresh()
        image.project.refresh()

        assert annotation.countReviewedAnnotations==0
        assert image.countImageReviewedAnnotations==0
        int nbreRevAnnotationProject = image.project.countReviewedAnnotations

        ReviewedAnnotation review = BasicInstance.getBasicReviewedAnnotationNotExist()
        review.image = annotation.image
        review.project = annotation.project
        review.putParentAnnotation(annotation)
        BasicInstance.checkDomain(review)
        BasicInstance.saveDomain(review)

        annotation.refresh()
        image.refresh()
        image.project.refresh()

        assert annotation.countReviewedAnnotations==1
        assert image.countImageReviewedAnnotations==1
//        assert image.project.countReviewedAnnotations==nbreRevAnnotationProject+1

        review.delete(flush: true)

        annotation.refresh()
        image.refresh()
        image.project.refresh()

        assert annotation.countReviewedAnnotations==0
        assert image.countImageReviewedAnnotations==0
        assert image.project.countReviewedAnnotations==nbreRevAnnotationProject
    }

    void testAnnotationReviewedCounterForAnnotationUser() {
        ImageInstance image = BasicInstance.createImageInstance(BasicInstance.createOrGetBasicProject())
        UserJob userJob = BasicInstance.createUserJob(image.project)
        UserAnnotation annotation = BasicInstance.createUserAnnotation(image.project,image)
        assert annotation.countReviewedAnnotations==0
        assert image.countImageReviewedAnnotations==0
        image.project.refresh()
        int nbreRevAnnotationProject = image.project.countReviewedAnnotations

        ReviewedAnnotation review = BasicInstance.getBasicReviewedAnnotationNotExist()
        review.image = annotation.image
        review.project = annotation.project
        review.putParentAnnotation(annotation)
        BasicInstance.checkDomain(review)
        BasicInstance.saveDomain(review)

        annotation.refresh()
        image.refresh()
        image.project.refresh()

        assert annotation.countReviewedAnnotations==1
        assert image.countImageReviewedAnnotations==1
        assert image.project.countReviewedAnnotations==nbreRevAnnotationProject+1

        review.delete(flush: true)

        annotation.refresh()
        image.refresh()
        image.project.refresh()

        assert annotation.countReviewedAnnotations==0
        assert image.countImageReviewedAnnotations==0
        assert image.project.countReviewedAnnotations==nbreRevAnnotationProject
    }

}
