package be.cytomine.test.http

import be.cytomine.ontology.SharedAnnotation
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage AnnotationComment to Cytomine with HTTP request during functional test
 */
class AnnotationCommentAPI extends DomainAPI {

    static def show(Long idAnnotation, Long idComment,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/$idAnnotation/comment/$idComment" + ".json"
        return doGET(URL, username, password)
    }

    static def list(Long idAnnotation,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/$idAnnotation/comment.json"
        return doGET(URL, username, password)
    }

    static def create(Long idAnnotation,String jsonAnnotationComment, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation/$idAnnotation/comment.json"
        def result = doPOST(URL, jsonAnnotationComment,username, password)
        result.data = SharedAnnotation.read(JSON.parse(result.data)?.sharedannotation?.id)
        return result
    }
}
