package be.cytomine

import be.cytomine.processing.Software
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.SoftwareAPI
import be.cytomine.test.http.SoftwareParameterAPI
import be.cytomine.test.http.SoftwareProjectAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.utils.UpdateData

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class SoftwareTests  {

    void testListSoftwareWithCredential() {
       def result = SoftwareAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
       def json = JSON.parse(result.data)
       assert json instanceof JSONArray
   }
 
   void testListSoftwareWithoutCredential() {
       def result = SoftwareAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
       assert 401 == result.code
   }
 
   void testShowSoftwareWithCredential() {
       def result = SoftwareAPI.show(BasicInstance.createOrGetBasicSoftware().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
       def json = JSON.parse(result.data)
       assert json instanceof JSONObject
   }
 
   void testAddSoftwareCorrect() {
       def softwareToAdd = BasicInstance.getBasicSoftwareNotExist()
       def result = SoftwareAPI.create(softwareToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
       int idSoftware = result.data.id
 
       result = SoftwareAPI.show(idSoftware, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
 
       result = SoftwareAPI.undo()
       assert 200 == result.code
 
       result = SoftwareAPI.show(idSoftware, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 404 == result.code
 
       result = SoftwareAPI.redo()
       assert 200 == result.code
 
       result = SoftwareAPI.show(idSoftware, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
   }
 
   void testAddSoftwareAlreadyExist() {
       def softwareToAdd = BasicInstance.createOrGetBasicSoftware()
       def result = SoftwareAPI.create(softwareToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 409 == result.code
   }
 
   void testUpdateSoftwareCorrect() {
       Software softwareToAdd = BasicInstance.createOrGetBasicSoftware()
       def data = UpdateData.createUpdateSet(softwareToAdd)
       def resultBase = SoftwareAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200==resultBase.code
       def json = JSON.parse(resultBase.data)
       assert json instanceof JSONObject
       int idSoftware = json.software.id
 
       def showResult = SoftwareAPI.show(idSoftware, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       json = JSON.parse(showResult.data)
       BasicInstance.compareSoftware(data.mapNew, json)

       def result = SoftwareAPI.undo()
       assert 200 == result.code
       showResult = SoftwareAPI.show(idSoftware, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       System.out.println("toto="+showResult);
       System.out.println("toto="+showResult.data);
       System.out.println("toto="+JSON.parse(showResult.data));
       System.out.println("toto="+JSON.parse(showResult.data).name);
       BasicInstance.compareSoftware(data.mapOld, JSON.parse(showResult.data))

       result = SoftwareAPI.redo()
       assert 200 == result.code
       showResult = SoftwareAPI.show(idSoftware, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       BasicInstance.compareSoftware(data.mapNew, JSON.parse(showResult.data))
   }
 
   void testUpdateSoftwareNotExist() {
       Software softwareWithOldName = BasicInstance.createOrGetBasicSoftware()
       Software softwareWithNewName = BasicInstance.getBasicSoftwareNotExist()
       softwareWithNewName.save(flush: true)
       Software softwareToEdit = Software.get(softwareWithNewName.id)
       def jsonSoftware = softwareToEdit.encodeAsJSON()
       def jsonUpdate = JSON.parse(jsonSoftware)
       jsonUpdate.name = softwareWithOldName.name
       jsonUpdate.id = -99
       jsonSoftware = jsonUpdate.encodeAsJSON()
       def result = SoftwareAPI.update(-99, jsonSoftware, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 404 == result.code
   }
 
   void testUpdateSoftwareWithNameAlreadyExist() {
       Software softwareWithOldName = BasicInstance.createOrGetBasicSoftware()
       Software softwareWithNewName = BasicInstance.getBasicSoftwareNotExist()
       softwareWithNewName.save(flush: true)
       Software softwareToEdit = Software.get(softwareWithNewName.id)
       def jsonSoftware = softwareToEdit.encodeAsJSON()
       def jsonUpdate = JSON.parse(jsonSoftware)
       jsonUpdate.name = softwareWithOldName.name
       jsonSoftware = jsonUpdate.encodeAsJSON()
       def result = SoftwareAPI.update(softwareToEdit.id, jsonSoftware, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 409 == result.code
   }
     
     void testEditSoftwareWithBadName() {
         Software softwareToAdd = BasicInstance.createOrGetBasicSoftware()
         Software softwareToEdit = Software.get(softwareToAdd.id)
         def jsonSoftware = softwareToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonSoftware)
         jsonUpdate.name = null
         jsonSoftware = jsonUpdate.encodeAsJSON()
         def result = SoftwareAPI.update(softwareToAdd.id, jsonSoftware, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assert 400 == result.code
     }
 
   void testDeleteSoftware() {
       def softwareToDelete = BasicInstance.getBasicSoftwareNotExist()
       softwareToDelete = softwareToDelete.save(flush: true)
       assert softwareToDelete!= null
       def id = softwareToDelete.id
       def result = SoftwareAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
 
       def showResult = SoftwareAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 404 == showResult.code
 
       result = SoftwareAPI.undo()
       assert 200 == result.code
 
       result = SoftwareAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
 
       result = SoftwareAPI.redo()
       assert 200 == result.code
 
       result = SoftwareAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 404 == result.code
   }
 
   void testDeleteSoftwareNotExist() {
       def result = SoftwareAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 404 == result.code
   }
 
   void testDeleteSoftwareWithProject() {
       def softwareProject = BasicInstance.createOrGetBasicSoftwareProject()
       def result = SoftwareAPI.delete(softwareProject.software.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
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


    void testAddSoftwareFullWorkflow() {
        /**
         * test add software
         */
        Software softwareToAdd = BasicInstance.getBasicSoftwareNotExist()
        def result = SoftwareAPI.create(softwareToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        int idSoftware = result.data.id

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
        result = SoftwareParameterAPI.create(jsonSoftwareparameter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

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
        result = SoftwareParameterAPI.create(jsonSoftwareparameter, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        /*
        * test add software parameter project x
        */
        def SoftwareProjectToAdd = BasicInstance.getBasicSoftwareProjectNotExist()
        result = SoftwareProjectAPI.create(SoftwareProjectToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        int idSoftwareProject = result.data.id
        /*
        * test add software parameter project y
        */
        SoftwareProjectToAdd = BasicInstance.getBasicSoftwareProjectNotExist()
        result = SoftwareProjectAPI.create(SoftwareProjectToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        idSoftwareProject = result.data.id
    }
}
