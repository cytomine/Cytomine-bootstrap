package be.cytomine

import be.cytomine.image.NestedImageInstance
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.NestedImageInstanceAPI
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
class NestedImageInstanceTests {

    void testGetNestedImageInstanceWithCredential() {
        def nested = BasicInstanceBuilder.getNestedImageInstance()
        def result = NestedImageInstanceAPI.show(nested.id,nested.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.id.toString()== nested.id.toString()

        result = ImageInstanceAPI.show(nested.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.id.toString()== nested.id.toString()
    }

    void testListNestedImagesInstanceByImage() {
        def nested = BasicInstanceBuilder.getNestedImageInstance()
        def result = NestedImageInstanceAPI.listByImageInstance(nested.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert NestedImageInstanceAPI.containsInJSONList(nested.id,json)

        result = NestedImageInstanceAPI.listByImageInstance(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testListImageCheckIfNoNestedImageInstance() {
        def nested = BasicInstanceBuilder.getNestedImageInstanceNotExist( BasicInstanceBuilder.getImageInstance(),true)

        def result = ImageInstanceAPI.listByProject(nested.project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        def json = JSON.parse(result.data)
        assert NestedImageInstanceAPI.containsInJSONList(nested.parent.id,json)
        assert !NestedImageInstanceAPI.containsInJSONList(nested.id,json)

        result = ImageInstanceAPI.listByProject(nested.project.id,1,2, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(result.data)
        assert NestedImageInstanceAPI.containsInJSONList(nested.parent.id,json)
        assert !NestedImageInstanceAPI.containsInJSONList(nested.id,json)

        result = ImageInstanceAPI.listByProject(nested.project.id, 0,1,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(result.data)
        assert NestedImageInstanceAPI.containsInJSONList(nested.parent.id,json)
        assert !NestedImageInstanceAPI.containsInJSONList(nested.id,json)

        result = ImageInstanceAPI.listByUser(nested.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(result.data)
        assert NestedImageInstanceAPI.containsInJSONList(nested.parent.id,json)
        assert !NestedImageInstanceAPI.containsInJSONList(nested.id,json)

    }


    void testAddNestedImageInstanceCorrect() {
        def nested = BasicInstanceBuilder.getNestedImageInstanceNotExist()
        println nested.encodeAsJSON()
        def result = NestedImageInstanceAPI.create(nested.parent.id,nested.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        NestedImageInstance image = result.data
        Long idImage = image.id

        result = NestedImageInstanceAPI.show(image.id,image.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = NestedImageInstanceAPI.undo()
        assert 200 == result.code

        result = NestedImageInstanceAPI.show(idImage,image.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = NestedImageInstanceAPI.redo()
        assert 200 == result.code

        result = NestedImageInstanceAPI.show(idImage,image.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

    }


    void testAddNestedImageInstanceAlreadyExist() {
        def imageToAdd = BasicInstanceBuilder.getNestedImageInstanceNotExist()
        def result = NestedImageInstanceAPI.create(imageToAdd.parent.id,imageToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        result = NestedImageInstanceAPI.create(imageToAdd.parent.id,imageToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }

    void testaddNestedImageInstanceWithUnexistingAbstractImage() {
        def imageToAdd = BasicInstanceBuilder.getNestedImageInstanceNotExist()
        String jsonImage = imageToAdd.encodeAsJSON()
        def updateImage = JSON.parse(jsonImage)
        updateImage.baseImage = -99
        jsonImage = updateImage.toString()
        def result = NestedImageInstanceAPI.create(imageToAdd.parent.id,jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testaddNestedImageInstanceWithUnexistingImageInstance() {
        def imageToAdd = BasicInstanceBuilder.getNestedImageInstanceNotExist()
        String jsonImage = imageToAdd.encodeAsJSON()
        def updateImage = JSON.parse(jsonImage)
        updateImage.parent = -99
        jsonImage = updateImage.toString()
        def result = NestedImageInstanceAPI.create(imageToAdd.parent.id,jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }



    void testaddNestedImageInstanceWithUnexistingProject() {
        def imageToAdd = BasicInstanceBuilder.getNestedImageInstance()
        String jsonImage = imageToAdd.encodeAsJSON()
        def updateImage = JSON.parse(jsonImage)
        updateImage.project = -99
        jsonImage = updateImage.toString()
        def result = NestedImageInstanceAPI.create(imageToAdd.parent.id,jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testEditNestedImageInstance() {

        def image = BasicInstanceBuilder.getNestedImageInstance()
        def data = UpdateData.createUpdateSet(image,[project: [BasicInstanceBuilder.getProject(),BasicInstanceBuilder.getProjectNotExist(true)]])

        def result = NestedImageInstanceAPI.update(image.id,image.parent.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idNestedImageInstance = json.nestedimageinstance.id
        def showResult = NestedImageInstanceAPI.show(idNestedImageInstance,image.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = NestedImageInstanceAPI.undo()
        assert 200==showResult.code

        showResult = NestedImageInstanceAPI.show(idNestedImageInstance,image.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)

        BasicInstanceBuilder.compare(data.mapOld, json)

        showResult = NestedImageInstanceAPI.redo()
        assert 200==showResult.code

        showResult = NestedImageInstanceAPI.show(idNestedImageInstance,image.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)
    }

    void testDeleteNestedImageInstance() {
        def NestedImageInstanceToDelete = BasicInstanceBuilder.getNestedImageInstanceNotExist()
        assert NestedImageInstanceToDelete.save(flush: true) != null
        def idImage = NestedImageInstanceToDelete.id

        def result = NestedImageInstanceAPI.delete(NestedImageInstanceToDelete.id,NestedImageInstanceToDelete.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def showResult = NestedImageInstanceAPI.show(NestedImageInstanceToDelete.id,NestedImageInstanceToDelete.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code

        result = NestedImageInstanceAPI.undo()
        assert 200 == result.code

        result = NestedImageInstanceAPI.show(idImage,NestedImageInstanceToDelete.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = NestedImageInstanceAPI.redo()
        assert 200 == result.code

        result = NestedImageInstanceAPI.show(idImage,NestedImageInstanceToDelete.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteNestedImageInstanceNoExist() {
        def NestedImageInstanceToDelete = BasicInstanceBuilder.getNestedImageInstanceNotExist()
        def result = NestedImageInstanceAPI.delete(NestedImageInstanceToDelete.id,NestedImageInstanceToDelete.parent.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


}
