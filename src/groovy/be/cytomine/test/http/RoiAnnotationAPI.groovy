package be.cytomine.test.http

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.UserAnnotation
import be.cytomine.processing.RoiAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class RoiAnnotationAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/roiannotation/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        listByProject(id,false,username,password)
    }

    static def listByProject(Long id, boolean offset, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json?roi=true&project=$id" + (offset? "&offset=0&max=3":"")
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        //String URL = Infos.CYTOMINEURL + "api/project/$id/userannotation.json?users="+idUser+"&images="+idImage
        String URL = Infos.CYTOMINEURL + "api/annotation.json?roi=true&project=$id&users="+idUser+"&images="+idImage
        return doGET(URL, username, password)
    }

    static def listByImage(Long id, String username, String password, List propertiesToShow = null) {
        println "propertiesToShow=$propertiesToShow"
        String URL = Infos.CYTOMINEURL + "api/annotation.json?roi=true&image=$id&" + buildPropertiesToShowURLParams(propertiesToShow)
        println "url=$URL"
        return doGET(URL, username, password)
    }

    static def listByImages(Long project,List<Long> ids, String username, String password, List propertiesToShow = null) {
        println "propertiesToShow=$propertiesToShow"
        String URL = Infos.CYTOMINEURL + "api/annotation.json?roi=true&project=${project}&images=${ids.join(',')}&" + buildPropertiesToShowURLParams(propertiesToShow)
        println "url=$URL"
        return doGET(URL, username, password)
    }

    static def buildPropertiesToShowURLParams(List propertiesToShow) {

        println "propertiesToShow=$propertiesToShow"
        if(!propertiesToShow)  return ""
        def params = []
        propertiesToShow.each {
            params << it + "=true"
        }
        println "params=${params.join("&")}"
        return params.join("&")
    }
    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?roi=true&user="+ idUser +"&image="+idImage
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsers(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?roi=true&project=$id&users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String bbox, boolean netReviewedOnly,Integer force,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?roi=true&user=$idUser&image=$idImage&bbox=${bbox.replace(" ","%20")}&notReviewedOnly=$netReviewedOnly" + (force? "&kmeansValue=$force": "")
        return doGET(URL, username, password)
    }

    static def create(String jsonAnnotation,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/roiannotation.json?"
        println "jsonAnnotation="+jsonAnnotation
        def result = doPOST(URL,jsonAnnotation,username,password)
        println result
        def json = JSON.parse(result.data)
        if(JSON.parse(jsonAnnotation) instanceof JSONArray) return [code: result.code]
        println "json="+json
        Long idAnnotation = json?.roiannotation?.id
        return [data: RoiAnnotation.get(idAnnotation), code: result.code]
    }

    static def update(def id, def jsonAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/roiannotation/" + id + ".json"
        return doPUT(URL,jsonAnnotation,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/roiannotation/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
