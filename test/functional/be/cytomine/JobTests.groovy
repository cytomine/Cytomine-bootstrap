package be.cytomine

import be.cytomine.processing.Software
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.Job

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class JobTests extends functionaltestplugin.FunctionalTestCase {

    void testListJobWithCredential() {

        log.info("list job")
        String URL = Infos.CYTOMINEURL + "api/job.json"
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

    void testListJobBySoftwareWithCredential() {

        Job job = BasicInstance.createOrGetBasicJob()
        log.info("list job")
        String URL = Infos.CYTOMINEURL + "api/software/" + job.software.id +"/job.json"
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

    void testListJobBySoftwareAndProjectWithCredential() {

        Job job = BasicInstance.createOrGetBasicJob()
        log.info("list job with software="+job.software.id +" project="+job.project)
        String URL = Infos.CYTOMINEURL + "api/software/" + job.software.id +"/project/" + job.project.id +"/job.json"
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

    void testListJobWithoutCredential() {

        log.info("list job")
        String URL = Infos.CYTOMINEURL + "api/job.json"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.BADLOGIN, Infos.BADPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();

        log.info("check response")
        assertEquals(401, code)

    }

    void testShowJobWithCredential() {

        Job job = BasicInstance.createOrGetBasicJob()

        log.info("list job")
        String URL = Infos.CYTOMINEURL + "api/job/" + job.id + ".json"
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

    void testAddJobCorrect() {

//        log.info("create job")
//        def jobToAdd = BasicInstance.getBasicJobNotExist()
//        println("jobToAdd.version=" + jobToAdd.version)
//        String jsonJob = jobToAdd.encodeAsJSON()
//
//        log.info("post job:" + jsonJob.replace("\n", ""))
//        String URL = Infos.CYTOMINEURL + "api/job.json"
//        HttpClient client = new HttpClient()
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        client.post(jsonJob)
//        int code = client.getResponseCode()
//        String response = client.getResponseData()
//        println response
//        client.disconnect();
//
//        log.info("check response")
//        assertEquals(200, code)
//        def json = JSON.parse(response)
//        assert json instanceof JSONObject
//        int idJob = json.job.id
//
//        log.info("check if object " + idJob + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/job/" + idJob + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
//        client.get()
//        code = client.getResponseCode()
//        response = client.getResponseData()
//        client.disconnect();
//        assertEquals(200, code)

    }

    void testAddJobWithBadSoftware() {
        log.info("create job")
        def jobToAdd = BasicInstance.getBasicJobNotExist()
        String jsonJob = jobToAdd.encodeAsJSON()

        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.software = null
        jsonJob = jsonUpdate.encodeAsJSON()

        log.info("post job:" + jsonJob.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/job.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonJob)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(400, code)
    }

    void testUpdateJobCorrect() {

        Integer oldProgress = 0
        Integer newProgress = 100

        def mapNew = ["progress": newProgress]
        def mapOld = ["progress": oldProgress]

        /* Create a Name1 job */
        log.info("create job")
        Job jobToAdd = BasicInstance.createOrGetBasicJob()
        jobToAdd.progress = oldProgress
        assert (jobToAdd.save(flush: true) != null)

        /* Encode a niew job Name2*/
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.progress = newProgress
        jsonJob = jsonUpdate.encodeAsJSON()

        log.info("put job:" + jsonJob.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/job/" + jobToEdit.id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.put(jsonJob)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        def json = JSON.parse(response)
        assert json instanceof JSONObject
        int idJob = json.job.id

        log.info("check if object " + idJob + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/job/" + idJob + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();

        assertEquals(200, code)
        json = JSON.parse(response)
        assert json instanceof JSONObject

        BasicInstance.compareJob(mapNew, json)

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
//        log.info("check if object " + idJob + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/job/" + idJob + ".json"
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
//        BasicInstance.compareJob(mapOld, json)
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
//        log.info("check if object " + idJob + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/job/" + idJob + ".json"
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
//        BasicInstance.compareJob(mapNew, json)
//
//
//        log.info("check if object " + idJob + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/job/" + idJob + ".json"
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

    void testUpdateJobNotExist() {
        /* Create a Name1 job */
        log.info("create job")
        Job jobToAdd = BasicInstance.createOrGetBasicJob()

        /* Encode a niew job Name2*/
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.id = -99
        jsonJob = jsonUpdate.encodeAsJSON()

        log.info("put job:" + jsonJob.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/job/-99.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.put(jsonJob)
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(404, code)
    }

    void testUpdateJobWithBadSoftware() {

        /* Create a Name1 job */
        log.info("create job")
        Job jobToAdd = BasicInstance.createOrGetBasicJob()

        /* Encode a niew job Name2*/
        Job jobToEdit = Job.get(jobToAdd.id)
        def jsonJob = jobToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJob)
        jsonUpdate.software = null
        jsonJob = jsonUpdate.encodeAsJSON()

        log.info("put job:" + jsonJob.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/job/" + jobToEdit.id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.put(jsonJob)
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(400, code)

    }

    void testDeleteJob() {

        log.info("create job")
        def jobToDelete = BasicInstance.getBasicJobNotExist()
        assert jobToDelete.save(flush: true) != null
        String jsonJob = jobToDelete.encodeAsJSON()
        int idJob = jobToDelete.id
        log.info("delete job:" + jsonJob.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/job/" + idJob + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.delete()
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)

        log.info("check if object " + idJob + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/job/" + idJob + ".json"
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
//        log.info("check if object " + idJob + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/job/" + idJob + ".json"
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
//        log.info("check if object " + idJob + " exist in DB")
//        client = new HttpClient();
//        URL = Infos.CYTOMINEURL + "api/job/" + idJob + ".json"
//        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
//        client.get()
//        code = client.getResponseCode()
//        client.disconnect();
//        assertEquals(404, code)

    }

    void testDeleteJobNotExist() {

        log.info("create job")
        def jobToDelete = BasicInstance.createOrGetBasicJob()
        String jsonJob = jobToDelete.encodeAsJSON()

        log.info("delete job:" + jsonJob.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/job/-99.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.delete()
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(404, code)

    }

    void testDeleteJobWithJob() {

        //TODO: implement this

//    log.info("create software")
//    //create project and try to delete his software
//    def project = BasicInstance.createOrGetBasicProject()
//    def softwareToDelete = project.software
//    assert softwareToDelete.save(flush:true)!=null
//    String jsonSoftware = softwareToDelete.encodeAsJSON()
//    int idSoftware = softwareToDelete.id
//    log.info("delete software:"+jsonSoftware.replace("\n",""))
//    String URL = Infos.CYTOMINEURL+"api/software/"+idSoftware+".json"
//    HttpClient client = new HttpClient()
//    client.connect(URL,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//    client.delete()
//    int code  = client.getResponseCode()
//    client.disconnect();
//
//    log.info("check response")
//    assertEquals(400,code)

    }
}
