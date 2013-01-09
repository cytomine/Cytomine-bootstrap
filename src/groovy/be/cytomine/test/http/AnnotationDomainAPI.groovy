package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.security.UserJob

import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.AnnotationDomain
import be.cytomine.ontology.UserAnnotation
import be.cytomine.social.SharedAnnotation

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class AnnotationDomainAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json"
        return doGET(URL, username, password)
    }

    static def listByUser(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/user/$id/annotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/annotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/annotation.json?users="+idUser+"&images="+idImage
        return doGET(URL, username, password)
    }

    static def listByTerm(Long idTerm, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/annotation.json"
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/annotation.json?users="+idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm,Long idImage, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/annotation.json?users="+idUser+"&offset=0&max=5"
        return doGET(URL, username, password)
    }

    static def listByProjectAndTermWithSuggest(Long idProject, Long idTerm,Long idSuggest, Long idJob,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/annotation.json?suggestTerm="+idSuggest+"&job=$idJob"
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/annotation.json"
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsers(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/annotation.json?users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersWithoutTerm(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/annotation.json?noTerm=true&users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersSeveralTerm(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/annotation.json?multipleTerm=true&users=" +idUser
        return doGET(URL, username, password)
    }

    static def downloadDocumentByProject(Long idProject,Long idUser, Long idTerm, Long idImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ idProject +"/annotation/download?users=" +idUser + "&terms=" + idTerm +"&images=" + idImageInstance + "&format=pdf"
        return doGET(URL, username, password)
    }

    static def create(String jsonAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json"
        def result = doPOST(URL, jsonAnnotation,username, password)
        def json = JSON.parse(result.data)
        if(JSON.parse(jsonAnnotation) instanceof JSONArray) return result
        Long idAnnotation = json?.annotation?.id
        AnnotationDomain annotation = UserAnnotation.read(idAnnotation)
        if(!annotation)  annotation = AlgoAnnotation.read(idAnnotation)
        result.data = annotation
        return result
    }

    static def update(def id, def jsonAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json"
        return doPUT(URL,jsonAnnotation,username,password)
    }

    static def fill(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json?fill=true"
        return doPUT(URL,"",username,password)
    }

    static def correctAnnotation(def id, def data,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotationcorrection.json"
        return doPOST(URL,data,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
