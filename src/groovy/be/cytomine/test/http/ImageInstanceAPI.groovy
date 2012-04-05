package be.cytomine.test.http

import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.image.ImageInstance
import be.cytomine.image.AbstractImage

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 *
 */
class ImageInstanceAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static ImageInstance buildBasicImage(String username, String password) {
        //Create project with user 1
        def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist(), username, password)
        assert 200==result.code
        Project project = result.data
        //Add image with user 1
        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.project = project
        result = ImageInstanceAPI.createImageInstance(image, username, password)
        assert 200==result.code
        image = result.data
        return image
    }

    static def listImageInstanceByProject(Long id, String username, String password) {
        log.info("list project")
        String URL = Infos.CYTOMINEURL + "api/project/$id/imageinstance.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listImageInstanceByImage(Long id, String username, String password) {
        log.info("list project")
        String URL = Infos.CYTOMINEURL + "api/image/$id/imageinstance.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def showImageInstance(Long id, String username, String password) {
        log.info("show project:" + id)
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def createImageInstance(ImageInstance imageToAdd, User user) {
       createImageInstance(imageToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def createImageInstance(ImageInstance imageToAdd, String username, String password) {
        return createImageInstance(imageToAdd.encodeAsJSON(), username, password)
    }

    static def createImageInstance(String jsonImageInstance, User user) {
        createImageInstance(jsonImageInstance,user.username,user.password)
    }

    static def createImageInstance(String jsonImageInstance, String username, String password) {
        log.info("post ImageInstance:" + jsonImageInstance.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/imageinstance.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonImageInstance)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
//        assertEquals(200, code)
        def json = JSON.parse(response)
        Long idImage = json?.imageinstance?.id
        return [data: ImageInstance.get(idImage), code: code]
    }

    static def updateImageInstance(ImageInstance image, String username, String password) {
        Project oldProject = BasicInstance.createOrGetBasicProject()
        Project newProject = BasicInstance.getBasicProjectNotExist()
        newProject.save(flush: true)

        AbstractImage oldImage = BasicInstance.createOrGetBasicAbstractImage()
        AbstractImage newImage = BasicInstance.getBasicAbstractImageNotExist()
        newImage.save(flush: true)

        User oldUser = BasicInstance.createOrGetBasicUser()
        User newUser = BasicInstance.getBasicUserNotExist()
        newUser.save(flush: true)

        def mapNew = ["project": newProject, "baseImage": newImage, "user": newUser]
        def mapOld = ["project": oldProject, "baseImage": oldImage, "user": oldUser]


        /* Create a old image */
        log.info("create image")
        ImageInstance imageToAdd = BasicInstance.createOrGetBasicImageInstance()
        imageToAdd.project = oldProject;
        imageToAdd.baseImage = oldImage;
        imageToAdd.user = oldUser;
        imageToAdd.save(flush: true)

        /* Encode a new image to modify */
        ImageInstance imageToEdit = ImageInstance.get(imageToAdd.id)
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)

        jsonUpdate.project = newProject.id
        jsonUpdate.baseImage = newImage.id
        jsonUpdate.user = newUser.id

        jsonImage = jsonUpdate.encodeAsJSON()

        def data = updateImageInstance(image.id, jsonImage, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def updateImageInstance(def id, def jsonImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonImageInstance)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def deleteImageInstance(ImageInstance image, String username, String password) {
        log.info("delete project")
        String URL = Infos.CYTOMINEURL + "api/project/" + image.project.id + "/image/"+ image.baseImage.id +"/imageinstance.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
