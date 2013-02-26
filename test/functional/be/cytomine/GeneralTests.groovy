package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import be.cytomine.test.http.UserAnnotationAPI
import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.ontology.UserAnnotation

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class GeneralTests  {

    def aclService
    def aclUtilService
    def objectIdentityRetrievalStrategy
    def sessionFactory
    def springSecurityService

    void testUIViewPortToXMLConversion() {
        try{
            ViewPortToBuildXML.process()
        } catch(Exception e) {
            log.error e
            fail()
        }
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
        assert code != 413
        def json = JSON.parse(response)
    }

    void testLastAction() {
        def annotationToAdd = BasicInstance.createOrGetBasicUserAnnotation()

        def result = UserAnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        int idAnnotation = result.data.id

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = UserAnnotationAPI.undo()
        assert 200 == result.code

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = UserAnnotationAPI.redo()
        assert 200 == result.code

        result = UserAnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

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
        assert 200 == code
        def json = JSON.parse(response)
        assert json.collection instanceof JSONArray

    }

    void testLastActionProjectNotExist() {
        HttpClient client = new HttpClient();
        String url = Infos.CYTOMINEURL + "api/project/-99/last/10.json"
        client.connect(url, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        client.disconnect();
        assert 404 == code
    }

    void testMultipleAuthConnexion() {
        BasicInstance.createOrGetBasicUserAnnotation()
        UserAnnotation annotation = UserAnnotation.list().first()

        log.info "show userannotation " + annotation.id
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + annotation.id + ".json"
        HttpClient client1 = new HttpClient();
        client1.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client1.get()
        int code = client1.getResponseCode()
        String response = client1.getResponseData()
        assert code == 200

        HttpClient client2 = new HttpClient();
        client2.connect(URL, Infos.ANOTHERLOGIN, Infos.ANOTHERPASSWORD);
        client2.get()
        code = client2.getResponseCode()
        assert code == 200
        client1.disconnect()

        client1.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client1.get()
        code = client1.getResponseCode()
        assert code == 200

        client2.disconnect();
        client2.connect(URL, Infos.ANOTHERLOGIN, Infos.ANOTHERPASSWORD);
        client2.get()
        code = client2.getResponseCode()
        assert code == 200

        client1.disconnect();
        client2.disconnect();
    }

    void testPing() {
        HttpClient client = new HttpClient();
        String url = Infos.CYTOMINEURL + "server/ping.json"
        client.connect(url, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        def json = '{"project": "' + BasicInstance.createOrGetBasicProject().id + '"}'
        client.post(json)
        int code = client.getResponseCode()
        client.disconnect();
        assert 200 == code
    }

}
