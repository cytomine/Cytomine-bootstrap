package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.AnnotationDomain
import be.cytomine.ontology.UserAnnotation

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class AnnotationDomainAPI extends DomainAPI {

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
        log.info "list aannotation by project " + id
        String URL = Infos.CYTOMINEURL + "api/project/$id/annotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        log.info "list annotation by project " + id
        String URL = Infos.CYTOMINEURL + "api/project/$id/annotation.json?users="+idUser+"&images="+idImage
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByTerm(Long idTerm, String username, String password) {
        log.info "list annotation by idTerm $idTerm"
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/annotation.json"
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

    static def listByProjectAndTerm(Long idProject, Long idTerm,Long idImage, Long idUser,String username, String password) {
        log.info "list annotation by project $idProject and term $idTerm"
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/annotation.json?users="+idUser+"&offset=0&max=5"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProjectAndTermWithSuggest(Long idProject, Long idTerm,Long idSuggest, Long idJob,String username, String password) {
        log.info "listByProjectAndTermWithSuggest by project $idProject and term $idTerm"
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/annotation.json?suggestTerm="+idSuggest+"&job=$idJob"
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
    static def create(AnnotationDomain annotationToAdd, User user) {
       create(annotationToAdd.encodeAsJSON(),user.username,user.password)
    }

    static def create(AnnotationDomain annotationToAdd, String username, String password) {
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
        AnnotationDomain annotation = UserAnnotation.read(idAnnotation)
        if(!annotation)  annotation = AlgoAnnotation.read(idAnnotation)


        return [data: annotation, code: code]
    }

    static def update(AnnotationDomain annotation, String username, String password) {
        log.info "update AnnotationDomain:" + annotation

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

        def oldUser = annotation.user
        def newUser = annotation.user

        def mapNew = ["geom":newGeom,"user":newUser]
        def mapOld = ["geom":oldGeom,"user":oldUser]

        /* Create a old annotation with point 1111 1111 */
        log.info("create AnnotationDomain")

        def jsonAnnotation

        if(annotation instanceof UserAnnotation) {
            log.info("create userAnnotation")
            UserAnnotation annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()
            annotationToAdd.location =  new WKTReader().read(oldGeom)
            annotationToAdd.user = oldUser
            assert (annotationToAdd.save(flush:true) != null)

            /* Encode a niew annotation with point 9999 9999 */
            UserAnnotation annotationToEdit = UserAnnotation.get(annotationToAdd.id)
            def jsonEdit = annotationToEdit
            jsonAnnotation = jsonEdit.encodeAsJSON()
            def jsonUpdate = JSON.parse(jsonAnnotation)
            jsonUpdate.location = newGeom
            jsonUpdate.user = newUser.id
            jsonAnnotation = jsonUpdate.encodeAsJSON()
        } else if(annotation instanceof AlgoAnnotation) {
            AlgoAnnotation annotationToAdd = BasicInstance.createOrGetBasicAlgoAnnotation()
            annotationToAdd.location =  new WKTReader().read(oldGeom)
            annotationToAdd.user = oldUser
            assert (annotationToAdd.save(flush:true) != null)

            /* Encode a niew annotation with point 9999 9999 */
            AlgoAnnotation annotationToEdit = AlgoAnnotation.get(annotationToAdd.id)
            def jsonEdit = annotationToEdit
            jsonAnnotation = jsonEdit.encodeAsJSON()
            def jsonUpdate = JSON.parse(jsonAnnotation)
            jsonUpdate.location = newGeom
            jsonUpdate.user = newUser.id
            jsonAnnotation = jsonUpdate.encodeAsJSON()
        } else {
            throw new Exception("Type is not supported!")
        }
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

    static def fill(def id, String username, String password) {
        log.info "update annotation:" + id
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json?fill=true"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.put("")
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def correctAnnotation(def id, def data,String username, String password) {
        log.info "update annotation:" + id
        String URL = Infos.CYTOMINEURL + "api/annotationcorrection.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(data)
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
}
