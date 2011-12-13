package be.cytomine.test.http

import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import be.cytomine.ontology.Annotation
import com.vividsolutions.jts.io.WKTReader

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 *
 */
class AnnotationAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def showAnnotation(Long id, String username, String password) {
        log.info("show Annotation:" + id)
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listAnnotation(String username, String password) {
        log.info("list annotation")
        String URL = Infos.CYTOMINEURL + "api/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listAnnotationByUser(Long id, String username, String password) {
        log.info("list annotation by user")
        String URL = Infos.CYTOMINEURL + "api/user/$id/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listAnnotationByProject(Long id, String username, String password) {
        log.info("list annotation by project")
        String URL = Infos.CYTOMINEURL + "api/project/$id/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listAnnotationByImage(Long id, String username, String password) {
        log.info("list annotation by user")
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$id/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listAnnotationByImageAndUser(Long idImage,Long idUser, String username, String password) {
        log.info("list annotation by user")
        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }



    static def createAnnotation(Annotation annotationToAdd, User user) {
       createAnnotation(annotationToAdd.encodeAsJSON(),user.username,user.password)
    }


    static def createAnnotation(Annotation annotationToAdd, String username, String password) {
        return createAnnotation(annotationToAdd.encodeAsJSON(), username, password)
    }

    static def createAnnotation(String jsonAnnotation, User user) {
        createAnnotation(jsonAnnotation,user.username,user.password)
    }

    static def createAnnotation(String jsonAnnotation, String username, String password) {
        log.info("post Annotation:" + jsonAnnotation.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/annotation.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonAnnotation)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        def json = JSON.parse(response)
        int idAnnotation = json?.annotation?.id
        return [data: Annotation.get(idAnnotation), code: code]
    }

    static def updateAnnotation(Annotation annotation, String username, String password) {
        String oldGeom = "POINT (1111 1111)"
        String newGeom = "POINT (9999 9999)"

        Double oldZoomLevel = 1
        Double newZoomLevel = 9

        String oldChannels = "OLDCHANNELS"
        String newChannels = "NEWCHANNELS"

        User oldUser = annotation.user
        User newUser = annotation.user

        def mapNew = ["geom":newGeom,"zoomLevel":newZoomLevel,"channels":newChannels,"user":newUser]
        def mapOld = ["geom":oldGeom,"zoomLevel":oldZoomLevel,"channels":oldChannels,"user":oldUser]

        /* Create a old annotation with point 1111 1111 */
        log.info("create annotation")
        Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        annotationToAdd.location =  new WKTReader().read(oldGeom)
        annotationToAdd.zoomLevel = oldZoomLevel
        annotationToAdd.channels = oldChannels
        annotationToAdd.user = oldUser
        assert (annotationToAdd.save(flush:true) != null)

        /* Encode a niew annotation with point 9999 9999 */
        Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
        def jsonEdit = annotationToEdit
        def jsonAnnotation = jsonEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotation)
        jsonUpdate.location = newGeom
        jsonUpdate.zoomLevel = newZoomLevel
        jsonUpdate.channels = newChannels
        jsonUpdate.user = newUser.id
        jsonAnnotation = jsonUpdate.encodeAsJSON()

        def data = updateAnnotation(annotation.id, jsonAnnotation, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def updateAnnotation(def id, def jsonAnnotation, String username, String password) {
        log.info("update annotation")
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put(jsonAnnotation)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def deleteAnnotation(def id, String username, String password) {
        log.info("delete annotation")
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }
}
