package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Annotation
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class AnnotationAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info "show annotation " + id
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list annotation"
        String URL = Infos.CYTOMINEURL + "api/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByUser(Long id, String username, String password) {
        log.info "list annotation by user " + id
        String URL = Infos.CYTOMINEURL + "api/user/$id/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id, String username, String password) {
        log.info "list annotation by project " + id
        String URL = Infos.CYTOMINEURL + "api/project/$id/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByImage(Long id, String username, String password) {
        log.info "list annotation by image " + id
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$id/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByTerm(Long id, String username, String password) {
        log.info "list annotation by term " + id
        String URL = Infos.CYTOMINEURL + "api/term/$id/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm, Long idUser,String username, String password) {
        log.info "list annotation by project $idProject and term $idTerm"
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/annotation.json?users="+idUser
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        log.info "list annotation by user " + idUser + " and image " + idImage
        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProjectAndUsers(Long id,Long idUser, String username, String password) {
        log.info "list annotation by user " + idUser + " and project " + id
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/annotation.json?users=" +idUser
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProjectAndUsersWithoutTerm(Long id,Long idUser, String username, String password) {
        log.info "list annotation by user " + idUser + " and project " + id
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/annotation.json?noTerm=true&users=" +idUser
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProjectAndUsersSeveralTerm(Long id,Long idUser, String username, String password) {
        log.info "list annotation by user " + idUser + " and project " + id
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/annotation.json?multipleTerm=true&users=" +idUser
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def downloadDocumentByProject(Long idProject,Long idUser, Long idTerm, Long idImageInstance, String username, String password) {
        log.info "download annotation by user " + idUser + " and project " + idProject + " and term " + idTerm
        String URL = Infos.CYTOMINEURL+"api/project/"+ idProject +"/annotation/download?users=" +idUser + "&terms=" + idTerm +"&images=" + idImageInstance + "&format=pdf"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        client.disconnect();
        return [code: code]
    }
    static def create(Annotation annotationToAdd, User user) {
       create(annotationToAdd.encodeAsJSON(),user.username,user.password)
    }

    static def create(Annotation annotationToAdd, String username, String password) {
        return create(annotationToAdd.encodeAsJSON(), username, password)
    }

    static def create(String jsonAnnotation, User user) {
        create(jsonAnnotation,user.username,user.password)
    }


    static def create(String jsonAnnotation, String username, String password) {
        log.info("post annotation:" + jsonAnnotation.replace("\n", ""))
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
        if(JSON.parse(jsonAnnotation) instanceof JSONArray) return [code: code]
        Long idAnnotation = json?.annotation?.id
        return [data: Annotation.get(idAnnotation), code: code]
    }

    static def update(Annotation annotation, String username, String password) {
        log.info "update annotation:" + annotation

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

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

        def data = update(annotation.id, jsonAnnotation, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonAnnotation, String username, String password) {
        log.info "update annotation:" + id
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

    static def delete(def id, String username, String password) {
        log.info "delete annotation:" + id
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def copy(def id, String username, String password) {
        log.info "copy annotation:" + id
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + "/copy.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post("")
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        def json = JSON.parse(response)
        Long idAnnotation = json?.annotation?.id
        return [data: Annotation.get(idAnnotation), code: code]
    }



    static def buildBasicAnnotation(String username, String password) {
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

        //Add annotation 1 with cytomine admin
        Annotation annotation = BasicInstance.getBasicAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        result = AnnotationAPI.create(annotation, username, password)
        assert 200==result.code
        annotation = result.data
        return annotation
    }
}
