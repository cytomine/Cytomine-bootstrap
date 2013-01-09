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
        String URL = Infos.CYTOMINEURL + "api/annotation/" + idAnnotation + "/term/"+ idTerm +"/user/"+idUser+".json"
        return doGET(URL, username, password)
    }

    static def listAnnotationTermByAnnotation(Long idAnnotation,String username, String password) {
        listAnnotationTermByAnnotation(idAnnotation,null,username,password)
    }

    static def listAnnotationTermByAnnotation(Long idAnnotation,Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation+"/term.json" + (idUser!=null? "?idUser=$idUser":"")
        return doGET(URL, username, password)
    }

    static def listAnnotationTermByUserNot(Long idAnnotation, Long idNotUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation/"+idAnnotation+"/notuser/" + idNotUser + "/term.json"
        return doGET(URL, username, password)
    }

    static def createAnnotationTerm(String jsonAnnotationTerm, String username, String password) {
        createAnnotationTerm(jsonAnnotationTerm,username,password,false)
    }

    static def createAnnotationTerm(String jsonAnnotationTerm, String username, String password, boolean deleteOldTerm) {
        def json = JSON.parse(jsonAnnotationTerm);
        String URL
        if(deleteOldTerm)
            URL=Infos.CYTOMINEURL+"api/annotation/"+ json.userannotation +"/term/"+ json.term +"/clearBefore.json"
        else  URL=Infos.CYTOMINEURL+"api/annotation/"+ json.userannotation +"/term/"+ json.term +".json"
        def result = doPOST(URL,jsonAnnotationTerm,username,password)
        json = JSON.parse(result.data)
        int idAnnotationTerm
        println "json=$json"
        try {idAnnotationTerm= json?.annotationterm?.id } catch(Exception e) {log.error e}
        return [data: AnnotationTerm.get(idAnnotationTerm), code: result.code]
    }

    static def deleteAnnotationTerm(def idAnnotation, def idTerm, def idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + idAnnotation + "/term/"+ idTerm +"/user/"+idUser+".json"
        return doDELETE(URL,username,password)
    }

    static def showAlgoAnnotationTerm(Long idAnnotation,Long idTerm, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/" + idAnnotation + "/term/"+ idTerm +"/user/"+idUser+".json"
        return doGET(URL,username,password)
    }

    static def createAlgoAnnotationTerm(String jsonAnnotationTerm, String username, String password) {
        def json = JSON.parse(jsonAnnotationTerm);
        String URL = Infos.CYTOMINEURL+"api/annotation/"+ json.userannotation +"/term/"+ json.term +".json"
        def result = doPOST(URL,jsonAnnotationTerm,username,password)
        json = JSON.parse(result.data)
        int idAlgoAnnotationTerm
        try {idAlgoAnnotationTerm= json?.algoannotationterm?.id } catch(Exception e) {log.error e}
        return [data: AnnotationTerm.get(idAlgoAnnotationTerm), code: result.code]
    }
}
