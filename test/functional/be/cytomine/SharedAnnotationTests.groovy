package be.cytomine

import be.cytomine.security.User
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationCommentAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class SharedAnnotationTests  {

    void testGetAnnotationCommentWithCredential() {
        def sharedAnnotation = BasicInstanceBuilder.getSharedAnnotation()
        def result = AnnotationCommentAPI.show(sharedAnnotation.userAnnotation.id, sharedAnnotation.id, Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListAnnotationCommentsByAnnotationWithCredential() {
        def sharedAnnotation = BasicInstanceBuilder.getSharedAnnotation()
        def result = AnnotationCommentAPI.list(sharedAnnotation.userAnnotation.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = AnnotationCommentAPI.list(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

//    void testAddAnnotationComments() {
//        def sharedAnnotation = BasicInstanceBuilder.getSharedAnnotationNotExist()
//        def json = JSON.parse((String)sharedAnnotation.encodeAsJSON())
//        json.subject = "subject for test mail"
//        json.message = "message for test mail"
//        json.users = [BasicInstanceBuilder.getUser1().id]
//        def result = AnnotationCommentAPI.create(sharedAnnotation.userAnnotation.id,json.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
//        assert 200 == result.code
//    }
}
