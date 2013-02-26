package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageInstanceAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import be.cytomine.utils.UpdateData

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 9:11
 * To change this template use File | Settings | File Templates.
 */
class ImageInstanceTests  {

    void testListImagesInstanceByProject() {
        BasicInstance.createOrGetBasicImageInstance()
        def result = ImageInstanceAPI.listByProject(BasicInstance.createOrGetBasicProject().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = ImageInstanceAPI.listByProject(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testListImagesInstanceByProjectWithBorder() {
        BasicInstance.createOrGetBasicImageInstance()
        def result = ImageInstanceAPI.listByProject(BasicInstance.createOrGetBasicProject().id, 0,1,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListImagesInstanceWithTreeStructure() {
        BasicInstance.createOrGetBasicImageInstance()
        def result = ImageInstanceAPI.listByProjectTree(BasicInstance.createOrGetBasicProject().id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
    }


    static def listByProjectTree(Long id, Long inf, Long sup,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/imageinstance.json?tree=true"
        return doGET(URL, username, password)
    }


    void testGetImageInstanceWithCredential() {
        def result = ImageInstanceAPI.show(BasicInstance.createOrGetBasicImageInstance().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddImageInstanceCorrect() {

        def result = ImageInstanceAPI.create(BasicInstance.getBasicImageInstanceNotExist().encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
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
        def imageToAdd = BasicInstance.getBasicImageInstanceNotExist()
        def result = ImageInstanceAPI.create(imageToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        result = ImageInstanceAPI.create(imageToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }

    void testaddImageInstanceWithUnexistingAbstractImage() {
        def imageToAdd = BasicInstance.getBasicImageInstanceNotExist()
        String jsonImage = imageToAdd.encodeAsJSON()
        def updateImage = JSON.parse(jsonImage)
        updateImage.baseImage = -99
        jsonImage = updateImage.encodeAsJSON()
        def result = ImageInstanceAPI.create(jsonImage.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testaddImageInstanceWithUnexistingProject() {
        def imageToAdd = BasicInstance.createOrGetBasicImageInstance()
        String jsonImage = imageToAdd.encodeAsJSON()
        def updateImage = JSON.parse(jsonImage)
        updateImage.project = -99
        jsonImage = updateImage.encodeAsJSON()
        def result = ImageInstanceAPI.create(jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testEditImageInstance() {
        ImageInstance imageInstanceToAdd = BasicInstance.createOrGetBasicImageInstance()
        def data = UpdateData.createUpdateSet(imageInstanceToAdd)
        def result = ImageInstanceAPI.update(data.oldData.id, data.newData.encodeAsJSON(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idImageInstance = json.imageinstance.id
        def showResult = ImageInstanceAPI.show(idImageInstance, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareImageInstance(data.mapNew, json)

        showResult = ImageInstanceAPI.undo()
        assert 200==showResult.code

        showResult = ImageInstanceAPI.show(idImageInstance, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)

        BasicInstance.compareImageInstance(data.mapOld, json)

        showResult = ImageInstanceAPI.redo()
        assert 200==showResult.code

        showResult = ImageInstanceAPI.show(idImageInstance, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareImageInstance(data.mapNew, json)
    }

    void testEditImageInstanceWithBadProject() {
        ImageInstance imageToEdit = BasicInstance.createOrGetBasicImageInstance()
        def jsonUpdate = JSON.parse(imageToEdit.encodeAsJSON())
        jsonUpdate.project = -99
        def result = ImageInstanceAPI.update(imageToEdit.id, jsonUpdate.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testEditImageInstanceWithBadUser() {
        ImageInstance imageToEdit = BasicInstance.createOrGetBasicImageInstance()
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)
        jsonUpdate.user = -99
        def result = ImageInstanceAPI.update(imageToEdit.id, jsonUpdate.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testEditImageInstanceWithBadImage() {
        ImageInstance imageToEdit = BasicInstance.createOrGetBasicImageInstance()
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)
        jsonUpdate.baseImage = -99
        def result = ImageInstanceAPI.update(imageToEdit.id, jsonUpdate.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code

    }

    void testDeleteImageInstance() {
        def imageInstanceToDelete = BasicInstance.getBasicImageInstanceNotExist()
        assert imageInstanceToDelete.save(flush: true) != null
        def idImage = imageInstanceToDelete.id

        def result = ImageInstanceAPI.delete(imageInstanceToDelete, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

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
        def imageInstanceToDelete = BasicInstance.getBasicImageInstanceNotExist()
        def result = ImageInstanceAPI.delete(imageInstanceToDelete, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testGetNextImageInstance() {

        def project = BasicInstance.createOrGetBasicProject()

        def image1 = BasicInstance.getBasicImageInstanceNotExist()
        image1.project = project
        BasicInstance.saveDomain(image1)

        def image2 = BasicInstance.getBasicImageInstanceNotExist()
        image2.project = project
        BasicInstance.saveDomain(image2)

        def result = ImageInstanceAPI.next(image2.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert Long.parseLong(json.id+"") == image1.id
    }
}
