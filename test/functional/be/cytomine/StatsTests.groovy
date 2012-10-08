package be.cytomine

import be.cytomine.processing.Job
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class StatsTests extends functionaltestplugin.FunctionalTestCase {

    void testRetrievalAVG() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()

        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=${job.id}"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assertEquals(200,code)
        def json = JSON.parse(response)
    }

    void testRetrievalConfusionMatrix() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()

        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/confusionmatrix.json?job=${job.id}"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assertEquals(200,code)
        def json = JSON.parse(response)
    }

    void testRetrievalWorstTerm() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()

        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTerm.json?job=${job.id}"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assertEquals(200,code)
        def json = JSON.parse(response)
    }

    void testRetrievalWorstAnnotation() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()

        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstAnnotation.json?job=${job.id}"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assertEquals(200,code)
        def json = JSON.parse(response)

    }

    void testRetrievalWorstTermWithSuggest() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()

        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTermWithSuggest.json?job=${job.id}"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assertEquals(200,code)
        def json = JSON.parse(response)
    }

    void testRetrievalEvolution() {
        Job job = BasicInstance.createJobWithAlgoAnnotationTerm()

        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/evolution.json?job=${job.id}"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assertEquals(200,code)
        def json = JSON.parse(response)
    }

/**
    *
            "/api/stats/retrieval/confusionmatrix"(controller:"stats"){
                action = [GET:"statRetrievalConfusionMatrix"]
            }
            "/api/stats/retrieval/worstTerm"(controller:"stats"){
                action = [GET:"statRetrievalWorstTerm"]
            }
            "/api/stats/retrieval/worstTermWithSuggest"(controller:"stats"){
                action = [GET:"statWorstTermWithSuggestedTerm"]
            }
            "/api/stats/retrieval/worstAnnotation"(controller:"stats"){
                action = [GET:"statRetrievalWorstAnnotation"]
            }
            "/api/stats/retrieval/evolution"(controller:"stats"){
                action = [GET:"statRetrievalEvolution"]
            }
            "/api/downloadPDF" (controller : "stats") {
                action = [GET:"convertHtmlContentToPDF"]
            }
  */

}
