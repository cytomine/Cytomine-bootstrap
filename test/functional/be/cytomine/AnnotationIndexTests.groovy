package be.cytomine

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationDomainAPI
import be.cytomine.test.http.AnnotationIndexAPI
import be.cytomine.test.http.ReviewedAnnotationAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class AnnotationIndexTests {

    static String USER1 = "AnnotationIndexTestsUSER1"
    static String USER2 = "AnnotationIndexTestsUSER2"

    static String PASSWORD = "PASSWORD"

    static String USERJOB1 = "AnnotationIndexTestsUSERJOB1"
    static String USERJOB2 = "AnnotationIndexTestsUSERJOB2"

    def testIndexWithImageNotExist() {
        def result = AnnotationIndexAPI.listByImage(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    def testIndexUserAnnotation() {

        //create project, with 2 users
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        User user1 = BasicInstanceBuilder.getUser(USER1,PASSWORD)
        User user2 = BasicInstanceBuilder.getUser(USER2,PASSWORD)
        Infos.addUserRight(user1.username,project)
        Infos.addUserRight(user2.username,project)

        checkAnnotationIndex(project,user1,user2)
    }

    def testIndexAlgoAnnotation() {
        //create project, with 2 users
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        UserJob user1 = BasicInstanceBuilder.getUserJob(USERJOB1,PASSWORD)
        UserJob user2 = BasicInstanceBuilder.getUserJob(USERJOB2,PASSWORD)
        Infos.addUserRight(user1.user.username,project)
        Infos.addUserRight(user2.user.username,project)
        Infos.printRight(project)

        checkAnnotationIndex(project,user1,user2)
    }

    def testIndexUserReviewedAnnotation() {
        //create project, with 2 users
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        User user1 = BasicInstanceBuilder.getUser(USER1,PASSWORD)
        User user2 = BasicInstanceBuilder.getUser(USER2,PASSWORD)
        Infos.addUserRight(user1.username,project)
        Infos.addUserRight(user2.username,project)

        checkAnnotationIndexReviewed(project,user1,user2)
    }

    def testIndexAlgoReviewedAnnotation() {
        //create project, with 2 users
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        UserJob user1 = BasicInstanceBuilder.getUserJob(USERJOB1,PASSWORD)
        UserJob user2 = BasicInstanceBuilder.getUserJob(USERJOB2,PASSWORD)
        Infos.addUserRight(user1.user.username,project)
        Infos.addUserRight(user2.user.username,project)

        checkAnnotationIndexReviewed(project,user1,user2)
    }

    def testIndexAlgoReviewedAnnotationWithoutannotationIndex() {
        //create project, with 2 users
        //for bug fix: see if annotation index row is created if user/image is not created and user do a review
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        UserJob user1 = BasicInstanceBuilder.getUserJob(USERJOB1,PASSWORD)
        UserJob user2 = BasicInstanceBuilder.getUserJob(USERJOB2,PASSWORD)
        Infos.addUserRight(user1.user.username,project)
        Infos.addUserRight(user2.user.username,project)

        checkAnnotationIndexReviewedWithoutAnnotationIndex(project,user1,user2)
    }


    def testIndexUserAnnotationUpdate() {
       //create image
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        User user1 = BasicInstanceBuilder.getUser(USER1,PASSWORD)
        User user2 = BasicInstanceBuilder.getUser(USER2,PASSWORD)
        Infos.addUserRight(user1.username,project)
        Infos.addUserRight(user2.username,project)

        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)

        //list index, check if 0
        def result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data).collection
        assert json.size()==0
        assert getCountAnnotationValue(json,user1.id, false)==0
        assert getCountAnnotationValue(json,user1.id, true)==0
        assert getCountAnnotationValue(json,user2.id, false)==0
        assert getCountAnnotationValue(json,user2.id, true)==0

        //add annotation by user 1
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project,image)
        result = AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(), user1.username, PASSWORD)
        assert 200 == result.code
        int idAnnotation = result.data.id

        //list index, check if 1 for user and 0 for other
        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert json.size()==1
        assert getCountAnnotationValue(json,user1.id, false)==1
        assert getCountAnnotationValue(json,user1.id, true)==0
        assert getCountAnnotationValue(json,user2.id, false)==0
        assert getCountAnnotationValue(json,user2.id, true)==0

        def annotation = UserAnnotation.read(idAnnotation)
        annotation.user = user2
        BasicInstanceBuilder.saveDomain(annotation)

        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert getCountAnnotationValue(json,user1.id, false)==0
        assert getCountAnnotationValue(json,user1.id, true)==0
        assert getCountAnnotationValue(json,user2.id, false)==1
        assert getCountAnnotationValue(json,user2.id, true)==0

    }



    private checkAnnotationIndex(Project project, SecUser user1, SecUser user2) {
        //create image
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)

        //list index, check if 0
        def result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data).collection
        assert json.size()==0
        assert getCountAnnotationValue(json,user1.id, false)==0
        assert getCountAnnotationValue(json,user1.id, true)==0
        assert getCountAnnotationValue(json,user2.id, false)==0
        assert getCountAnnotationValue(json,user2.id, true)==0

        //add annotation by user 1
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project,image)
        result = AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(), user1.username, PASSWORD)
        assert 200 == result.code
        int idAnnotation = result.data.id

        //list index, check if 1 for user and 0 for other
        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert json.size()==1
        assert getCountAnnotationValue(json,user1.id, false)==1
        assert getCountAnnotationValue(json,user1.id, true)==0
        assert getCountAnnotationValue(json,user2.id, false)==0
        assert getCountAnnotationValue(json,user2.id, true)==0


        //add annotation by user 1
        annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project,image)
        result = AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(), user1.username, PASSWORD)
        assert 200 == result.code

        //add annotation by user 2
        annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project,image)
        result = AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(), user2.username, PASSWORD)
        assert 200 == result.code

        //list index, check if 2 for user and 1 for other
        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert json.size()==2
        assert getCountAnnotationValue(json,user1.id, false)==2
        assert getCountAnnotationValue(json,user1.id, true)==0
        assert getCountAnnotationValue(json,user2.id, false)==1
        assert getCountAnnotationValue(json,user2.id, true)==0

        //remove annotation for user 1
        result = AnnotationDomainAPI.delete(idAnnotation, user1.username, PASSWORD)
        assert 200 == result.code


        //list index, check if 1 for user and 1 for other
        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert json.size()==2
        assert getCountAnnotationValue(json,user1.id, false)==1
        assert getCountAnnotationValue(json,user1.id, true)==0
        assert getCountAnnotationValue(json,user2.id, false)==1
        assert getCountAnnotationValue(json,user2.id, true)==0
    }


    private checkAnnotationIndexReviewed(Project project, SecUser user1, SecUser user2) {
        //create image
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        def result = ReviewedAnnotationAPI.markStartReview(image.id,user1.username, PASSWORD)
        assert 200 == result.code

        //list index, check if 0
        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data).collection
        assert json.size()==0
        assert getCountAnnotationValue(json,user1.id, false)==0
        assert getCountAnnotationValue(json,user1.id, true)==0

        //add annotation by user 1
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project,image)
        result = AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(), user1.username, PASSWORD)
        assert 200 == result.code
        int idAnnotation = result.data.id
        result = ReviewedAnnotationAPI.addReviewAnnotation(idAnnotation,user1.username, PASSWORD)
        assert 200 == result.code

        //list index, check if 1 for user and 0 for other
        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection


        assert json.size()==1
        assert getCountAnnotationValue(json,user1.id, false)==1
        assert getCountAnnotationValue(json,user1.id, true)==1


        //add annotation by user 1
        annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project,image)
        result = AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(), user1.username, PASSWORD)
        assert 200 == result.code
        result = ReviewedAnnotationAPI.addReviewAnnotation(result.data.id,user1.username, PASSWORD)
        assert 200 == result.code

        //add annotation by user 1
        annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project,image)
        result = AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(), user1.username, PASSWORD)
        assert 200 == result.code


        //list index, check if 2 for user and 1 for other
        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert json.size()==1
        assert getCountAnnotationValue(json,user1.id, false)==3
        assert getCountAnnotationValue(json,user1.id, true)==2

        //remove annotation for user 1
        result = ReviewedAnnotationAPI.removeReviewAnnotation(idAnnotation,user1.username, PASSWORD)
        assert 200 == result.code

        //list index, check if 1 for user and 1 for other
        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection
        assert json.size()==1
        assert getCountAnnotationValue(json,user1.id, false)==3
        assert getCountAnnotationValue(json,user1.id, true)==1
    }


    private checkAnnotationIndexReviewedWithoutAnnotationIndex(Project project, SecUser user1, SecUser user2) {
        //create image
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        def result = ReviewedAnnotationAPI.markStartReview(image.id,user1.username, PASSWORD)
        assert 200 == result.code

        //list index, check if 0
        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data).collection
        assert json.size()==0
        assert getCountAnnotationValue(json,user1.id, false)==0
        assert getCountAnnotationValue(json,user1.id, true)==0

        //add annotation by user 1
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project,image)
        result = AnnotationDomainAPI.create(annotationToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idAnnotation = result.data.id
        result = ReviewedAnnotationAPI.addReviewAnnotation(idAnnotation,user1.username, PASSWORD)
        assert 200 == result.code

        //list index, check if 1 for user and 0 for other
        result = AnnotationIndexAPI.listByImage(image.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data).collection


        assert json.size()==2
        assert getCountAnnotationValue(json,user1.id, false)==0
        assert getCountAnnotationValue(json,user1.id, true)==1
        assert getCountAnnotationValue(json,User.findByUsername(Infos.SUPERADMINLOGIN).id, false)==1
        assert getCountAnnotationValue(json,User.findByUsername(Infos.SUPERADMINLOGIN).id, true)==0

    }



    private Long getCountAnnotationValue(def json, Long idUser, boolean reviewed) {
        println json

        //check that a user is not twice or missing in list
        def users = json.collect{it.user}.unique()
        assert users.size()==json.size()

        for(int i=0;i<json.size();i++) {
            def item = json.get(i)
            if(item.user==idUser)  {
                return (reviewed ? item.countReviewedAnnotation : item.countAnnotation)
            }
        }
        return 0
    }
}
