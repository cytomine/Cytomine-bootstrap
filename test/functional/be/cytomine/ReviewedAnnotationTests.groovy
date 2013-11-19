package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.UserAnnotationAPI
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.test.http.ReviewedAnnotationAPI
import be.cytomine.ontology.ReviewedAnnotation

import be.cytomine.security.UserJob
import be.cytomine.ontology.AlgoAnnotation

import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.security.SecUser
import be.cytomine.utils.UpdateData

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class ReviewedAnnotationTests  {

    void testGetReviewedAnnotation() {
        def annotation = BasicInstanceBuilder.getReviewedAnnotation()
        def result = ReviewedAnnotationAPI.show(annotation.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testGetReviewedAnnotationNotExist() {
        def result = ReviewedAnnotationAPI.show(-99, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 404 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testCountReviewedAnnotationWithCredential() {
        def result = ReviewedAnnotationAPI.countByUser(BasicInstanceBuilder.getUser1().id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.total >= 0
    }

    void testAddReviewedAnnotationCorrect() {
        def annotationToAdd = BasicInstanceBuilder.getReviewedAnnotationNotExist()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.term = BasicInstanceBuilder.getTerm().id
        println "json.encodeAsJSON()="+json.encodeAsJSON()
        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        int idAnnotation = result.data.id

        result = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ReviewedAnnotationAPI.undo()
        assert 200 == result.code

        result = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = ReviewedAnnotationAPI.redo()
        assert 200 == result.code

        result = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }


//    void testAddReviewedAnnotationCorrectWithoutTerm() {
//        def annotationToAdd = BasicInstanceBuilder.getReviewedAnnotationNotExist()
//        def json = JSON.parse(annotationToAdd.encodeAsJSON())
//        json.term = []
//
//        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assert 400 == result.code
//    }

    void testAddReviewedAnnotationCorrectWithBadProject() {
        def annotationToAdd = BasicInstanceBuilder.getReviewedAnnotationNotExist()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.project = null

        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testAddReviewedAnnotationCorrectWithBadImage() {
        def annotationToAdd = BasicInstanceBuilder.getReviewedAnnotationNotExist()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.image = null

        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

//    void testAddReviewedAnnotationCorrectWithBadParent() {
//        def annotationToAdd = BasicInstanceBuilder.getReviewedAnnotationNotExist()
//        def json = JSON.parse(annotationToAdd.encodeAsJSON())
//        json.annotationIdent = null
//
//        def result = ReviewedAnnotationAPI.create(json.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assert 400 == result.code
//    }


    void testEditReviewedAnnotation() {
        def reviewed = BasicInstanceBuilder.getReviewedAnnotation()
        def data = UpdateData.createUpdateSet(
                reviewed,
                [location: [new WKTReader().read("POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"),new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))")]]
        )

        def result = ReviewedAnnotationAPI.update(reviewed.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAnnotation = json.reviewedannotation.id

        def showResult = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = ReviewedAnnotationAPI.undo()
        assert 200==showResult.code
        showResult = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

        showResult = ReviewedAnnotationAPI.redo()
        assert 200==showResult.code
        showResult = ReviewedAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
    }


//    void testDeleteReviewedAnnotation() {
//        def annotationToDelete = BasicInstanceBuilder.getReviewedAnnotationNotExist()
//        assert annotationToDelete.addToTerm(BasicInstanceBuilder.getTerm()).save(flush: true)  != null
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
//        assert 200 == result.code
//
//        def showResult = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assert 404 == showResult.code
//
//        result = ReviewedAnnotationAPI.undo()
//        assert 200 == result.code
//
//        result = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assert 200 == result.code
//
//        result = ReviewedAnnotationAPI.redo()
//        assert 200 == result.code
//
//        result = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assert 404 == result.code
//    }
//
//    void testDeleteReviewedAnnotationWithTerm() {
//        def annotationToDelete = BasicInstanceBuilder.getReviewedAnnotationNotExist()
//        assert annotationToDelete.save(flush: true)  != null
//        annotationToDelete.addToTerm(BasicInstanceBuilder.getTerm())
//        //annotationToDelete.save(flush: true)
//        def id = annotationToDelete.id
//        println annotationToDelete.encodeAsJSON()
//        def result = ReviewedAnnotationAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assert 200 == result.code
//
//        def showResult = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assert 404 == showResult.code
//
//        result = ReviewedAnnotationAPI.undo()
//        assert 200 == result.code
//
//        result = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assert 200 == result.code
//
//        result = ReviewedAnnotationAPI.redo()
//        assert 200 == result.code
//
//        result = ReviewedAnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assert 404 == result.code
//    }

    void testDeleteReviewedAnnotationNotExist() {
        def result = ReviewedAnnotationAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }



//    void testAddReviewedAnnotationForAllJobData() {
//        Job job = BasicInstanceBuilder.getJobNotExist()
//        BasicInstanceBuilder.checkDomain(job)
//        BasicInstanceBuilder.saveDomain(job)
//        BasicInstanceBuilder.createSoftwareProject(job.software,job.project)
//
//        UserJob userJob = BasicInstanceBuilder.getUserJobNotExist()
//        userJob.job = job
//        userJob.user = BasicInstanceBuilder.getNewUser()
//        BasicInstanceBuilder.checkDomain(userJob)
//        BasicInstanceBuilder.saveDomain(userJob)
//
//        //add algo-annotation for this job
//        AlgoAnnotation a1 = createAlgoAnnotation(job, userJob)
//
//        //add algo-annotation-term for this job
//        AlgoAnnotationTerm at1 = createAlgoAnnotationTerm(job, a1, userJob)
//
//        //add user-annotation for this job
//        UserAnnotation a2 = getUserAnnotationNotExist(true,job)
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
//        assert 200 == result.code
//        def json = JSON.parse(result.data)
//        assert json.collection instanceof JSONArray
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
          ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)

          //check image attributes
          def result = ImageInstanceAPI.show(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
          def json = JSON.parse(result.data)
          assert json instanceof JSONObject
          assert json.id == image.id
          assert json.isNull('reviewStart')
          assert json.isNull('reviewStop')
          assert json.isNull('reviewUser')

          //mark start review + check attr
          result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
          json = JSON.parse(result.data)
          assert json instanceof JSONObject
          assert json.imageinstance.id == image.id
          assert !json.imageinstance.isNull('reviewStart')
          assert json.imageinstance.isNull('reviewStop')
          assert !json.imageinstance.isNull('reviewUser')

          //mark stop review + check attr
          result = ReviewedAnnotationAPI.markStopReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
          json = JSON.parse(result.data)
          assert json instanceof JSONObject
          assert json.imageinstance.id == image.id
          assert !json.imageinstance.isNull('reviewStart')
          assert !json.imageinstance.isNull('reviewStop')
          assert !json.imageinstance.isNull('reviewUser')
          assert Long.parseLong(json.imageinstance.reviewStart.toString())<Long.parseLong(json.imageinstance.reviewStop.toString())

      }

    void testStartReviewNotExist() {
        def result = ReviewedAnnotationAPI.markStartReview(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

      void testLockImageReviewing() {
          //check image lock, only review if image is mark as review star
          //create image
          ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)

          //add review
          UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)

          def result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert ConstraintException.CODE==result.code

          //mark start review + check attr
          result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code

          //add review
          result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code

          //mark stop review + check attr
          result = ReviewedAnnotationAPI.markStopReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code

          //create image
          ImageInstance image2 = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
          UserAnnotation annotation2 = BasicInstanceBuilder.getUserAnnotationNotExist(image2.project,image2,true)
          result = ReviewedAnnotationAPI.addReviewAnnotation(annotation2.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert ConstraintException.CODE==result.code

      }

    void testStopReviewCancel() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = ReviewedAnnotationAPI.markStopReview(image.id, true,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testStopReviewValidate() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = ReviewedAnnotationAPI.markStopReview(image.id, false,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testStopReviewInvalid() {
        def result = ReviewedAnnotationAPI.markStopReview(-99, true,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        ImageInstance image

        //validate image review by other user
        image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = ReviewedAnnotationAPI.markStopReview(image.id, false,Infos.ANOTHERLOGIN, Infos.ANOTHERPASSWORD)
        assert 400 == result.code
    }

     void testLockImageReviewingForOtherUser() {
          //create image
          ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)

          //mark start review + check attr
          def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code

          //add review with another login/user
          UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)
          result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.ANOTHERLOGIN, Infos.ANOTHERPASSWORD)
         assert ConstraintException.CODE==result.code

     }

      void testUnReviewing() {
          //review image => add review => check image is not reviewed
          //create image
          ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
          UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)

          //mark start review + check attr
          def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code

          result = ReviewedAnnotationAPI.markStopReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code

          result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code

          result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
          assert 200 == result.code
      }

    void testAddReviewForImageNotReviewed() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)
        def result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testAddReviewForUserNotReviewer() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)
        def result = ReviewedAnnotationAPI.markStartReview(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(), Infos.ANOTHERLOGIN, Infos.ANOTHERPASSWORD)
        assert 400 == result.code
    }

    void testAddReviewForAnnotationTerm() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)

        AnnotationTerm at = BasicInstanceBuilder.getAnnotationTermNotExist(annotation,true)

        def result = ReviewedAnnotationAPI.markStartReview(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert Long.parseLong(json.reviewedannotation.parentIdent.toString()) == annotation.id
        assert json.reviewedannotation.terms !=null
        assert json.reviewedannotation.terms.size()==1

        def idReviewAnnotation = json.reviewedannotation.id
        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 1
        result = ReviewedAnnotationAPI.undo()
        assert 200 == result.code

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 0
        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code


        result = ReviewedAnnotationAPI.redo()
        assert 200 == result.code

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 1
        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }


    void testAddReviewForAnnotationTermOtherTerm() {
         ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
         UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)

         AnnotationTerm at = BasicInstanceBuilder.getAnnotationTermNotExist(annotation,true)
         Term term =  BasicInstanceBuilder.getTermNotExist(at.term.ontology,true)

         def result = ReviewedAnnotationAPI.markStartReview(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code

         result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, [term.id], Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json instanceof JSONObject
         assert json.reviewedannotation.terms !=null
         assert json.reviewedannotation.terms.size()==1

         assert json.reviewedannotation.terms.contains(term.id.intValue())
         assert !json.reviewedannotation.terms.contains(at.term.id.intValue())
         def idReviewAnnotation = json.reviewedannotation.id
         result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code

     }

    void testAddReviewForAnnotationTermWIthBadOntology() {
         ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
         UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)

         AnnotationTerm at = BasicInstanceBuilder.getAnnotationTermNotExist(annotation,true)
         Term term =  BasicInstanceBuilder.getTermNotExist(at.term.ontology,true)

         def result = ReviewedAnnotationAPI.markStartReview(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 200 == result.code

         result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, [BasicInstanceBuilder.getTermNotExist(true).id], Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 400 == result.code


     }

    void testAddReviewForAlgoAnnotationTerm() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserJob user = BasicInstanceBuilder.getUserJob(image.project)
        AlgoAnnotation annotation = BasicInstanceBuilder.getAlgoAnnotationNotExist(user.job,user,true)
        annotation.image = image
        BasicInstanceBuilder.saveDomain(annotation)
        BasicInstanceBuilder.getAlgoAnnotationTerm(user.job,annotation,user)


        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert Long.parseLong(json.reviewedannotation.parentIdent.toString()) == annotation.id
        assert json.reviewedannotation.terms !=null
        assert json.reviewedannotation.terms.size()==1
    }

    void testRemoveReviewForAnnotationNotReviewed() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)
        def result = ReviewedAnnotationAPI.removeReviewAnnotation(annotation.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testRemoveReviewForUserNotReviewed() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)
        def result = ReviewedAnnotationAPI.markStartReview(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = ReviewedAnnotationAPI.removeReviewAnnotation(annotation.id,Infos.ANOTHERLOGIN, Infos.ANOTHERPASSWORD)
        assert 400 == result.code
    }

    void testRemoveReviewForAlgoAnnotationTerm() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserJob user = BasicInstanceBuilder.getUserJob(image.project)
        AlgoAnnotation annotation = BasicInstanceBuilder.getAlgoAnnotationNotExist(user.job,user,true)
        annotation.image = image
        BasicInstanceBuilder.saveDomain(annotation)
        BasicInstanceBuilder.getAlgoAnnotationTerm(user.job,annotation,user)


        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
//        def json = JSON.parse(result.data)
//        def idReviewAnnotation = json.reviewedannotation.i

        result = ReviewedAnnotationAPI.removeReviewAnnotation(annotation.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }


    void testRemoveReviewForAnnotationTerm() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)

        def result = ReviewedAnnotationAPI.markStartReview(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, annotation.termsId(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        def json = JSON.parse(result.data)
        def idReviewAnnotation = json.reviewedannotation.id
        assert 200 == result.code

        result = ReviewedAnnotationAPI.removeReviewAnnotation(annotation.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code


        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 0
        result = ReviewedAnnotationAPI.undo()
        assert 200 == result.code

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 1
        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code


        result = ReviewedAnnotationAPI.redo()
        assert 200 == result.code

        annotation.refresh()
        assert annotation.countReviewedAnnotations == 0
        result = ReviewedAnnotationAPI.show(idReviewAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testAddReviewAndUpdateGeometry() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)

        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        String newLocation = "POLYGON ((19830 21680, 21070 21600, 20470 20740, 19830 21680))"
        json.reviewedannotation.location = newLocation

        result = ReviewedAnnotationAPI.update(json.reviewedannotation.id,json.reviewedannotation.toString(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        println result.data
        assert JSON.parse(result.data).reviewedannotation.location.trim().equals("POLYGON ((19830 21680, 21070 21600, 20470 20740, 19830 21680))")
    }

    void testAddReviewDeleteParentAndReject() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)

        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        result = UserAnnotationAPI.delete(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ReviewedAnnotationAPI.removeReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json instanceof JSONObject

    }

    void testaddConflictReview() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)

        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ReviewedAnnotationAPI.addReviewAnnotation(annotation.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert AlreadyExistException.CODE==result.code
    }


    void testReviewAllUserLayerImageNotFound() {
        def result =  ReviewedAnnotationAPI.addReviewAll(-99,[BasicInstanceBuilder.user1.id],Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testReviewAllUserLayerImageNotReviewMode() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        def result =  ReviewedAnnotationAPI.addReviewAll(image.id,[BasicInstanceBuilder.user1.id],Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testReviewAllUserLayerUserNotReviewer() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        result =  ReviewedAnnotationAPI.addReviewAll(image.id,[BasicInstanceBuilder.user1.id],Infos.ANOTHERLOGIN, Infos.ANOTHERPASSWORD)
        assert 400 == result.code
    }


    void testReviewAllUserLayer() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)
        List<Long> users = [annotation.user.id, SecUser.findByUsername(Infos.ANOTHERLOGIN).id]

        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)

        result =  ReviewedAnnotationAPI.addReviewAll(image.id,users,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()==1
    }

    void testReviewAllJobLayer() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserJob userJob = BasicInstanceBuilder.getUserJob(image.project)
        AlgoAnnotation annotation = BasicInstanceBuilder.getAlgoAnnotationNotExist(userJob.job,userJob,true)
        annotation.image = image
        annotation.project = image.project
        BasicInstanceBuilder.saveDomain(annotation)
        List<Long> users = [annotation.user.id, SecUser.findByUsername(Infos.ANOTHERLOGIN).id]

        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)

        result =  ReviewedAnnotationAPI.addReviewAll(image.id,users,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()==1
    }


    void testUnReviewAllUserLayer() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)
        List<Long> users = [annotation.user.id, SecUser.findByUsername(Infos.ANOTHERLOGIN).id]

        def result = ReviewedAnnotationAPI.markStartReview(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        result =  ReviewedAnnotationAPI.addReviewAll(image.id,users,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result =  ReviewedAnnotationAPI.deleteReviewAll(image.id,users,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testUnReviewAllUserLayerImageNotFound() {
        def result =  ReviewedAnnotationAPI.deleteReviewAll(-99,[BasicInstanceBuilder.user1.id],Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }














    void testAnnotationReviewedCounterForAnnotationAlgo() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserJob userJob = BasicInstanceBuilder.getUserJob(image.project)
        AlgoAnnotation annotation = BasicInstanceBuilder.getAlgoAnnotationNotExist(userJob.job,userJob,true)
        annotation.image = image
        BasicInstanceBuilder.saveDomain(annotation)

        image.refresh()
        image.project.refresh()

        assert annotation.countReviewedAnnotations==0
        assert image.countImageReviewedAnnotations==0
        int nbreRevAnnotationProject = image.project.countReviewedAnnotations

        ReviewedAnnotation review = BasicInstanceBuilder.getReviewedAnnotationNotExist()
        review.image = annotation.image
        review.project = annotation.project
        review.putParentAnnotation(annotation)
        BasicInstanceBuilder.saveDomain(review)

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


        assert image.countImageReviewedAnnotations==0
        assert image.project.countReviewedAnnotations==nbreRevAnnotationProject
        assert annotation.countReviewedAnnotations==0
    }

    void testAnnotationReviewedCounterForAnnotationUser() {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(BasicInstanceBuilder.getProject(),true)
        UserJob userJob = BasicInstanceBuilder.getUserJob(image.project)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(image.project,image,true)
        assert annotation.countReviewedAnnotations==0
        assert image.countImageReviewedAnnotations==0
        image.project.refresh()
        int nbreRevAnnotationProject = image.project.countReviewedAnnotations

        ReviewedAnnotation review = BasicInstanceBuilder.getReviewedAnnotationNotExist()
        review.image = annotation.image
        review.project = annotation.project
        review.putParentAnnotation(annotation)
        BasicInstanceBuilder.saveDomain(review)

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
