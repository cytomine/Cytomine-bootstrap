package be.cytomine.test.http

import be.cytomine.ontology.UserAnnotation
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class UserAnnotationAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        listByProject(id,false,username,password)
    }

    static def listByProject(Long id, boolean offset, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/userannotation.json" + (offset? "?offset=0&max=3":"")
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
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        return doPUT(URL,jsonAnnotation,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        return doDELETE(URL,username,password)
    }

}
