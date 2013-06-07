package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class AlgoAnnotationAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/search.json?project=$id&users="+idUser+"&images="+idImage
        return doGET(URL, username, password)
    }

    static def listByTerm(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/search.json?term=$id"
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm, Long idUser,String username, String password) {
        //String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/algoannotation.json?users="+idUser
        String URL = Infos.CYTOMINEURL + "api/annotation/search.json?term=$idTerm&project=$idProject&users="+idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm,List<ImageInstance> images,Long idUser,String username, String password) {
        //String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/algoannotation.json?users="+idUser+"&offset=0&max=5&images=${images.collect{it.id}.join(",")}"
        String URL = Infos.CYTOMINEURL + "api/annotation/search.json?term=$idTerm&project=$idProject&users="+idUser+"&offset=0&max=5&images=${images.collect{it.id}.join(",")}"
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm,Long idImage, Long idUser,String username, String password) {
        //String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/algoannotation.json?users="+idUser+"&offset=0&max=5&images=${idImage}"
        String URL = Infos.CYTOMINEURL + "api/annotation/search.json?term=$idTerm&project=$idProject&users="+idUser+"&offset=0&max=5&images=${idImage}"
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        //String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/algoannotation.json"
        String URL = Infos.CYTOMINEURL+"api/annotation/search.json?user=$idUser&image=$idImage"
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String bbox, boolean netReviewedOnly,String username, String password) {
        //String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/algoannotation.json?bbox=$bbox&notreviewed=$netReviewedOnly"
        String URL = Infos.CYTOMINEURL+"api/annotation/search.json?user=$idUser&image=$idImage&bbox=$bbox&notReviewedOnly=$netReviewedOnly"
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String bbox, boolean netReviewedOnly,Integer force,String username, String password) {
//        String URL = Infos.CYTOMINEURL+"api/user/"+ idUser +"/imageinstance/"+idImage+"/algoannotation.json?bbox=$bbox&notreviewed=$netReviewedOnly" + (force? "&force="+force:"")
        String URL = Infos.CYTOMINEURL+"api/annotation/search.json?user=$idUser&image=$idImage&bbox=$bbox&notReviewedOnly=$netReviewedOnly" + (force? "&kmeansValue="+force:"")
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsers(Long id,Long idUser, String username, String password) {
//        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/algoannotation.json?users=" +idUser
        String URL = Infos.CYTOMINEURL+"api/annotation/search.json?project=$id&users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersWithoutTerm(Long id,Long idUser, String username, String password) {
//        String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/algoannotation.json?noTerm=true&users=" +idUser
        String URL = Infos.CYTOMINEURL+"api/annotation/search.json?project=$id&noTerm=true&users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersSeveralTerm(Long id,Long idUser, String username, String password) {
        //String URL = Infos.CYTOMINEURL+"api/project/"+ id +"/algoannotation.json?multipleTerm=true&users=" +idUser
        String URL = Infos.CYTOMINEURL+"api/annotation/search.json?project=$id&multipleTerm=true&users=" +idUser
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

    static def union(def idImage, def idUser, def idTerm, def minIntersectionLength, def bufferLength, def area, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/algoannotation/union.json?idImage=$idImage&idUser=$idUser&idTerm=$idTerm&minIntersectionLength=$minIntersectionLength&bufferLength=$bufferLength&area=$area"
        return doPUT(URL,"",username,password)
    }

}
