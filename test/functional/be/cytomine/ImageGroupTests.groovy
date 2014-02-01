package be.cytomine

import be.cytomine.image.multidim.ImageGroup
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageGroupAPI
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
class ImageGroupTests {
    
    void testGetImageGroup() {
        def result = ImageGroupAPI.show(BasicInstanceBuilder.getImageGroup().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }    

    void testListImageGroupByProject() {
        BasicInstanceBuilder.getImageGroup()
        def result = ImageGroupAPI.list(BasicInstanceBuilder.getImageGroup().project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()>=1

        result = ImageGroupAPI.list(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testAddImageGroupCorrect() {

        def result = ImageGroupAPI.create(BasicInstanceBuilder.getImageGroupNotExist(BasicInstanceBuilder.getProject(),false).encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        ImageGroup image = result.data
        Long idImage = image.id

        result = ImageGroupAPI.show(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ImageGroupAPI.undo()
        assert 200 == result.code

        result = ImageGroupAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = ImageGroupAPI.redo()
        assert 200 == result.code

        result = ImageGroupAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

    }

    void testaddImageGroupWithUnexistingProject() {
        def imageToAdd = BasicInstanceBuilder.getImageGroup()
        String jsonImage = imageToAdd.encodeAsJSON()
        def updateImage = JSON.parse(jsonImage)
        updateImage.project = -99
        jsonImage = updateImage.toString()
        def result = ImageGroupAPI.create(jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testaddImageGroupAlreadyExist() {
        def imageGroup = BasicInstanceBuilder.getImageGroupNotExist(BasicInstanceBuilder.getProject(),false)
        def result = ImageGroupAPI.create(imageGroup.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testEditImageGroup() {

        def image = BasicInstanceBuilder.getImageGroup()
        def data = UpdateData.createUpdateSet(image,[project: [BasicInstanceBuilder.getProject(),BasicInstanceBuilder.getProjectNotExist(true)]])

        def result = ImageGroupAPI.update(image.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idImageGroup = json.imagegroup.id
        def showResult = ImageGroupAPI.show(idImageGroup, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = ImageGroupAPI.undo()
        assert 200==showResult.code

        showResult = ImageGroupAPI.show(idImageGroup, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)

        BasicInstanceBuilder.compare(data.mapOld, json)

        showResult = ImageGroupAPI.redo()
        assert 200==showResult.code

        showResult = ImageGroupAPI.show(idImageGroup, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)
    }

    void testEditImageGroupWithBadProject() {
        ImageGroup imageToEdit = BasicInstanceBuilder.getImageGroup()
        def jsonUpdate = JSON.parse(imageToEdit.encodeAsJSON())
        jsonUpdate.project = -99
        def result = ImageGroupAPI.update(imageToEdit.id, jsonUpdate.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testDeleteImageGroup() {
        def imageGroupToDelete = BasicInstanceBuilder.getImageGroupNotExist()
        assert imageGroupToDelete.save(flush: true) != null
        def idImage = imageGroupToDelete.id

        def result = ImageGroupAPI.delete(imageGroupToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def showResult = ImageGroupAPI.show(imageGroupToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code

        result = ImageGroupAPI.undo()
        assert 200 == result.code

        result = ImageGroupAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ImageGroupAPI.redo()
        assert 200 == result.code

        result = ImageGroupAPI.show(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteImageGroupNoExist() {
        def imageGroupToDelete = BasicInstanceBuilder.getImageGroupNotExist()
        def result = ImageGroupAPI.delete(imageGroupToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
}
