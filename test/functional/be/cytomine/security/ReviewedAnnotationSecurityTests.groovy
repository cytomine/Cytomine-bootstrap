package be.cytomine.security

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos

import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON

import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.test.http.ReviewedAnnotationAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class ReviewedAnnotationSecurityTests extends SecurityTestsAbstract {

    void testAnnotationSecurityForCytomineAdmin() {
        //Get User 1
        User user = getUser1()

        //Get cytomine admin
        User admin = getUserAdmin()

        //Create project with user 1
        ImageInstance image = BasicInstance.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with cytomine admin
        ReviewedAnnotation annotation1 = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotation1.image = image
        annotation1.project = project
        def result = ReviewedAnnotationAPI.create(annotation1.encodeAsJSON(), SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assert 200 == result.code
        annotation1 = result.data

        //Add annotation 2 with user 1
        ReviewedAnnotation annotation2 = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotation2.image = image
        annotation2.project = project
        Infos.printRight(annotation2.project)
        result = ReviewedAnnotationAPI.create(annotation2.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        annotation2 = result.data

        //Get/List annotation with cytomine admin
        assert (200 == ReviewedAnnotationAPI.show(annotation2.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)
        result = ReviewedAnnotationAPI.listByProject(project.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assert 200 == result.code
        log.info "JSON.parse(result.data)="+JSON.parse(result.data)
        assert (true ==ReviewedAnnotationAPI.containsInJSONList(annotation2.id, JSON.parse(result.data)))

        //update annotation 2 with cytomine admin
        assert (200 == ReviewedAnnotationAPI.update(annotation2.id,annotation2.encodeAsJSON(), SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)

        //Delete annotation 2 with cytomine admin
        assert (200 == ReviewedAnnotationAPI.delete(annotation2.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)


    }

    void testAnnotationSecurityForProjectUserAndAnnotationCreator() {
        //Get User 1
        User user = getUser1()

        //Create project with user 1
        ImageInstance image = BasicInstance.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with user1
        ReviewedAnnotation annotation = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = ReviewedAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        annotation = result.data

        //Get/List annotation 1 with user 1
        assert (200 == ReviewedAnnotationAPI.show(annotation.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
        result = ReviewedAnnotationAPI.listByProject(project.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        assert (true ==ReviewedAnnotationAPI.containsInJSONList(annotation.id, JSON.parse(result.data)))

        //update annotation 1 with user 1
        annotation.refresh()
        assert (200 == ReviewedAnnotationAPI.update(annotation.id,annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)

        //Delete annotation 1 with user 1
        assert (200 == ReviewedAnnotationAPI.delete(annotation.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
    }

    void testAnnotationSecurityForProjectUser() {
        //Get User 1
        User user1 = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        ImageInstance image = BasicInstance.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add project right for user 2
        def resAddUser = ProjectAPI.addUserProject(project.id, user2.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Infos.printRight(project)
        assert 200 == resAddUser.code

        //Add annotation 1 with user 1
        ReviewedAnnotation annotation = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = ReviewedAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        annotation = result.data

        //Get/List annotation 1 with user 2
        assert (200 == ReviewedAnnotationAPI.show(annotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
        result = ReviewedAnnotationAPI.listByProject(project.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assert 200 == result.code
        assert (true ==ReviewedAnnotationAPI.containsInJSONList(annotation.id, JSON.parse(result.data)))

        //update annotation 1 with user 2
        assert (403 == ReviewedAnnotationAPI.update(annotation.id,annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)

        //Delete annotation 1 with user 2
        assert (403 == ReviewedAnnotationAPI.delete(annotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }


    void testAnnotationSecurityForUser() {
        //Get User 1
        User user1 = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        ImageInstance image = BasicInstance.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with user 1
        ReviewedAnnotation annotation = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = ReviewedAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        annotation = result.data

        //Get/List annotation 1 with user 2
        assert (403 == ReviewedAnnotationAPI.show(annotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
        result = ReviewedAnnotationAPI.listByProject(project.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assert (403 == result.code)

        //update annotation 1 with user 2
        assert (403 == ReviewedAnnotationAPI.update(annotation.id,annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)

        //Delete annotation 1 with user 2
        assert (403 == ReviewedAnnotationAPI.delete(annotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }



    void testAnnotationSecurityForAnonymous() {
        //Get User 1
        User user1 = getUser1()

        //Create project with user 1
        ImageInstance image = BasicInstance.buildBasicImage(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Project project = image.project

        //Add annotation 1 with user 1
        ReviewedAnnotation annotation = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        def result = ReviewedAnnotationAPI.create(annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        annotation = result.data

        //Get/List annotation 1 with user 2
        assert (401 == ReviewedAnnotationAPI.show(annotation.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assert (401 == ReviewedAnnotationAPI.listByProject(project.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assert (401 == ReviewedAnnotationAPI.update(annotation.id,annotation.encodeAsJSON(), SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assert (401 == ReviewedAnnotationAPI.delete(annotation.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
    }

}
