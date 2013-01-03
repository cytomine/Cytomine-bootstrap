package be.cytomine.test.http

import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.security.User
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage AnnotationTerm to Cytomine with HTTP request during functional test
 */
class AnnotationTermAPI extends DomainAPI {

    private static final log = LogFactory.getLog(this)

    static def showAnnotationTerm(Long idAnnotation,Long idTerm, Long idUser,String username, String password) {
        log.info "Show idAnnotation=$idAnnotation and idTerm=$idTerm"
        String URL = Infos.CYTOMINEURL + "api/annotation/" + idAnnotation + "/term/"+ idTerm +"/user/"+idUser+".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listAnnotationTermByAnnotation(Long idAnnotation,String username, String password) {
        listAnnotationTermByAnnotation(idAnnotation,null,username,password)
    }

    static def listAnnotationTermByAnnotation(Long idAnnotation,Long idUser,String username, String password) {
        log.info "List by userAnnotation idAnnotation=$idAnnotation"
        String URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation+"/term.json" + (idUser!=null? "?idUser=$idUser":"")

        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def listAnnotationTermByUserNot(Long idAnnotation, Long idNotUser, String username, String password) {
        log.info "List by userAnnotation idAnnotation=$idAnnotation"
        String URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation+"/notuser/" + idNotUser + "/term.json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }



    static def createAnnotationTerm(AnnotationTerm annotationTermToAdd, User user) {
       createAnnotationTerm(annotationTermToAdd.encodeAsJSON(),user.username,user.password)
    }

    static def createAnnotationTerm(AnnotationTerm annotationTermToAdd, String username, String password) {
        return createAnnotationTerm(annotationTermToAdd.encodeAsJSON(), username, password)
    }

    static def createAnnotationTerm(String jsonAnnotationTerm, User user) {
        createAnnotationTerm(jsonAnnotationTerm,user.username,user.password)
    }

    static def createAnnotationTerm(String jsonAnnotationTerm, String username, String password) {
        log.info("create")
        createAnnotationTerm(jsonAnnotationTerm,username,password,false)
    }

    static def createAnnotationTerm(String jsonAnnotationTerm, String username, String password, boolean deleteOldTerm) {
        log.info("create")
        def json = JSON.parse(jsonAnnotationTerm);
        String URL = ""
        if(deleteOldTerm)
            URL=Infos.CYTOMINEURL+"api/annotation/"+ json.userannotation +"/term/"+ json.term +"/clearBefore.json"
        else  URL=Infos.CYTOMINEURL+"api/annotation/"+ json.userannotation +"/term/"+ json.term +".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonAnnotationTerm)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        json = JSON.parse(response)
        int idAnnotationTerm
        try {idAnnotationTerm= json?.annotationterm?.id } catch(Exception e) {log.error e}
        return [data: AnnotationTerm.get(idAnnotationTerm), code: code]
    }

    static def deleteAnnotationTerm(def idAnnotation, def idTerm, def idUser, String username, String password) {
        log.info("delete")
        String URL = Infos.CYTOMINEURL + "api/annotation/" + idAnnotation + "/term/"+ idTerm +"/user/"+idUser+".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def showAlgoAnnotationTerm(Long idAnnotation,Long idTerm, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + idAnnotation + "/term/"+ idTerm +"/user/"+idUser+".json"
        HttpClient client = new HttpClient();
        client.connect(URL, username, password);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return [data: response, code: code]
    }

    static def createAlgoAnnotationTerm(AnnotationTerm annotationTermToAdd, User user) {
       createAnnotationTerm(annotationTermToAdd.encodeAsJSON(),user.username,user.password)
    }

    static def createAlgoAnnotationTerm(AnnotationTerm annotationTermToAdd, String username, String password) {
        return createAnnotationTerm(annotationTermToAdd.encodeAsJSON(), username, password)
    }

    static def createAlgoAnnotationTerm(String jsonAnnotationTerm, User user) {
        createAlgoAnnotationTerm(jsonAnnotationTerm,user.username,user.password)
    }

    static def createAlgoAnnotationTerm(String jsonAnnotationTerm, String username, String password) {
        log.info("create algoannotationterm")
        def json = JSON.parse(jsonAnnotationTerm);
        String URL = Infos.CYTOMINEURL+"api/annotation/"+ json.userannotation +"/term/"+ json.term +".json"
        HttpClient client = new HttpClient()
        client.connect(URL, username, password)
        client.post(jsonAnnotationTerm)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();
        log.info("check response")
        json = JSON.parse(response)
        int idAnnotationTerm
        try {idAnnotationTerm= json?.algoannotationterm?.id } catch(Exception e) {log.error e}
        return [data: AlgoAnnotationTerm.get(idAnnotationTerm), code: code]
    }




}
