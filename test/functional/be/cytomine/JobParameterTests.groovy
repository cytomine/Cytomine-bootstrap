package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.JobParameter

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class JobParameterTests extends functionaltestplugin.FunctionalTestCase {

    void testListJobParameterWithCredential() {

        log.info("list jobparameter")
        String URL = Infos.CYTOMINEURL + "api/jobparameter.json"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();

        log.info("check response=" + response)
        assertEquals(200, code)
        def json = JSON.parse(response)
        assert json instanceof JSONArray

    }

    void testListJobParameterWithoutCredential() {

        log.info("list jobparameter")
        String URL = Infos.CYTOMINEURL + "api/jobparameter.json"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.BADLOGIN, Infos.BADPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();

        log.info("check response")
        assertEquals(401, code)

    }

    void testShowJobparameterWithCredential() {

        JobParameter jobparameter = BasicInstance.createOrGetBasicJobParameter()

        log.info("list jobparameter")
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + jobparameter.id + ".json"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        def json = JSON.parse(response)
        assert json instanceof JSONObject

    }

    void testAddJobparameterCorrect() {

        log.info("create jobparameter")
        def jobparameterToAdd = BasicInstance.getBasicJobParameterNotExist()
        println("jobparameterToAdd.version=" + jobparameterToAdd.version)
        String jsonJobparameter = jobparameterToAdd.encodeAsJSON()

        log.info("post jobparameter:" + jsonJobparameter.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/jobparameter.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonJobparameter)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        def json = JSON.parse(response)
        assert json instanceof JSONObject
        int idJobparameter = json.jobparameter.id

        log.info("check if object " + idJobparameter + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

//        log.info("test undo")
//        client = new HttpClient()
//        URL = Infos.CYTOMINEURL + Infos.UNDOURL + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//        assertEquals(200, code)
//
//        log.info("check if object " + idJobparameter + " not exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//        assertEquals(404, code)
//
//        log.info("test redo")
//        client = new HttpClient()
//        URL = Infos.CYTOMINEURL + Infos.REDOURL + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//        assertEquals(200, code)
//
//        //must be done because redo change id
//        json = JSON.parse(response)
//        assert json instanceof JSONArray
//
//        log.info("check if object " + idJobparameter + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//        assertEquals(200, code)

    }

    void testAddJobparameterAlreadyExist() {
        log.info("create jobparameter")
        def jobparameterToAdd = BasicInstance.createOrGetBasicJobParameter()
        //jobparameterToAdd is save in DB
        String jsonJobparameter = jobparameterToAdd.encodeAsJSON()

        log.info("post jobparameter:" + jsonJobparameter.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/jobparameter.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonJobparameter)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(409, code)
    }

    void testUpdateJobparameterCorrect() {

        String oldValue = "Name1"
        String newValue = "Name2"

        def mapNew = ["value": newValue]
        def mapOld = ["value": oldValue]

        /* Create a Name1 jobparameter */
        log.info("create jobparameter")
        JobParameter jobparameterToAdd = BasicInstance.createOrGetBasicJobParameter()
        jobparameterToAdd.value = oldValue
        assert (jobparameterToAdd.save(flush: true) != null)

        /* Encode a niew jobparameter Name2*/
        JobParameter jobparameterToEdit = JobParameter.get(jobparameterToAdd.id)
        def jsonJobparameter = jobparameterToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJobparameter)
        jsonUpdate.value = newValue
        jsonJobparameter = jsonUpdate.encodeAsJSON()

        log.info("put jobparameter:" + jsonJobparameter.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + jobparameterToEdit.id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.put(jsonJobparameter)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        def json = JSON.parse(response)
        assert json instanceof JSONObject
        int idJobparameter = json.jobparameter.id

        log.info("check if object " + idJobparameter + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();

        assertEquals(200, code)
        json = JSON.parse(response)
        assert json instanceof JSONObject

        BasicInstance.compareJobParameter(mapNew, json)

//        log.info("test undo")
//        client = new HttpClient()
//        URL = Infos.CYTOMINEURL + Infos.UNDOURL + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//        assertEquals(200, code)
//
//        log.info("check if object " + idJobparameter + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//
//        assertEquals(200, code)
//        json = JSON.parse(response)
//        assert json instanceof JSONObject
//
//        BasicInstance.compareJobparameter(mapOld, json)
//
//        log.info("test redo")
//        client = new HttpClient()
//        URL = Infos.CYTOMINEURL + Infos.REDOURL + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//        assertEquals(200, code)
//
//        log.info("check if object " + idJobparameter + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//
//        assertEquals(200, code)
//        json = JSON.parse(response)
//        assert json instanceof JSONObject
//
//        BasicInstance.compareJobparameter(mapNew, json)
//
//
//        log.info("check if object " + idJobparameter + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//
//        assertEquals(200, code)
//        json = JSON.parse(response)
//        assert json instanceof JSONObject

    }

    void testUpdateJobparameterNotExist() {
        /* Create a Name1 jobparameter */
        log.info("create jobparameter")
        JobParameter jobparameterToAdd = BasicInstance.createOrGetBasicJobParameter()

        /* Encode a niew jobparameter Name2*/
        JobParameter jobparameterToEdit = JobParameter.get(jobparameterToAdd.id)
        def jsonJobparameter = jobparameterToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJobparameter)
        jsonUpdate.id = -99
        jsonJobparameter = jsonUpdate.encodeAsJSON()

        log.info("put jobparameter:" + jsonJobparameter.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/jobparameter/-99.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.put(jsonJobparameter)
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(404, code)
    }

    void testDeleteJobparameter() {

        log.info("create jobparameter")
        def jobparameterToDelete = BasicInstance.getBasicJobParameterNotExist()
        assert jobparameterToDelete.save(flush: true) != null
        String jsonJobparameter = jobparameterToDelete.encodeAsJSON()
        int idJobparameter = jobparameterToDelete.id
        log.info("delete jobparameter:" + jsonJobparameter.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.delete()
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)

        log.info("check if object " + idJobparameter + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        client.disconnect();

        assertEquals(404, code)

//        log.info("test undo")
//        client = new HttpClient()
//        URL = Infos.CYTOMINEURL + Infos.UNDOURL + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        client.get()
//        code = client.getResponseCode()
//        String response = client.getResponseData()
//        client.disconnect();
//        assertEquals(200, code)
//
//        log.info("check if object " + idJobparameter + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//
//        assertEquals(200, code)
//
//
//        log.info("test redo")
//        client = new HttpClient()
//        URL = Infos.CYTOMINEURL + Infos.REDOURL + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        client.get()
//        code = client.getResponseCode()
//        client.disconnect();
//        assertEquals(200, code)
//
//        log.info("check if object " + idJobparameter + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/jobparameter/" + idJobparameter + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
//        client.get()
//        code = client.getResponseCode()
//        client.disconnect();
//        assertEquals(404, code)

    }

    void testDeleteJobparameterNotExist() {

        log.info("create jobparameter")
        def jobparameterToDelete = BasicInstance.createOrGetBasicJobParameter()
        String jsonJobparameter = jobparameterToDelete.encodeAsJSON()

        log.info("delete jobparameter:" + jsonJobparameter.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/jobparameter/-99.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.delete()
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(404, code)

    }
}
