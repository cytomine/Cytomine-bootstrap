package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.DomainAPI
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.UserAnnotationAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 9:11
 * To change this template use File | Settings | File Templates.
 */
class ImageInstanceTests  {


    void testListImagesInstanceByProject() {
        BasicInstanceBuilder.getImageInstance()
        def result = ImageInstanceAPI.listByProject(BasicInstanceBuilder.getProject().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = ImageInstanceAPI.listByProject(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListImagesInstanceByProjectDatatables() {
        BasicInstanceBuilder.getImageInstance()
        def result = ImageInstanceAPI.listByProjectDatatables(BasicInstanceBuilder.getProject().id, 1,2,"test",Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = ImageInstanceAPI.listByProjectDatatables(BasicInstanceBuilder.getProject().id, 1,2,null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testListImagesInstanceByProjectMaxOffset() {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        BasicInstanceBuilder.getImageInstanceNotExist(project,true)


        def result = ImageInstanceAPI.listByProject(BasicInstanceBuilder.getProject().id,1,2, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }


    void testListImagesInstanceByProjectWithBorder() {
        BasicInstanceBuilder.getImageInstance()
        def result = ImageInstanceAPI.listByProject(BasicInstanceBuilder.getProject().id, 0,1,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }


    void testListImagesInstanceByUserLigh() {
        BasicInstanceBuilder.getImageInstance()
        def result = ImageInstanceAPI.listByUser(BasicInstanceBuilder.getUser1().id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }



    void testListImagesInstanceWithTreeStructure() {
        BasicInstanceBuilder.getImageInstance()
        def result = ImageInstanceAPI.listByProjectTree(BasicInstanceBuilder.getProject().id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
    }


    static def listByProjectTree(Long id, Long inf, Long sup,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/imageinstance.json?tree=true"
        return doGET(URL, username, password)
    }


    void testGetImageInstanceWithCredential() {
        def result = ImageInstanceAPI.show(BasicInstanceBuilder.getImageInstance().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddImageInstanceCorrect() {

        def result = ImageInstanceAPI.create(BasicInstanceBuilder.getImageInstanceNotExist().encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        ImageInstance image = result.data
        Long idImage = image.id

        result = ImageInstanceAPI.show(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ImageInstanceAPI.undo()
        assert 200 == result.code

        result = ImageInstanceAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = ImageInstanceAPI.redo()
        assert 200 == result.code

        result = ImageInstanceAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

    }

    void testAddImageInstanceAlreadyExist() {
        def imageToAdd = BasicInstanceBuilder.getImageInstanceNotExist()
        def result = ImageInstanceAPI.create(imageToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        result = ImageInstanceAPI.create(imageToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }

    void testaddImageInstanceWithUnexistingAbstractImage() {
        def imageToAdd = BasicInstanceBuilder.getImageInstanceNotExist()
        String jsonImage = imageToAdd.encodeAsJSON()
        def updateImage = JSON.parse(jsonImage)
        updateImage.baseImage = -99
        jsonImage = updateImage.toString()
        def result = ImageInstanceAPI.create(jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testaddImageInstanceWithUnexistingProject() {
        def imageToAdd = BasicInstanceBuilder.getImageInstance()
        String jsonImage = imageToAdd.encodeAsJSON()
        def updateImage = JSON.parse(jsonImage)
        updateImage.project = -99
        jsonImage = updateImage.toString()
        def result = ImageInstanceAPI.create(jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testEditImageInstance() {


        def image = BasicInstanceBuilder.getImageInstance()
        def data = UpdateData.createUpdateSet(image,[project: [BasicInstanceBuilder.getProject(),BasicInstanceBuilder.getProjectNotExist(true)]])

        def result = ImageInstanceAPI.update(image.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idImageInstance = json.imageinstance.id
        def showResult = ImageInstanceAPI.show(idImageInstance, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = ImageInstanceAPI.undo()
        assert 200==showResult.code

        showResult = ImageInstanceAPI.show(idImageInstance, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)

        BasicInstanceBuilder.compare(data.mapOld, json)

        showResult = ImageInstanceAPI.redo()
        assert 200==showResult.code

        showResult = ImageInstanceAPI.show(idImageInstance, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)
    }

    void testEditImageInstanceWithBadProject() {
        ImageInstance imageToEdit = BasicInstanceBuilder.getImageInstance()
        def jsonUpdate = JSON.parse(imageToEdit.encodeAsJSON())
        jsonUpdate.project = -99
        def result = ImageInstanceAPI.update(imageToEdit.id, jsonUpdate.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testEditImageInstanceWithBadUser() {
        ImageInstance imageToEdit = BasicInstanceBuilder.getImageInstance()
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)
        jsonUpdate.user = -99
        def result = ImageInstanceAPI.update(imageToEdit.id, jsonUpdate.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testEditImageInstanceWithBadImage() {
        ImageInstance imageToEdit = BasicInstanceBuilder.getImageInstance()
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)
        jsonUpdate.baseImage = -99
        def result = ImageInstanceAPI.update(imageToEdit.id, jsonUpdate.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code

    }

    void testDeleteImageInstance() {
        def imageInstanceToDelete = BasicInstanceBuilder.getImageInstanceNotExist()
        assert imageInstanceToDelete.save(flush: true) != null
        def idImage = imageInstanceToDelete.id
        println "Image=${imageInstanceToDelete.id} ${imageInstanceToDelete.deleted}"

        def result = ImageInstanceAPI.delete(imageInstanceToDelete, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        imageInstanceToDelete.refresh()
        println "Image=${imageInstanceToDelete.id} ${imageInstanceToDelete.deleted}"
        def showResult = ImageInstanceAPI.show(imageInstanceToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code

        result = ImageInstanceAPI.undo()
        assert 200 == result.code

        result = ImageInstanceAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ImageInstanceAPI.redo()
        assert 200 == result.code

        result = ImageInstanceAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteImageInstanceNoExist() {
        def imageInstanceToDelete = BasicInstanceBuilder.getImageInstanceNotExist()
        def result = ImageInstanceAPI.delete(imageInstanceToDelete, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testGetNextImageInstance() {

        def project = BasicInstanceBuilder.getProject()

        def image1 = BasicInstanceBuilder.getImageInstanceNotExist()
        image1.project = project
        BasicInstanceBuilder.saveDomain(image1)

        def image2 = BasicInstanceBuilder.getImageInstanceNotExist()
        image2.project = project
        BasicInstanceBuilder.saveDomain(image2)

        def result = ImageInstanceAPI.next(image2.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert Long.parseLong(json.id+"") == image1.id

        result = ImageInstanceAPI.next(image1.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testGetPreviousImageInstance() {

        def project = BasicInstanceBuilder.getProject()

        def image1 = BasicInstanceBuilder.getImageInstanceNotExist()
        image1.project = project
        BasicInstanceBuilder.saveDomain(image1)

        def image2 = BasicInstanceBuilder.getImageInstanceNotExist()
        image2.project = project
        BasicInstanceBuilder.saveDomain(image2)

        def result = ImageInstanceAPI.previous(image1.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert Long.parseLong(json.id+"") == image2.id

        result = ImageInstanceAPI.previous(image2.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }


    void testListDeleteImageInstance() {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(project,image,true)

        assert 200 == ImageInstanceAPI.show(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD).code

        def response = ImageInstanceAPI.listByProjectDatatables(project.id,0,0,null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == response.code
        assert DomainAPI.containsInJSONList(image.id,JSON.parse(response.data))

        response = ImageInstanceAPI.listByProject(project.id,0,0,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == response.code
        assert DomainAPI.containsInJSONList(image.id,JSON.parse(response.data))

        response = ImageInstanceAPI.listByProject(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == response.code
        assert DomainAPI.containsInJSONList(image.id,JSON.parse(response.data))

        response = UserAnnotationAPI.listByImage(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == response.code
        assert DomainAPI.containsInJSONList(annotation.id,JSON.parse(response.data))

        response = UserAnnotationAPI.listByImages(project.id,[image.id],Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == response.code
        assert DomainAPI.containsInJSONList(annotation.id,JSON.parse(response.data))

        //now delete image and check if correctloy removed from listing
        def result = ImageInstanceAPI.delete(image, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        assert 404 == ImageInstanceAPI.show(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD).code

        response = ImageInstanceAPI.listByProjectDatatables(project.id,0,0,null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == response.code
        assert !DomainAPI.containsInJSONList(image.id,JSON.parse(response.data))

        response = ImageInstanceAPI.listByProject(project.id,0,0,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == response.code
        assert !DomainAPI.containsInJSONList(image.id,JSON.parse(response.data))

        response = ImageInstanceAPI.listByProject(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == response.code
        assert !DomainAPI.containsInJSONList(image.id,JSON.parse(response.data))

        response = UserAnnotationAPI.listByImage(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == response.code
        assert !DomainAPI.containsInJSONList(annotation.id,JSON.parse(response.data))

//        response = UserAnnotationAPI.listByImages(project.id,[image.id],Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assert !DomainAPI.containsInJSONList(annotation.id,JSON.parse(response.data))




    }

    void testDeleteImageInstanceAndRestoreIt() {

        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        def result = ImageInstanceAPI.create(BasicInstanceBuilder.getImageInstanceNotExist(project).encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        ImageInstance image = result.data
        Long idImage = image.id

        project.refresh()
        assert project.countImages == 1

        result = ImageInstanceAPI.show(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ImageInstanceAPI.delete(image, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        println result.data


        image = ImageInstance.read(image.id)
        println image.id+"=>"+image.deleted  + " version"+image.version
        image.refresh()
        println image.id+"=>"+image.deleted + " version"+image.version

        println project.list().collect{it.name}
        project.refresh()
        //assert project.countImages == 0

        result = ImageInstanceAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code


        result = ImageInstanceAPI.create(image.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        project.refresh()
        assert project.countImages == 1

        result = ImageInstanceAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

    }
}
