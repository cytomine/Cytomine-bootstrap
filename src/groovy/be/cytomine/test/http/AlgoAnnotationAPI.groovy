package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.User

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.security.UserJob

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class AlgoAnnotationAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation.json"
        return doGET(URL, username, password)
    }

    static def listByUser(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$id/algoannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/algoannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/algoannotation.json?users="+idUser+"&images="+idImage
        return doGET(URL, username, password)
    }

    static def listByImage(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$id/algoannotation.json"
        return doGET(URL, username, password)
    }

    static def listByTerm(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$id/algoannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/algoannotation.json?users="+idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm,Long idImage, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/algoannotation.json?users="+idUser+"&offset=0&max=5"
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/algoannotation.json"
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String bbox, boolean netReviewedOnly,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/algoannotation.json?bbox=$bbox&notreviewed=$netReviewedOnly"
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsers(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/algoannotation.json?users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersWithoutTerm(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/algoannotation.json?noTerm=true&users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersSeveralTerm(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/algoannotation.json?multipleTerm=true&users=" +idUser
        return doGET(URL, username, password)
    }

    static def downloadDocumentByProject(Long idProject,Long idUser, Long idTerm, Long idImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ idProject +"/algoannotation/download?users=" +idUser + "&terms=" + idTerm +"&images=" + idImageInstance + "&format=pdf"
        return doGET(URL, username, password)
    }

    static def create(String jsonAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation.json"
        def result = doPOST(URL, jsonAnnotation,username, password)
        if(JSON.parse(jsonAnnotation) instanceof JSONArray) return result
        result.data = AlgoAnnotation.read(JSON.parse(result.data)?.annotation?.id)
        return result
    }

    static def update(def id, def jsonAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/" + id + ".json"
        return doPUT(URL,jsonAnnotation,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/" + id + ".json"
        return doDELETE(URL,username, password)
    }

    static def copy(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/" + id + "/copy.json"
        def result = doPOST(URL,"",username, password)
        result.data = AlgoAnnotation.get(JSON.parse(result.data)?.annotation?.id)
        return result
    }

    static def union(def idImage, def idUser, def idTerm, def minIntersectionLength, def bufferLength, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/union.json?idImage=$idImage&idUser=$idUser&idTerm=$idTerm&minIntersectionLength=$minIntersectionLength&bufferLength=$bufferLength"
        return doPUT(URL,"",username,password)
    }



}
