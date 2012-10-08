package be.cytomine.test.http

import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

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
        result = ImageInstanceAPI.create(image, username, password)
        assert 200==result.code
        image = result.data
        return image
    }

    static def listByProject(Long id, String username, String password) {
        log.info "list image by project $id"
        String URL = Infos.CYTOMINEURL + "api/project/$id/imageinstance.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def show(Long id, String username, String password) {
        log.info "show image $id"
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def create(ImageInstance imageToAdd, User user) {
       create(imageToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(ImageInstance imageToAdd, String username, String password) {
        return create(imageToAdd.encodeAsJSON(), username, password)
    }

    static def create(String jsonImageInstance, User user) {
        create(jsonImageInstance,user.username,user.password)
    }

    static def create(String jsonImageInstance, String username, String password) {
        log.info "create ImageInstance"
        String URL = Infos.CYTOMINEURL + "api/imageinstance.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonImageInstance)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        def json = JSON.parse(response)
        Long idImage = json?.imageinstance?.id
        return [data: ImageInstance.get(idImage), code: code]
    }

    static def update(ImageInstance image, String username, String password) {
        log.info "update ImageInstance"
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

        def data = update(image.id, jsonImage, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonImageInstance, String username, String password) {
        log.info "update ImageInstance $id"
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

    static def delete(ImageInstance image, String username, String password) {
        log.info "delete ImageInstance"
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
