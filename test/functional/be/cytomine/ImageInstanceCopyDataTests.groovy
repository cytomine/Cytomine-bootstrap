package be.cytomine

import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Property
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.TaskAPI
import be.cytomine.utils.Description
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import static org.springframework.security.acls.domain.BasePermission.*
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 9:11
 * To change this template use File | Settings | File Templates.
 */
class ImageInstanceCopyDataTests {

    void testGetLayers() {
        def data = initData()

        //get layers for images 3
        def response = ImageInstanceAPI.sameImageData(data.image3.id,data.user2.username,"password")
        assert 200 == response.code
        def json = JSON.parse(response.data)

        json = json.collection

        //check if image1 - user 1
        assert checkIfExist(json,data.image1.id,data.user1.id)

        //check if image1 - user 2
        assert checkIfExist(json,data.image1.id,data.user2.id)

        //check if NOT image2 - user 1 (user 1 not in project 2)
        assert !checkIfExist(json,data.image2.id,data.user1.id)

        //check if image2 - user 1
        assert checkIfExist(json,data.image2.id,data.user2.id)

        //check if NOT image3 - user 1 (exlude current image)
        assert !checkIfExist(json,data.image3.id,data.user1.id)

        //check if NOT image3 - user 2 (exlude current image)
        assert !checkIfExist(json,data.image3.id,data.user2.id)

        //check if number of layer = 3
        assert json.size()==3
    }


    void testGetLayersProjectSelection() {
        def data = initData()

        //get layers for images 3
        def response = ImageInstanceAPI.sameImageData(data.image3.id,data.user2.username,"password",data.image2.project.id)
        assert 200 == response.code
        def json = JSON.parse(response.data)

        json = json.collection

        //check if NOT image1 - user 1 (not project 2)
        assert !checkIfExist(json,data.image1.id,data.user1.id)

        //check if NOT image1 - user 2  (not project 2)
        assert !checkIfExist(json,data.image1.id,data.user2.id)

        //check if NOT image2 - user 1 (user 1 not in project 2)
        assert !checkIfExist(json,data.image2.id,data.user1.id)

        //check if image2 - user 1
        assert checkIfExist(json,data.image2.id,data.user2.id)

        //check if NOT image3 - user 1 (exlude current image)
        assert !checkIfExist(json,data.image3.id,data.user1.id)

        //check if NOT image3 - user 2 (exlude current image)
        assert !checkIfExist(json,data.image3.id,data.user2.id)
    }




    void testGetLayersWithOtherUser() {
           def data = initData()

           //get layers for images 3
           def response = ImageInstanceAPI.sameImageData(data.image3.id,data.user1.username,"password")
           assert 200 == response.code
           def json = JSON.parse(response.data)

           json = json.collection

           //check if image1 - user 1
           assert checkIfExist(json,data.image1.id,data.user1.id)

           //check if image1 - user 2
           assert checkIfExist(json,data.image1.id,data.user2.id)

           //check if NOT image2 - user 1 (user 1 not in project 2)
           assert !checkIfExist(json,data.image2.id,data.user1.id)

           //check if NOT image2 - user 2 (user 1 is not in project 2)
           assert !checkIfExist(json,data.image2.id,data.user2.id)

           //check if NOT image3 - user 1 (exlude current image)
           assert !checkIfExist(json,data.image3.id,data.user1.id)

           //check if NOT image3 - user 2 (exlude current image)
           assert !checkIfExist(json,data.image3.id,data.user2.id)

       }

    void testGetLayersImageNotExist() {
        def response = ImageInstanceAPI.sameImageData(-99,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 404 == response.code
    }

    void testGetLayersImageUnauthorized() {
        def data = initData()
        def response = ImageInstanceAPI.sameImageData(data.image3.id,data.user3.username,"password")
        assert 403 == response.code

        response = ImageInstanceAPI.copyImageData(data.image3.id,[[user:data.user1,image:data.image1],[user:data.user1,image:data.image2],[user:data.user2,image:data.image2]],null,data.user3.username,"password")
        assert 403 == response.code
    }

    void testCopyLayersFull() {
        def data = initData()

        //summary
        //Annotation 1: project 1 (image1) with description  with property and term from user 1
        //Annotation 2: project 1 (image1) and term  from user 2
        //Annotation 3: project 2 (image2) term  that must be skipped (not same ontology) from user 2


        //Copy image 1 - user1, image 2 - user 1 (not in layers for this project), iamge 2 - user 2
        def response = ImageInstanceAPI.copyImageData(data.image3.id,[[user:data.user1,image:data.image1],[user:data.user1,image:data.image2],[user:data.user2,image:data.image2]],null,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == response.code

        //check if there are 2 annotations on image3 (1 from image1-user1 and 1 from image 2 - user 1)
        assert 2 == UserAnnotation.findAllByImage(data.image3).size()


        def annotation1 = UserAnnotation.findByUserAndImage(data.user1,data.image3)
        def annotation2 = UserAnnotation.findByUserAndImage(data.user2,data.image3)

        assert annotation1
        assert annotation1.termsId().contains(data.term1.id)
        assert Description.findByDomainIdent(annotation1.id)
        assert Description.findByDomainIdent(annotation1.id).data == Description.findByDomainIdent(data.annotation1.id).data
        assert Property.findByDomainIdent(annotation1.id)
        assert Property.findByDomainIdent(annotation1.id).value == Property.findByDomainIdent(annotation1.id).value
        assert Property.findByDomainIdent(annotation1.id).key == Property.findByDomainIdent(annotation1.id).key

        assert annotation2
        assert annotation2.termsId().isEmpty()
        assert !Description.findByDomainIdent(annotation2.id)
        assert !Property.findByDomainIdent(annotation2.id)
    }

    void testCopyLayersFullGiveMe() {
        def data = initData()

        //summary
        //Annotation 1: project 1 (image1) with description  with property and term from user 1
        //Annotation 2: project 1 (image1) and term  from user 2
        //Annotation 3: project 2 (image2) term  that must be skipped (not same ontology) from user 2


        //Copy image 1 - user1, image 2 - user 1 (not in layers for this project), iamge 2 - user 2
        def response = ImageInstanceAPI.copyImageData(data.image3.id,true,[[user:data.user1,image:data.image1],[user:data.user1,image:data.image2],[user:data.user2,image:data.image2]],null,data.user1.username,"password")
        assert 200 == response.code

        //check if there are 2 annotations on image3 (1 from image1-user1 and 1 from image 2 - user 1)
        assert 2 == UserAnnotation.findAllByImage(data.image3).size()


        def annotation1 = UserAnnotation.findByUserAndImage(data.user1,data.image3)
        def annotation2 = UserAnnotation.findByUserAndImage(data.user1,data.image3)   //not on layer user 2 ==> giveMe = true

    }



    void testCopyLayersFullWithTask() {
        def data = initData()

        //summary
        //Annotation 1: project 1 (image1) with description  with property and term from user 1
        //Annotation 2: project 1 (image1) and term  from user 2
        //Annotation 3: project 2 (image2) term  that must be skipped (not same ontology) from user 2

        def result = TaskAPI.create(data.image3.project.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def jsonTask = JSON.parse(result.data)

        //Copy image 1 - user1, image 2 - user 1 (not in layers for this project), iamge 2 - user 2
        def response = ImageInstanceAPI.copyImageData(data.image3.id,[[user:data.user1,image:data.image1],[user:data.user1,image:data.image2],[user:data.user2,image:data.image2]],jsonTask.task.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == response.code

        //check if there are 2 annotations on image3 (1 from image1-user1 and 1 from image 2 - user 1)
        assert 2 == UserAnnotation.findAllByImage(data.image3).size()
    }


    void testCopyMetadata() {
        Project project =  BasicInstanceBuilder.getProjectNotExist(true)
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        Description description =  BasicInstanceBuilder.getDescriptionNotExist(image,true)
        Property property = BasicInstanceBuilder.getImageInstancePropertyNotExist(image,true)
        Property property2 = BasicInstanceBuilder.getImageInstancePropertyNotExist(image,true)

        Project project2 =  BasicInstanceBuilder.getProjectNotExist(true)
        ImageInstance image2 = BasicInstanceBuilder.getImageInstanceNotExist(project2,true)

        assert Description.countByDomainIdent(image2.id)==0
        assert Property.countByDomainIdent(image2.id)==0

        def response = ImageInstanceAPI.copyMetaData(image2.id,image.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == response.code


        assert Description.countByDomainIdent(image2.id)==1
        assert Property.countByDomainIdent(image2.id)==2
    }



    private boolean checkIfExist(def json, def idImage, def idUser) {
        boolean exist = false
        json.each {
            if(it.image==idImage && it.user==idUser) {
                exist = true
            }
        }
        return exist
    }

    private def initData() {
        def data = [:]
        data.user1 = BasicInstanceBuilder.getUserNotExist(true)
        data.user2 = BasicInstanceBuilder.getUserNotExist(true)
        data.user3 = BasicInstanceBuilder.getUserNotExist(true)
        data.ontology1 = BasicInstanceBuilder.getOntologyNotExist(true)
        data.ontology2 = BasicInstanceBuilder.getOntologyNotExist(true)

        data.project1 = BasicInstanceBuilder.getProjectNotExist(data.ontology1,true)
        data.project2 = BasicInstanceBuilder.getProjectNotExist(data.ontology1,true)
        data.project3 = BasicInstanceBuilder.getProjectNotExist(data.ontology1,true)

        Infos.addUserRight(data.user1.username,data.project1)
        Infos.addUserRight(data.user1.username,data.project3)

        Infos.addUserRight(data.user2.username,data.project1)
        Infos.addUserRight(data.user2.username,data.project2)
        Infos.addUserRight(data.user2.username,data.project3)

        Infos.addUserRight(data.user1,data.project3,[READ])

        data.image = BasicInstanceBuilder.getAbstractImageNotExist(true)
        BasicInstanceBuilder.getImageInstanceNotExist(data.project1,true)

        data.image1 = BasicInstanceBuilder.getImageInstanceNotExist(data.project1,false)
        data.image1.baseImage = data.image
        BasicInstanceBuilder.saveDomain(data.image1)

        data.image2 = BasicInstanceBuilder.getImageInstanceNotExist(data.project2,false)
        data.image2.baseImage = data.image
        BasicInstanceBuilder.saveDomain(data.image2)

        data.image3 = BasicInstanceBuilder.getImageInstanceNotExist(data.project3,false)
        data.image3.baseImage = data.image
        BasicInstanceBuilder.saveDomain(data.image3)

        data.term1 = BasicInstanceBuilder.getTermNotExist(data.ontology1,true)
        data.term2 = BasicInstanceBuilder.getTermNotExist(data.ontology2,true)

        //Annotation 1: project 1 (image1) with description  with property and term from user 1
        data.annotation1 = BasicInstanceBuilder.getUserAnnotationNotExist(data.image1,data.user1,data.term1)
        Description description = BasicInstanceBuilder.getDescriptionNotExist(data.annotation1,true)
        Property property = BasicInstanceBuilder.getAnnotationPropertyNotExist(data.annotation1,true)

        //Annotation 2: project 1 (image1) and term  from user 2
        data.annotation2 = BasicInstanceBuilder.getUserAnnotationNotExist(data.image1,data.user2,data.term1)

        //Annotation 3: project 2 (image2) with property and term  that must be skipped (not same ontology) from user 2
        data.annotation3 = BasicInstanceBuilder.getUserAnnotationNotExist(data.image2,data.user2,data.term2)
        println "data.annotation3=${data.annotation3.id}"


        //summary
        //Annotation 1: project 1 (image1) with description  with property and term from user 1
        //Annotation 2: project 1 (image1) and term  from user 2
        //Annotation 3: project 2 (image2) with property and term  that must be skipped (not same ontology) from user 2
        return data
    }
}
