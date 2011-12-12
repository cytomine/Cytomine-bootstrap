package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.image.AbstractImage
import be.cytomine.image.acquisition.Scanner
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.security.User
import be.cytomine.project.Slide
import be.cytomine.image.Mime
import com.vividsolutions.jts.io.WKTReader
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.test.http.ImageInstanceAPI
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 9:11
 * To change this template use File | Settings | File Templates.
 */
class ImageInstanceTests extends functionaltestplugin.FunctionalTestCase {

    void testListImagesInstanceByProjectWithCredential() {
        log.info("create imageinstance")
        ImageInstance image = BasicInstance.createOrGetBasicImageInstance()
        Project project = BasicInstance.createOrGetBasicProject()
        User user = BasicInstance.createOrGetBasicUser()
        log.info("list project by user")
        def result = ImageInstanceAPI.listImageInstanceByProject(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testGetImageInstanceWithCredential() {
        log.info("create project")
        ImageInstance image = BasicInstance.createOrGetBasicImageInstance()
        log.info("show project")
        def result = ImageInstanceAPI.showImageInstance(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddImageInstanceCorrect() {

        log.info("create project")
        def imageToAdd = BasicInstance.getBasicImageInstanceNotExist()
        def result = ImageInstanceAPI.createImageInstance(imageToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(200, result.code)
        ImageInstance image = result.data
        Long idImage = image.id
        log.info("check if object " + image.id + " exist in DB")
        result = ImageInstanceAPI.showImageInstance(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        log.info("test undo")
        result = ImageInstanceAPI.undo()
        assertEquals(200, result.code)

        log.info("check if object " + idImage + " not exist in DB")
        result = ImageInstanceAPI.showImageInstance(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)

        log.info("test redo")
        result = ImageInstanceAPI.redo()
        assertEquals(200, result.code)

        log.info("check if object " + idImage + " not exist in DB")
        result = ImageInstanceAPI.showImageInstance(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

    }

    void testAddImageInstanceAlreadyExist() {

        log.info("create imageinstance")
        def imageToAdd = BasicInstance.getBasicImageInstanceNotExist()
        String jsonImage = imageToAdd.encodeAsJSON()
        def result = ImageInstanceAPI.createImageInstance(imageToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        result = ImageInstanceAPI.createImageInstance(imageToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(400, result.code)
    }

    void testaddImageInstanceWithUnexistingAbstractImage() {

        log.info("create imageinstance")
        def imageToAdd = BasicInstance.createOrGetBasicImageInstance()
        String jsonImage = imageToAdd.encodeAsJSON()
        def updateImage = JSON.parse(jsonImage)
        updateImage.baseImage = -99
        jsonImage = updateImage.encodeAsJSON()

        def result = ImageInstanceAPI.createImageInstance(imageToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        result = ImageInstanceAPI.createImageInstance(imageToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(400, result.code)

    }

    void testaddImageInstanceWithUnexistingProject() {

        log.info("create image")
        def imageToAdd = BasicInstance.createOrGetBasicImageInstance()
        String jsonImage = imageToAdd.encodeAsJSON()
        def updateImage = JSON.parse(jsonImage)
        updateImage.project = -99
        jsonImage = updateImage.encodeAsJSON()

        def result = ImageInstanceAPI.createImageInstance(jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(400, result.code)

    }


    void testEditImageInstance() {

        log.info("create project")
        ImageInstance imageInstanceToAdd = BasicInstance.createOrGetBasicImageInstance()
        def result = ImageInstanceAPI.updateImageInstance(imageInstanceToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check responsex:"+result)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idImageInstance = json.imageinstance.id
        def showResult = ImageInstanceAPI.showImageInstance(idImageInstance, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareImageInstance(result.mapNew, json)

        log.info("test undo")
        showResult = ImageInstanceAPI.undo()
        assertEquals(200, result.code)
        showResult = ImageInstanceAPI.showImageInstance(idImageInstance, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)

        BasicInstance.compareImageInstance(result.mapOld, json)

        log.info("test redo")
        showResult = ImageInstanceAPI.redo()
        assertEquals(200, result.code)
        showResult = ImageInstanceAPI.showImageInstance(idImageInstance, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareImageInstance(result.mapNew, json)
    }


    void testEditImageInstanceWithBadProject() {
        Project oldProject = BasicInstance.createOrGetBasicProject()
        Project newProject = BasicInstance.getBasicProjectNotExist()

        /* Create a old image */
        log.info("create imageinstance")
        ImageInstance imageToAdd = BasicInstance.createOrGetBasicImageInstance()
        imageToAdd.project = oldProject
        imageToAdd.save(flush: true)

        /* Encode a new image to modify */
        ImageInstance imageToEdit = ImageInstance.get(imageToAdd.id)
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)

        jsonUpdate.project = -99

        jsonImage = jsonUpdate.encodeAsJSON()

        def result = ImageInstanceAPI.updateImageInstance(imageToAdd.id, jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testEditImageInstanceWithBadUser() {
        User oldUser = BasicInstance.createOrGetBasicUser()
        User newUser = BasicInstance.getBasicUserNotExist()

        /* Create a old image */
        log.info("create imageinstance")
        ImageInstance imageToAdd = BasicInstance.createOrGetBasicImageInstance()
        imageToAdd.user = oldUser
        imageToAdd.save(flush: true)

        /* Encode a new image to modify */
        ImageInstance imageToEdit = ImageInstance.get(imageToAdd.id)
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)

        jsonUpdate.user = -99

        jsonImage = jsonUpdate.encodeAsJSON()

        def result = ImageInstanceAPI.updateImageInstance(imageToAdd.id, jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testEditImageInstanceWithBadImage() {
        AbstractImage oldImage = BasicInstance.createOrGetBasicAbstractImage()
        AbstractImage newImage = BasicInstance.getBasicAbstractImageNotExist()

        /* Create a old image */
        log.info("create imageinstance")
        ImageInstance imageToAdd = BasicInstance.createOrGetBasicImageInstance()
        imageToAdd.baseImage = oldImage
        imageToAdd.save(flush: true)

        /* Encode a new image to modify */
        ImageInstance imageToEdit = ImageInstance.get(imageToAdd.id)
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)

        jsonUpdate.baseImage = -99

        jsonImage = jsonUpdate.encodeAsJSON()

        def result = ImageInstanceAPI.updateImageInstance(imageToAdd.id, jsonImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)

    }


    void testDeleteImageInstance() {
        log.info("create project")
        def imageInstanceToDelete = BasicInstance.getBasicImageInstanceNotExist()
        assert imageInstanceToDelete.save(flush: true) != null
        def idImage = imageInstanceToDelete.id
        log.info("delete project")
        def result = ImageInstanceAPI.deleteImageInstance(imageInstanceToDelete, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def showResult = ImageInstanceAPI.showImageInstance(imageInstanceToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(404, showResult.code)

        log.info("test undo")
        result = ImageInstanceAPI.undo()
        assertEquals(200, result.code)

        log.info("check if object " + idImage + " not exist in DB")
        result = ImageInstanceAPI.showImageInstance(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        log.info("test redo")
        result = ImageInstanceAPI.redo()
        assertEquals(200, result.code)

        log.info("check if object " + idImage + " not exist in DB")
        result = ImageInstanceAPI.showImageInstance(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteImageInstanceNoExist() {
        log.info("delete project")
        def imageInstanceToDelete = BasicInstance.getBasicImageInstanceNotExist()
        def result = ImageInstanceAPI.deleteImageInstance(imageInstanceToDelete, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(404, result.code)
    }
}
