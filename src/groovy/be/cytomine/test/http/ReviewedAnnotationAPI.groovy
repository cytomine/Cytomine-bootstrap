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
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.Term
import be.cytomine.security.SecUser

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class ReviewedAnnotationAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/reviewedannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/reviewedannotation.json?users="+idUser
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/reviewedannotation.json?users="+idUser+"&images="+idImage
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long idUser, Long idImage, Long idTerm, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/project/$id/reviewedannotation.json?users="+idUser+"&images="+idImage+"&terms="+idTerm
        return doGET(URL, username, password)
    }

    static def listByImageAndTerm(Long idImage, Long idTerm, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/imageinstance/$idImage/reviewedannotation.json"
        return doGET(URL, username, password)
    }

    static def listByImage(Long id,String username, String password) {
        listByImage(id,null,username,password)
    }

    static def listByImage(Long id, String bbox, String username,String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$id/reviewedannotation.json" + (bbox? "?bbox=$bbox" : "")
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/reviewedannotation.json?conflict=true"
        return doGET(URL, username, password)
    }

    static def listByImageAndUserAndBBOX(Long idImage,Long idUser, String bbox, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/reviewedannotation.json?bbox=$bbox"
        return doGET(URL, username, password)
    }

    static def create(String jsonAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation.json"
        def result = doPOST(URL,jsonAnnotation,username,password)
        def json = JSON.parse(result.data)
        if(JSON.parse(jsonAnnotation) instanceof JSONArray) return [code: result.code]
        Long idAnnotation = json?.reviewedannotation?.id
        return [data: ReviewedAnnotation.get(idAnnotation), code: result.code]
    }

    static def update(def id, def jsonAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/" + id + ".json"
        return doPUT(URL,jsonAnnotation,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/" + id + ".json"
        return doDELETE(URL,username,password)
    }

    static def markStartReview(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + "/review.json"
        return doPOST(URL,"",username,password)
    }

    static def markStopReview(def id, String username, String password) {
        markStopReview(id,false,username,password)
    }

    static def markStopReview(def id, boolean cancel, String username,String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + "/review.json" + (cancel? "?cancel=true": "")
        return doDELETE(URL,username,password)
    }

    static def addReviewAnnotation(def id, String username, String password) {
        addReviewAnnotation(id,null,username,password)
    }

    static def addReviewAnnotation(def id, def terms, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + "/review.json"
        return doPOST(URL,(!terms? "" : "{ \"terms\":[${terms.join(",")}]}"),username,password)
    }

    static def addReviewAll(Long idImage, List<Long> users, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + idImage + "/annotation/review.json?users="+users.join(",")
        return doPOST(URL,"",username,password)
    }

    static def deleteReviewAll(Long idImage, List<Long> users, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + idImage + "/annotation/review.json?users="+users.join(",")
        return doDELETE(URL,username,password)
    }

    static def removeReviewAnnotation(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + "/review.json"
        return doDELETE(URL,username,password)
    }

}
