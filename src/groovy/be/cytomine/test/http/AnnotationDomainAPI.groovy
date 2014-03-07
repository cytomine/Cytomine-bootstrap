package be.cytomine.test.http

import be.cytomine.AnnotationDomain
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.RoiAnnotation
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class AnnotationDomainAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + id + ".json"
        return doGET(URL, username, password)
    }


    static def listByProject(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json?project=$id"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        //String URL = Infos.CYTOMINEURL + "api/project/$id/userannotation.json?users="+idUser+"&images="+idImage
        String URL = Infos.CYTOMINEURL + "api/annotation.json?project=$id&users="+idUser+"&images="+idImage
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json?term=$idTerm&project=$idProject&users="+idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm,Long idImage, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json?term=$idTerm&project=$idProject&users="+idUser+"&offset=0&max=5&image=$idImage"
        return doGET(URL, username, password)
    }

    static def listByProjectAndTermWithSuggest(Long idProject, Long idTerm,Long idSuggest, Long idJob,String username, String password) {
        //String URL = Infos.CYTOMINEURL + "api/term/$idTerm/project/$idProject/annotation.json?suggestTerm="+idSuggest+"&job=$idJob"
        String URL = Infos.CYTOMINEURL + "api/annotation.json?term=$idTerm&project=$idProject&suggestedTerm=$idSuggest&jobForTermAlgo=$idJob"
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?user="+ idUser +"&image="+idImage
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersWithoutTerm(Long id,Long idUser, Long idImage,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?project=$id&noTerm=true&users=$idUser"+ (idImage? "&image="+idImage:"")
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersSeveralTerm(Long id,Long idUser, Long idImage,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?project=$id&multipleTerm=true&users=" +idUser + (idImage? "&image="+idImage:"")
        return doGET(URL, username, password)
    }


    static def downloadDocumentByProject(Long idProject,Long idUser, Long idTerm, Long idImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ idProject +"/annotation/download?users=" +idUser + "&terms=" + idTerm +"&images=" + idImageInstance + "&format=pdf"
        return doGET(URL, username, password)
    }

    static def downloadDocumentNewImplementation(Long idProject,Long idUser, Long idTerm, Long idImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation/method/download?project=$idProject&users=" +idUser + "&terms=" + idTerm +"&images=" + idImageInstance + "&format=pdf"
        return doGET(URL, username, password)
    }

    static def create(String jsonAnnotation, String username, String password, boolean roi = false) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json" + (roi? "?roi=true" : "")
        def result = doPOST(URL, jsonAnnotation,username, password)
        def json = JSON.parse(result.data)
        if(JSON.parse(jsonAnnotation) instanceof JSONArray) return result
        Long idAnnotation = json?.annotation?.id
        AnnotationDomain annotation = UserAnnotation.read(idAnnotation)
        if(!annotation)  annotation = AlgoAnnotation.read(idAnnotation)
        if(!annotation)  annotation = RoiAnnotation.read(idAnnotation)
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

    static def listIncluded(String geometry, Long idImage, Long idUser,List<Long> terms,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/imageinstance/$idImage/annotation/included.json?geometry=${geometry.replace(" ","%20")}" + (terms? "&terms=${terms.join(',')}" : "") + "&user=${idUser}"
        return doGET(URL, username, password)
    }

    static def listIncluded(AnnotationDomain annotation, Long idImage, Long idUser,List<Long> terms,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/imageinstance/$idImage/annotation/included.json?annotation=${annotation.id}" + (terms? "&terms=${terms.join(',')}" : "") + "&user=${idUser}"
        return doGET(URL, username, password)
    }

    static def downloadIncluded(AnnotationDomain annotation, Long idImage, Long idUser,List<Long> terms, String format, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/imageinstance/$idImage/annotation/included/download?format=$format&annotation=${annotation.id}" + (terms? "&terms=${terms.join(',')}" : "") + "&user=${idUser}"
        return doGET(URL, username, password)
    }

    static def downloadIncluded(String geometry, Long idImage, Long idUser,List<Long> terms,String format,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/imageinstance/$idImage/annotation/included/download?format=$format&geometry=${geometry.replace(" ","%20")}" + (terms? "&terms=${terms.join(',')}" : "") + "&user=${idUser}"
        return doGET(URL, username, password)
    }

    static def simplifyAnnotation(def id, def minPoint, def maxPoint, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation/$id/simplify.json?minPoint=$minPoint&maxPoint=$maxPoint"
        return doPUT(URL,"", username, password)
    }
}
