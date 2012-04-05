package be.cytomine

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance

import be.cytomine.test.Infos
import be.cytomine.test.HttpClient

import org.codehaus.groovy.grails.web.json.JSONArray

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import be.cytomine.test.http.AnnotationAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class GeneralTests extends functionaltestplugin.FunctionalTestCase {

    def aclService
    def aclUtilService
    def objectIdentityRetrievalStrategy
    def sessionFactory
    def springSecurityService

    void testCommandMaxSizeTooLong() {
        String jsonImage = "{\"text\" : \"*************************************************************************"
        String textAdded = "***************************************************************************************"
        textAdded = textAdded + textAdded + textAdded + textAdded + textAdded + textAdded + textAdded + textAdded + textAdded + textAdded
        //create a big string (don't care about content)
        while (jsonImage.size() <= (ConfigurationHolder.config.cytomine.maxRequestSize * 2)) {
            jsonImage += textAdded
        }
        jsonImage = jsonImage + "\"}"

        log.info("post with data size:" + jsonImage.size())
        String URL = Infos.CYTOMINEURL + "api/image.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonImage)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(413, code)
        def json = JSON.parse(response)
    }

    void testCommandMaxSizeOK() {
        log.info("create image")
        String jsonImage = "{\"text\" : \"*************************************************************************"
        String textAdded = "***************************************************************************************"
        jsonImage = jsonImage + "\"}"

        log.info("post with data size:" + jsonImage.size())
        String URL = Infos.CYTOMINEURL + "api/image.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonImage)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(true, code != 413)
        def json = JSON.parse(response)
    }

    void testLastAction() {
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        def result = AnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        int idAnnotation = result.data.id

        result = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = AnnotationAPI.undo()
        assertEquals(200, result.code)

        result = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)

        result = AnnotationAPI.redo()
        assertEquals(200, result.code)

        result = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        /*
        * Get the last 3 commands: it must be "REDO ADD ANNOTATION", "UNDO ADD ANNOTATION" and "ADD ANNOTATION"
        */
        Long idProject = annotationToAdd.image.project.id
        Integer max = 3
        HttpClient client = new HttpClient();
        String url = Infos.CYTOMINEURL + "api/project/" + idProject + "/last/" + max + ".json"
        client.connect(url, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)
        def json = JSON.parse(response)
        assert json instanceof JSONArray

    }
}
