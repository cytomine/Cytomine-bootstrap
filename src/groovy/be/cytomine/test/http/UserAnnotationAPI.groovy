package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.ontology.UserAnnotation

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class UserAnnotationAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        log.info "show userannotation " + id
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list userannotation"
        String URL = Infos.CYTOMINEURL + "api/userannotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByUser(Long id, String username, String password) {
        log.info "list userannotation by user " + id
        String URL = Infos.CYTOMINEURL + "api/user/$id/userannotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id, String username, String password) {
        log.info "list userannotation by project " + id
        String URL = Infos.CYTOMINEURL + "api/project/$id/userannotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        log.info "list userannotation by project " + id
        String URL = Infos.CYTOMINEURL + "api/project/$id/userannotation.json?users="+idUser+"&images="+idImage
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByImage(Long id, String username, String password) {
        log.info "list userannotation by image " + id
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$id/userannotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByTerm(Long id, String username, String password) {
        log.info "list userannotation by term " + id
        String URL = Infos.CYTOMINEURL + "api/term/$id/userannotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm, Long idUser,String username, String password) {
        log.info "list userannotation by project $idProject and term $idTerm"
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/userannotation.json?users="+idUser
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm,Long idImage, Long idUser,String username, String password) {
        log.info "list userannotation by project $idProject and term $idTerm"
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/userannotation.json?users="+idUser+"&offset=0&max=5"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        log.info "list userannotation by user " + idUser + " and image " + idImage
        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/userannotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProjectAndUsers(Long id,Long idUser, String username, String password) {
        log.info "list userannotation by user " + idUser + " and project " + id
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/userannotation.json?users=" +idUser
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProjectAndUsersWithoutTerm(Long id,Long idUser, String username, String password) {
        log.info "list userannotation by user " + idUser + " and project " + id
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/userannotation.json?noTerm=true&users=" +idUser
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProjectAndUsersSeveralTerm(Long id,Long idUser, String username, String password) {
        log.info "list userannotation by user " + idUser + " and project " + id
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/userannotation.json?multipleTerm=true&users=" +idUser
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def downloadDocumentByProject(Long idProject,Long idUser, Long idTerm, Long idImageInstance, String username, String password) {
        log.info "download userannotation by user " + idUser + " and project " + idProject + " and term " + idTerm
        String URL = Infos.CYTOMINEURL+"api/project/"+ idProject +"/userannotation/download?users=" +idUser + "&terms=" + idTerm +"&images=" + idImageInstance + "&format=pdf"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        client.disconnect();
        return [code: code]
    }
    static def create(UserAnnotation annotationToAdd, User user) {
       create(annotationToAdd.encodeAsJSON(),user.username,user.password)
    }

    static def create(UserAnnotation annotationToAdd, String username, String password) {
        return create(annotationToAdd.encodeAsJSON(), username, password)
    }

    static def create(String jsonAnnotation, User user) {
        create(jsonAnnotation,user.username,user.password)
    }


    static def create(String jsonAnnotation, String username, String password) {
        log.info("post userAnnotation:" + jsonAnnotation.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/userannotation.json"
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
        return [data: UserAnnotation.get(idAnnotation), code: code]
    }

    static def update(UserAnnotation annotation, String username, String password) {
        log.info "update userAnnotation:" + annotation

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

        User oldUser = annotation.user
        User newUser = annotation.user

        def mapNew = ["geom":newGeom,"user":newUser]
        def mapOld = ["geom":oldGeom,"user":oldUser]

        /* Create a old annotation with point 1111 1111 */
        log.info("create userAnnotation")
        UserAnnotation annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
        annotationToAdd.location =  new WKTReader().read(oldGeom)
        annotationToAdd.user = oldUser
        assert (annotationToAdd.save(flush:true) != null)

        /* Encode a niew annotation with point 9999 9999 */
        UserAnnotation annotationToEdit = UserAnnotation.get(annotationToAdd.id)
        def jsonEdit = annotationToEdit
        def jsonAnnotation = jsonEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotation)
        jsonUpdate.location = newGeom
        jsonUpdate.user = newUser.id
        jsonAnnotation = jsonUpdate.encodeAsJSON()

        def data = update(annotation.id, jsonAnnotation, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonAnnotation, String username, String password) {
        log.info "update userannotation:" + id
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
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
        log.info "delete userannotation:" + id
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def copy(def id, String username, String password) {
        log.info "copy userannotation:" + id
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + "/copy.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post("")
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        def json = JSON.parse(response)
        Long idAnnotation = json?.annotation?.id
        return [data: UserAnnotation.get(idAnnotation), code: code]
    }



    static def buildBasicUserAnnotation(String username, String password) {
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
        UserAnnotation annotation = BasicInstance.getBasicUserAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        result = UserAnnotationAPI.create(annotation, username, password)
        assert 200==result.code
        annotation = result.data
        return annotation
    }
}
