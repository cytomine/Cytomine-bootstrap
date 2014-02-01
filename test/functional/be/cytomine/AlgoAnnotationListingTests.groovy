package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AlgoAnnotationAPI
import be.cytomine.test.http.AnnotationDomainAPI
import be.cytomine.test.http.DomainAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class AlgoAnnotationListingTests {
    void testListAlgoAnnotationWithCredential() {
        BasicInstanceBuilder.getAlgoAnnotation()
        def result = AlgoAnnotationAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListAlgoAnnotationByProjecImageAndUsertWithCredential() {
        AlgoAnnotation annotation = BasicInstanceBuilder.getAlgoAnnotation()
        def result = AlgoAnnotationAPI.listByProject(annotation.project.id, annotation.user.id, annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = AlgoAnnotationAPI.listByProject(-99, annotation.user.id, annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListAlgoAnnotationByImageAndUser() {
        AlgoAnnotation annotation = BasicInstanceBuilder.getAlgoAnnotation()
        AlgoAnnotation annotationWith2Term = BasicInstanceBuilder.getAlgoAnnotation()
        AlgoAnnotationTerm aat = BasicInstanceBuilder.getAlgoAnnotationTerm(annotationWith2Term.user.job,annotationWith2Term,annotationWith2Term.user)


        def result = AlgoAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray


        //very small bbox, hight annotation number
        //force for each kmeans level (1,2,3)
        String bbox = "1,1,100,100"
        result = AlgoAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, bbox, true,null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        result = AlgoAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, bbox, true,1,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        result = AlgoAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, bbox, true,2,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        result = AlgoAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, bbox, true,3,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = AlgoAnnotationAPI.listByImageAndUser(-99, annotation.user.id, bbox, false,null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
        result = AlgoAnnotationAPI.listByImageAndUser(annotation.image.id, -99, bbox, false,null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }



    void testListAlgoAnnotationByProjectAndTermAndUserWithCredential() {
        AlgoAnnotationTerm annotationTerm = BasicInstanceBuilder.getAlgoAnnotationTerm(false)
        def result = AlgoAnnotationAPI.listByProjectAndTerm(annotationTerm.retrieveAnnotationDomain().project.id, annotationTerm.term.id, annotationTerm.retrieveAnnotationDomain().user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        //assert json.collection instanceof JSONArray
        result = AlgoAnnotationAPI.listByProjectAndTerm(-99, annotationTerm.term.id, annotationTerm.retrieveAnnotationDomain().user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
        json = JSON.parse(result.data)
    }

    void testListAlgoAnnotationByProjectAndTermAndUserWithAllProjectImage() {
        AlgoAnnotationTerm annotationTerm = BasicInstanceBuilder.getAlgoAnnotationTerm(true)
        def result = AlgoAnnotationAPI.listByProjectAndTerm(
                annotationTerm.retrieveAnnotationDomain().project.id,
                annotationTerm.term.id,
                ImageInstance.findAllByProject(annotationTerm.retrieveAnnotationDomain().project),
                annotationTerm.retrieveAnnotationDomain().user.id,
                Infos.GOODLOGIN,
                Infos.GOODPASSWORD
        )
        assert 200 == result.code
        def json = JSON.parse(result.data)
    }

    void testListAlgoAnnotationByProjectAndTermWithUserNullWithCredential() {
        AlgoAnnotationTerm annotationTerm = BasicInstanceBuilder.getAlgoAnnotationTerm(true)
        def result = AlgoAnnotationAPI.listByProjectAndTerm(annotationTerm.retrieveAnnotationDomain().project.id, annotationTerm.term.id, -1, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
        result = AlgoAnnotationAPI.listByProjectAndTerm(annotationTerm.retrieveAnnotationDomain().project.id, -99, -1, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 404 == result.code
    }

    void testListAlgoAnnotationByProjectAndTermAndUserAndImageWithCredential() {
        AlgoAnnotationTerm annotationTerm = BasicInstanceBuilder.getAlgoAnnotationTerm(false)
        Infos.addUserRight(Infos.GOODLOGIN,annotationTerm.retrieveAnnotationDomain().project)
        def result = AlgoAnnotationAPI.listByProjectAndTerm(annotationTerm.retrieveAnnotationDomain().project.id, annotationTerm.term.id, annotationTerm.retrieveAnnotationDomain().image.id,annotationTerm.retrieveAnnotationDomain().user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        //assert json.collection instanceof JSONArray
    }

    void testListAlgoAnnotationyProjectAndUsersWithCredential() {
        AlgoAnnotation annotation = BasicInstanceBuilder.getAlgoAnnotation()
        def result = AlgoAnnotationAPI.listByProjectAndUsers(annotation.project.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
    }

    void testListingAlgoAnnotationWithoutTermAlgo() {
        //create annotation without term
        UserJob userJob = BasicInstanceBuilder.getUserJob()
        User user = User.findByUsername(Infos.GOODLOGIN)
        Project project = BasicInstanceBuilder.getProjectNotExist()
        Ontology ontology = BasicInstanceBuilder.getOntology()
        project.ontology = ontology
        project.save(flush: true)

        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.project = project
        image.save(flush: true)

        AlgoAnnotation annotationWithoutTerm = BasicInstanceBuilder.getAlgoAnnotationNotExist()
        annotationWithoutTerm.project = project
        annotationWithoutTerm.image = image
        annotationWithoutTerm.user = userJob
        assert annotationWithoutTerm.save(flush: true)

        AlgoAnnotationTerm at = BasicInstanceBuilder.getAlgoAnnotationTermNotExistForAlgoAnnotation()
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
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(annotationWithoutTerm.id,json)
        assert !DomainAPI.containsInJSONList(annotationWithTerm.id,json)

        //list annotation without term with this user
        result = AnnotationDomainAPI.listByProjectAndUsersWithoutTerm(project.id, userJob.id, image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(annotationWithoutTerm.id,json)
        assert !DomainAPI.containsInJSONList(annotationWithTerm.id,json)

        //all images
        result = AnnotationDomainAPI.listByProjectAndUsersWithoutTerm(project.id, userJob.id, null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(annotationWithoutTerm.id,json)
        assert !DomainAPI.containsInJSONList(annotationWithTerm.id,json)
    }

    void testListingAlgoAnnotationWithSeveralTermAlgo() {
        //create annotation without term
        UserJob userJob = BasicInstanceBuilder.getUserJob()
        User user = User.findByUsername(Infos.GOODLOGIN)
        Project project = BasicInstanceBuilder.getProjectNotExist()
        Ontology ontology = BasicInstanceBuilder.getOntology()
        project.ontology = ontology
        project.save(flush: true)

        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.project = project
        image.save(flush: true)

        //annotation with no multiple term
        AlgoAnnotation annotationWithNoTerm = BasicInstanceBuilder.getAlgoAnnotationNotExist()
        annotationWithNoTerm.project = project
        annotationWithNoTerm.image = image
        annotationWithNoTerm.user = userJob
        assert annotationWithNoTerm.save(flush: true)

        //annotation with multiple term
        AlgoAnnotationTerm at = BasicInstanceBuilder.getAlgoAnnotationTermNotExistForAlgoAnnotation()
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
        AlgoAnnotationTerm at2 = BasicInstanceBuilder.getAlgoAnnotationTermNotExistForAlgoAnnotation()
        at2.term.ontology = ontology
        at2.project = project
        assert at2.term.save(flush: true)
        at2.userJob = userJob
        at2.setAnnotation(annotationWithMultipleTerm)
        at2.save(flush: true)

        //list annotation without term with this user
        def result = AlgoAnnotationAPI.listByProjectAndUsersSeveralTerm(project.id, userJob.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert !DomainAPI.containsInJSONList(annotationWithNoTerm.id,json)
        assert DomainAPI.containsInJSONList(annotationWithMultipleTerm.id,json)

        //list annotation without term with this user
        result = AnnotationDomainAPI.listByProjectAndUsersSeveralTerm(project.id, userJob.id, image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert !DomainAPI.containsInJSONList(annotationWithNoTerm.id,json)
        assert DomainAPI.containsInJSONList(annotationWithMultipleTerm.id,json)

        //all images
        result = AnnotationDomainAPI.listByProjectAndUsersSeveralTerm(project.id, userJob.id, null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert !DomainAPI.containsInJSONList(annotationWithNoTerm.id,json)
        assert DomainAPI.containsInJSONList(annotationWithMultipleTerm.id,json)
    }


}
