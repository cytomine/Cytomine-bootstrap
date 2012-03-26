package be.cytomine

import be.cytomine.ontology.Ontology
import be.cytomine.test.BasicInstance
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.processing.Software

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class SoftwareTests extends functionaltestplugin.FunctionalTestCase {

    void testListSoftwareWithCredential() {

        log.info("list software")
        String URL = Infos.CYTOMINEURL + "api/software.json"
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

    void testListSoftwareWithoutCredential() {

        log.info("list software")
        String URL = Infos.CYTOMINEURL + "api/software.json"
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.BADLOGIN, Infos.BADPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();

        log.info("check response")
        assertEquals(401, code)

    }

    void testShowSoftwareWithCredential() {

        Software software = BasicInstance.createOrGetBasicSoftware()

        log.info("list software")
        String URL = Infos.CYTOMINEURL + "api/software/" + software.id + ".json"
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

    void testAddSoftwareCorrect() {

        log.info("create software")
        def softwareToAdd = BasicInstance.getBasicSoftwareNotExist()
        println("softwareToAdd.version=" + softwareToAdd.version)
        String jsonSoftware = softwareToAdd.encodeAsJSON()

        log.info("post software:" + jsonSoftware.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/software.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonSoftware)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        def json = JSON.parse(response)
        assert json instanceof JSONObject
        int idSoftware = json.software.id

        log.info("check if object " + idSoftware + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

        log.info("test undo")
        client = new HttpClient()
        URL = Infos.CYTOMINEURL + Infos.UNDOURL + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

        log.info("check if object " + idSoftware + " not exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(404, code)

        log.info("test redo")
        client = new HttpClient()
        URL = Infos.CYTOMINEURL + Infos.REDOURL + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

        //must be done because redo change id
        json = JSON.parse(response)
        assert json instanceof JSONArray

        log.info("check if object " + idSoftware + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

    }

    void testAddSoftwareAlreadyExist() {
        log.info("create software")
        def softwareToAdd = BasicInstance.createOrGetBasicSoftware()
        //softwareToAdd is save in DB
        String jsonSoftware = softwareToAdd.encodeAsJSON()

        log.info("post software:" + jsonSoftware.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/software.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonSoftware)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(409, code)
    }

    void testAddSoftwareWithBadName() {
        log.info("create software")
        def softwareToAdd = BasicInstance.getBasicSoftwareNotExist()
        String jsonSoftware = softwareToAdd.encodeAsJSON()

        def jsonUpdate = JSON.parse(jsonSoftware)
        jsonUpdate.name = null
        jsonSoftware = jsonUpdate.encodeAsJSON()

        log.info("post software:" + jsonSoftware.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/software.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonSoftware)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(400, code)
    }

    void testAddSoftwareFullWorkflow() {
        /**
         * test add software
         */
        log.info("create software")
        def softwareToAdd = BasicInstance.getBasicSoftwareNotExist()
        println("softwareToAdd.version=" + softwareToAdd.version)
        String jsonSoftware = softwareToAdd.encodeAsJSON()

        log.info("post software:" + jsonSoftware.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/software.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonSoftware)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        def json = JSON.parse(response)
        assert json instanceof JSONObject
        int idSoftware = json.software.id

        /*
        * test add software parameter N
        */
        log.info("create softwareparameter")
        def softwareparameterToAdd = BasicInstance.getBasicSoftwareParameterNotExist()
        softwareparameterToAdd.software = Software.read(idSoftware)
        softwareparameterToAdd.name = "N"
        softwareparameterToAdd.type = "String"
        println("softwareparameterToAdd.version=" + softwareparameterToAdd.version)
        String jsonSoftwareparameter = softwareparameterToAdd.encodeAsJSON()

        log.info("post softwareparameter:" + jsonSoftwareparameter.replace("\n", ""))
        URL = Infos.CYTOMINEURL + "api/softwareparameter.json"
        client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonSoftwareparameter)
        code = client.getResponseCode()
        response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        json = JSON.parse(response)
        assert json instanceof JSONObject
        int idSoftwareparameter = json.softwareparameter.id

        log.info("check if object " + idSoftwareparameter + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/softwareparameter/" + idSoftwareparameter + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

        /*
        * test add software parameter T
        */
        log.info("create softwareparameter")
        softwareparameterToAdd = BasicInstance.getBasicSoftwareParameterNotExist()
        softwareparameterToAdd.software = Software.read(idSoftware)
        softwareparameterToAdd.name = "T"
        softwareparameterToAdd.type = "String"
        println("softwareparameterToAdd.version=" + softwareparameterToAdd.version)
        jsonSoftwareparameter = softwareparameterToAdd.encodeAsJSON()

        log.info("post softwareparameter:" + jsonSoftwareparameter.replace("\n", ""))
        URL = Infos.CYTOMINEURL + "api/softwareparameter.json"
        client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonSoftwareparameter)
        code = client.getResponseCode()
        response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        json = JSON.parse(response)
        assert json instanceof JSONObject
        idSoftwareparameter = json.softwareparameter.id

        log.info("check if object " + idSoftwareparameter + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/softwareparameter/" + idSoftwareparameter + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

        /*
        * test add software parameter project x
        */
        log.info("create softwareproject")
        def softwareprojectToAdd = BasicInstance.getBasicSoftwareProjectNotExist()
        println("softwareprojectToAdd.version=" + softwareprojectToAdd.version)
        String jsonSoftwareproject = softwareprojectToAdd.encodeAsJSON()

        log.info("post softwareproject:" + jsonSoftwareproject.replace("\n", ""))
        URL = Infos.CYTOMINEURL + "api/softwareproject.json"
        client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonSoftwareproject)
        code = client.getResponseCode()
        response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        json = JSON.parse(response)
        assert json instanceof JSONObject
        int idSoftwareproject = json.softwareproject.id

        log.info("check if object " + idSoftwareproject + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/softwareproject/" + idSoftwareproject + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

        /*
        * test add software parameter project y
        */
        log.info("create softwareproject")
        softwareprojectToAdd = BasicInstance.getBasicSoftwareProjectNotExist()
        println("softwareprojectToAdd.version=" + softwareprojectToAdd.version)
        jsonSoftwareproject = softwareprojectToAdd.encodeAsJSON()

        log.info("post softwareproject:" + jsonSoftwareproject.replace("\n", ""))
        URL = Infos.CYTOMINEURL + "api/softwareproject.json"
        client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.post(jsonSoftwareproject)
        code = client.getResponseCode()
        response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        json = JSON.parse(response)
        assert json instanceof JSONObject
        idSoftwareproject = json.softwareproject.id

        log.info("check if object " + idSoftwareproject + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/softwareproject/" + idSoftwareproject + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)
    }

    void testUpdateSoftwareCorrect() {

        String oldName = "Name1"
        String newName = "Name2"
        String oldNameService = "projectService"
        String newNameService = "annotationService"

        def mapNew = ["name": newName,"serviceName" : newNameService]
        def mapOld = ["name": oldName,"serviceName" : oldNameService]

        /* Create a Name1 software */
        log.info("create software")
        Software softwareToAdd = BasicInstance.createOrGetBasicSoftware()
        softwareToAdd.name = oldName
        softwareToAdd.serviceName = oldNameService
        assert (softwareToAdd.save(flush: true) != null)

        /* Encode a niew software Name2*/
        Software softwareToEdit = Software.get(softwareToAdd.id)
        def jsonSoftware = softwareToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSoftware)
        jsonUpdate.name = newName
        jsonUpdate.serviceName = newNameService
        jsonSoftware = jsonUpdate.encodeAsJSON()

        log.info("put software:" + jsonSoftware.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/software/" + softwareToEdit.id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.put(jsonSoftware)
        int code = client.getResponseCode()
        String response = client.getResponseData()
        println response
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)
        def json = JSON.parse(response)
        assert json instanceof JSONObject
        int idSoftware = json.software.id

        log.info("check if object " + idSoftware + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();

        assertEquals(200, code)
        json = JSON.parse(response)
        assert json instanceof JSONObject

        BasicInstance.compareSoftware(mapNew, json)

        log.info("test undo")
        client = new HttpClient()
        URL = Infos.CYTOMINEURL + Infos.UNDOURL + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

        log.info("check if object " + idSoftware + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();

        assertEquals(200, code)
        json = JSON.parse(response)
        assert json instanceof JSONObject

        BasicInstance.compareSoftware(mapOld, json)

        log.info("test redo")
        client = new HttpClient()
        URL = Infos.CYTOMINEURL + Infos.REDOURL + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

        log.info("check if object " + idSoftware + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();

        assertEquals(200, code)
        json = JSON.parse(response)
        assert json instanceof JSONObject

        BasicInstance.compareSoftware(mapNew, json)


        log.info("check if object " + idSoftware + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();

        assertEquals(200, code)
        json = JSON.parse(response)
        assert json instanceof JSONObject

    }

    void testUpdateSoftwareNotExist() {
        /* Create a Name1 software */
        log.info("create software")
        Software softwareToAdd = BasicInstance.createOrGetBasicSoftware()

        /* Encode a niew software Name2*/
        Software softwareToEdit = Software.get(softwareToAdd.id)
        def jsonSoftware = softwareToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSoftware)
        jsonUpdate.id = -99
        jsonSoftware = jsonUpdate.encodeAsJSON()

        log.info("put software:" + jsonSoftware.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/software/-99.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.put(jsonSoftware)
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(404, code)
    }

    void testUpdateSoftwareWithNameAlreadyExist() {

        /* Create a Name1 software */
        log.info("create software")
        Software softwareWithOldName = BasicInstance.createOrGetBasicSoftware()
        Software softwareWithNewName = BasicInstance.getBasicSoftwareNotExist()
        softwareWithNewName.save(flush: true)

        /* Encode a niew software Name2*/
        Software softwareToEdit = Software.get(softwareWithNewName.id)
        log.info("softwareToEdit=" + softwareToEdit)
        def jsonSoftware = softwareToEdit.encodeAsJSON()
        log.info("jsonSoftware=" + jsonSoftware)
        def jsonUpdate = JSON.parse(jsonSoftware)
        jsonUpdate.name = softwareWithOldName.name
        jsonSoftware = jsonUpdate.encodeAsJSON()

        log.info("put software:" + jsonSoftware.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/software/" + softwareToEdit.id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.put(jsonSoftware)
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(409, code)

    }

    void testUpdateSoftwareWithBadName() {

        /* Create a Name1 software */
        log.info("create software")
        Software softwareToAdd = BasicInstance.createOrGetBasicSoftware()

        /* Encode a niew software Name2*/
        Software softwareToEdit = Software.get(softwareToAdd.id)
        def jsonSoftware = softwareToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSoftware)
        jsonUpdate.name = null
        jsonSoftware = jsonUpdate.encodeAsJSON()

        log.info("put software:" + jsonSoftware.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/software/" + softwareToEdit.id + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.put(jsonSoftware)
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(400, code)

    }

    void testDeleteSoftware() {

        log.info("create software")
        def softwareToDelete = BasicInstance.getBasicSoftwareNotExist()
        assert softwareToDelete.save(flush: true) != null
        String jsonSoftware = softwareToDelete.encodeAsJSON()
        int idSoftware = softwareToDelete.id
        log.info("delete software:" + jsonSoftware.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.delete()
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(200, code)

        log.info("check if object " + idSoftware + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        client.disconnect();

        assertEquals(404, code)

        log.info("test undo")
        client = new HttpClient()
        URL = Infos.CYTOMINEURL + Infos.UNDOURL + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.get()
        code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assertEquals(200, code)

        log.info("check if object " + idSoftware + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        response = client.getResponseData()
        client.disconnect();

        assertEquals(200, code)


        log.info("test redo")
        client = new HttpClient()
        URL = Infos.CYTOMINEURL + Infos.REDOURL + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.get()
        code = client.getResponseCode()
        client.disconnect();
        assertEquals(200, code)

        log.info("check if object " + idSoftware + " exist in DB")
        client = new HttpClient();
        URL = Infos.CYTOMINEURL + "api/software/" + idSoftware + ".json"
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD);
        client.get()
        code = client.getResponseCode()
        client.disconnect();
        assertEquals(404, code)

    }

    void testDeleteSoftwareNotExist() {

        log.info("create software")
        def softwareToDelete = BasicInstance.createOrGetBasicSoftware()
        String jsonSoftware = softwareToDelete.encodeAsJSON()

        log.info("delete software:" + jsonSoftware.replace("\n", ""))
        String URL = Infos.CYTOMINEURL + "api/software/-99.json"
        HttpClient client = new HttpClient()
        client.connect(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        client.delete()
        int code = client.getResponseCode()
        client.disconnect();

        log.info("check response")
        assertEquals(404, code)

    }

    void testDeleteSoftwareWithJob() {

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
