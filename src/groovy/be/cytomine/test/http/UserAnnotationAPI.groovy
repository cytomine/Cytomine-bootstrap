package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.User
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
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation.json"
        return doGET(URL, username, password)
    }

    static def listByUser(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$id/userannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/userannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/userannotation.json?users="+idUser+"&images="+idImage
        return doGET(URL, username, password)
    }

    static def listByImage(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$id/userannotation.json"
        return doGET(URL, username, password)
    }

    static def listByTerm(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$id/userannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/userannotation.json?users="+idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm,Long idImage, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/userannotation.json?users="+idUser+"&offset=0&max=5"
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/userannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsers(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/userannotation.json?users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersWithoutTerm(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/userannotation.json?noTerm=true&users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersSeveralTerm(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/userannotation.json?multipleTerm=true&users=" +idUser
        return doGET(URL, username, password)
    }

    static def downloadDocumentByProject(Long idProject,Long idUser, Long idTerm, Long idImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ idProject +"/userannotation/download?users=" +idUser + "&terms=" + idTerm +"&images=" + idImageInstance + "&format=pdf"
        return doGET(URL, username, password)
    }

    static def create(String jsonAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation.json"
        def result = doPOST(URL,jsonAnnotation,username,password)
        def json = JSON.parse(result.data)
        if(JSON.parse(jsonAnnotation) instanceof JSONArray) return [code: result.code]
        println "json="+json
        Long idAnnotation = json?.annotation?.id
        return [data: UserAnnotation.get(idAnnotation), code: result.code]
    }

    static def update(def id, def jsonAnnotation, String username, String password) {
        log.info "update userannotation:" + id
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        return doPUT(URL,jsonAnnotation,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        return doDELETE(URL,username,password)
    }

    static def buildBasicUserAnnotation(String username, String password) {
        //Create project with user 1
        def result = ProjectAPI.create(BasicInstance.getBasicProjectNotExist().encodeAsJSON(), username, password)
        assert 200==result.code
        Project project = result.data

        //Add image with user 1
        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.project = project
        result = ImageInstanceAPI.create(image.encodeAsJSON(), username, password)
        assert 200==result.code
        image = result.data

        //Add annotation 1 with cytomine admin
        UserAnnotation annotation = BasicInstance.getBasicUserAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        result = UserAnnotationAPI.create(annotation.encodeAsJSON(), username, password)
        assert 200==result.code
        annotation = result.data
        return annotation
    }
}
