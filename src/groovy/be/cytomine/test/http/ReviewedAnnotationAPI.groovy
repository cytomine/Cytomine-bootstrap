package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
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
        log.info "show reviewedannotation " + id
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/" + id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def list(String username, String password) {
        log.info "list reviewedannotation"
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id, String username, String password) {
        log.info "list reviewedannotation by project " + id
        String URL = Infos.CYTOMINEURL + "api/project/$id/reviewedannotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id, Long idUser, String username, String password) {
        log.info "list reviewedannotation by project " + id
        String URL = Infos.CYTOMINEURL + "api/project/$id/reviewedannotation.json?users="+idUser
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        log.info "list reviewedannotation by project " + id
        String URL = Infos.CYTOMINEURL + "api/project/$id/reviewedannotation.json?users="+idUser+"&images="+idImage
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByProject(Long id, Long idUser, Long idImage, Long idTerm, String username, String password) {
        log.info "list reviewedannotation by project " + id
        String URL = Infos.CYTOMINEURL + "api/project/$id/reviewedannotation.json?users="+idUser+"&images="+idImage+"&terms="+idTerm
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByImageAndTerm(Long idImage, Long idTerm, String username, String password) {
        log.info "list listByImageAndTerm"
        String URL = Infos.CYTOMINEURL + "api/term/$idTerm/imageinstance/$idImage/reviewedannotation.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }


    static def listByImage(Long id,String username, String password) {
        listByImage(id,null,username,password)
    }

    static def listByImage(Long id, String bbox, String username,String password) {
        log.info "list reviewedannotation by image " + id
        String URL = Infos.CYTOMINEURL + "api/imageinstance/$id/reviewedannotation.json" + (bbox? "?bbox=$bbox" : "")
        log.info URL
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        log.info "list reviewedannotation by user " + idUser + " and image " + idImage
        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/reviewedannotation.json?conflict=true"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def create(ReviewedAnnotation annotationToAdd, User user) {
       create(annotationToAdd.encodeAsJSON(),user.username,user.password)
    }

    static def create(ReviewedAnnotation annotationToAdd, String username, String password) {
        return create(annotationToAdd.encodeAsJSON(), username, password)
    }

    static def create(String jsonAnnotation, User user) {
        create(jsonAnnotation,user.username,user.password)
    }

    static def create(String jsonAnnotation, String username, String password) {
        log.info("post reviewedannotation:" + jsonAnnotation.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation.json"
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
        Long idAnnotation = json?.reviewedannotation?.id
        return [data: ReviewedAnnotation.get(idAnnotation), code: code]
    }

//    static def create(Long job, String username, String password) {
//        log.info("post reviewedannotation for all job from :" + job)
//        String URL = Infos.CYTOMINEURL + "api/job/$job/reviewedannotation.json"
//        HttpClient client = new HttpClient()
//        client.connect(URL, username, password)
//        client.post("")
//        int code = client.getResponseCode()
//        String response = client.getResponseData()
//        println response
//        client.disconnect();
//        log.info("check response")
//        def json = JSON.parse(response)
//        Long idAnnotation = json?.reviewedannotation?.id
//        return [data: ReviewedAnnotation.get(idAnnotation), code: code]
//    }

    static def update(ReviewedAnnotation annotation, String username, String password) {
        log.info "update reviewedannotation:" + annotation

        String oldGeom = "POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"
        String newGeom = "POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))"

        User oldUser = annotation.user
        User newUser = annotation.user

        Term oldTerm = BasicInstance.createOrGetBasicTerm()
        Term newTerm = BasicInstance.getBasicTermNotExist()
        newTerm.save(flush: true)

        def mapNew = ["geom":newGeom,"user":newUser,"term":newTerm]
        def mapOld = ["geom":oldGeom,"user":oldUser,"term":oldTerm]

        /* Create a old annotation with point 1111 1111 */
        log.info("create reviewedannotation")
        ReviewedAnnotation annotationToAdd = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotationToAdd.location =  new WKTReader().read(oldGeom)
        annotationToAdd.user = oldUser
        annotationToAdd.addToTerm(oldTerm)
        assert (annotationToAdd.save(flush:true) != null)

        /* Encode a niew annotation with point 9999 9999 */
        ReviewedAnnotation annotationToEdit = ReviewedAnnotation.get(annotationToAdd.id)
        def jsonEdit = annotationToEdit
        def jsonAnnotation = jsonEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotation)
        jsonUpdate.location = newGeom
        jsonUpdate.user = newUser.id
        jsonUpdate.term = [newTerm.id]
        jsonAnnotation = jsonUpdate.encodeAsJSON()

        def data = update(annotation.id, jsonAnnotation, username, password)
        data.mapNew = mapNew
        data.mapOld = mapOld
        return data
    }

    static def update(def id, def jsonAnnotation, String username, String password) {
        log.info "update reviewedannotation:" + id
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/" + id + ".json"
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
        log.info "delete reviewedannotation:" + id
        String URL = Infos.CYTOMINEURL + "api/reviewedannotation/" + id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def markStartReview(def id, String username, String password) {
        log.info "update reviewedannotation:" + id
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + "/review.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post("")
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def markStopReview(def id, String username, String password) {
        markStopReview(id,false,username,password)
    }

    static def markStopReview(def id, boolean cancel, String username,String password) {
        log.info "update reviewedannotation:" + id
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + id + "/review.json" + (cancel? "?cancel=true": "")
        log.info URL
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def addReviewAnnotation(def id, String username, String password) {
        addReviewAnnotation(id,null,username,password)
    }

    static def addReviewAnnotation(def id, def terms, String username, String password) {
        log.info "update reviewedannotation:" + id
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + "/review.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        if(!terms) client.post("")
        else client.post("{ \"terms\":[${terms.join(",")}]}")
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }


    static def addReviewAll(Long idImage, List<Long> users, String username, String password) {
        log.info "update reviewedannotation:" + idImage
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + idImage + "/annotation/review.json?users="+users.join(",")
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post("")
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def deleteReviewAll(Long idImage, List<Long> users, String username, String password) {
        log.info "update reviewedannotation:" + idImage
        String URL = Infos.CYTOMINEURL + "api/imageinstance/" + idImage + "/annotation/review.json?users="+users.join(",")
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def removeReviewAnnotation(def id, String username, String password) {
        log.info "update reviewedannotation:" + id
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + "/review.json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        return [data: response, code: code]
    }

    static def buildBasicReviewedAnnotation(String username, String password) {
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
        ReviewedAnnotation annotation = BasicInstance.getBasicReviewedAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        result = ReviewedAnnotationAPI.create(annotation, username, password)
        assert 200==result.code
        annotation = result.data
        return annotation
    }




}
