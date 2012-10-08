package be.cytomine.test.http

import be.cytomine.image.AbstractImage
import be.cytomine.image.Mime
import be.cytomine.image.acquisition.Instrument
import be.cytomine.project.Slide
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
class AbstractImageAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)
    static def list(String username, String password) {
        log.info("list AbstractImage")
        String URL = Infos.CYTOMINEURL + "api/image.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def show(Long id, String username, String password) {
        log.info("show AbstractImage:" + id)
        String URL = Infos.CYTOMINEURL + "api/image/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }



    static def listByUser(Long id, String username, String password) {
        log.info("list AbstractImage by user")
        String URL = Infos.CYTOMINEURL + "api/user/$id/image.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(AbstractImage AbstractImageToAdd, User user) {
       create(AbstractImageToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def create(AbstractImage AbstractImageToAdd, String username, String password) {
        return create(AbstractImageToAdd.encodeAsJSON(), username, password)
    }

    static def create(String jsonAbstractImage, User user) {
        create(jsonAbstractImage,user.username,user.password)
    }

    static def create(String jsonAbstractImage, String username, String password) {
        log.info("post AbstractImage:" + jsonAbstractImage.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/image.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonAbstractImage)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        def json = JSON.parse(response)
        Long idAbstractImage = json?.abstractimage?.id
        return [data: AbstractImage.get(idAbstractImage), code: code]
    }

    static def update(AbstractImage AbstractImage, String username, String password) {
        String oldFilename = "oldName"
        String newFilename = "newName"

        String oldGeom = "POINT (1111 1111)"
        String newGeom = "POINT (9999 9999)"

        Instrument oldScanner = BasicInstance.createOrGetBasicScanner()
        Instrument newScanner = BasicInstance.getNewScannerNotExist()
        newScanner.save(flush:true)

        Slide oldSlide = BasicInstance.createOrGetBasicSlide()
        Slide newSlide = BasicInstance.getBasicSlideNotExist()
        newSlide.save(flush:true)

        User oldUser = BasicInstance.createOrGetBasicUser()
        User newUser = BasicInstance.getBasicUserNotExist()
        newUser.save(flush:true)


        String oldPath = "oldPath"
        String newPath = "newPath"

        Mime oldMime = BasicInstance.createOrGetBasicMime() //TODO: replace by a mime different with image server
        Mime newMime = BasicInstance.createOrGetBasicMime()  //jp2

        Integer oldWidth = 1000
        Integer newWidth = 9000

        Integer oldHeight = 10000
        Integer newHeight = 900000


        def mapNew = ["filename":newFilename,"geom":newGeom,"scanner":newScanner,"slide":newSlide,"path":newPath,"mime":newMime,"width":newWidth,"height":newHeight,"user":newUser]
        def mapOld = ["filename":oldFilename,"geom":oldGeom,"scanner":oldScanner,"slide":oldSlide,"path":oldPath,"mime":oldMime,"width":oldWidth,"height":oldHeight,"user":oldUser]

        /* Create a old AbstractImage with point 1111 1111 */
        /* Create a old image */
        log.info("create image")
        AbstractImage imageToAdd = BasicInstance.createOrGetBasicAbstractImage()
        imageToAdd.filename = oldFilename
        imageToAdd.scanner = oldScanner
        imageToAdd.slide = oldSlide
        imageToAdd.path = oldPath
        imageToAdd.mime = oldMime
        imageToAdd.width = oldWidth
        imageToAdd.height = oldHeight
        imageToAdd.save(flush:true)

        /* Encode a new image to modify */
        AbstractImage imageToEdit = AbstractImage.get(imageToAdd.id)
        def jsonImage = imageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonImage)

        jsonUpdate.filename = newFilename
        jsonUpdate.scanner = newScanner.id
        jsonUpdate.slide = newSlide.id
        jsonUpdate.path = newPath
        jsonUpdate.mime = newMime.extension
        jsonUpdate.width = newWidth
        jsonUpdate.height = newHeight
        jsonImage = jsonUpdate.encodeAsJSON()

        def data = update(imageToEdit.id, jsonImage, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonAbstractImage, String username, String password) {
        log.info("update AbstractImage")
        String URL = Infos.CYTOMINEURL + "api/image/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonAbstractImage)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def delete(def id, String username, String password) {
        log.info("delete AbstractImage")
        String URL = Infos.CYTOMINEURL + "api/image/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
