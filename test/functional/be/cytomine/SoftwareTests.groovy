package be.cytomine

import be.cytomine.processing.Software
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.SoftwareAPI
import be.cytomine.test.http.SoftwareParameterAPI
import be.cytomine.test.http.SoftwareProjectAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class SoftwareTests  {

    void testListSoftwareWithCredential() {
       def result = SoftwareAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 200 == result.code
       def json = JSON.parse(result.data)
       assert json.collection instanceof JSONArray
   }
 
   void testListSoftwareWithoutCredential() {
       def result = SoftwareAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
       assert 401 == result.code
   }
 
   void testShowSoftwareWithCredential() {
       def result = SoftwareAPI.show(BasicInstanceBuilder.getSoftware().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 200 == result.code
       def json = JSON.parse(result.data)
       assert json instanceof JSONObject
   }
 
   void testAddSoftwareCorrect() {
       def softwareToAdd = BasicInstanceBuilder.getSoftwareNotExist()
       def result = SoftwareAPI.create(softwareToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 200 == result.code
       int idSoftware = result.data.id
 
       result = SoftwareAPI.show(idSoftware, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 200 == result.code
   }
 
   void testAddSoftwareAlreadyExist() {
       def softwareToAdd = BasicInstanceBuilder.getSoftware()
       def result = SoftwareAPI.create(softwareToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 409 == result.code
   }
 
   void testUpdateSoftwareCorrect() {
       def software = BasicInstanceBuilder.getSoftware()
       def data = UpdateData.createUpdateSet(software,[name: ["OLDNAME","NEWNAME"],serviceName : ["projectService","userAnnotationService"]])
       def resultBase = SoftwareAPI.update(software.id, data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 200==resultBase.code
       def json = JSON.parse(resultBase.data)
       assert json instanceof JSONObject
       int idSoftware = json.software.id
 
       def showResult = SoftwareAPI.show(idSoftware, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       json = JSON.parse(showResult.data)
       BasicInstanceBuilder.compare(data.mapNew, json)
   }
 
   void testUpdateSoftwareNotExist() {
       Software softwareWithOldName = BasicInstanceBuilder.getSoftware()
       Software softwareWithNewName = BasicInstanceBuilder.getSoftwareNotExist()
       softwareWithNewName.save(flush: true)
       Software softwareToEdit = Software.get(softwareWithNewName.id)
       def jsonSoftware = softwareToEdit.encodeAsJSON()
       def jsonUpdate = JSON.parse(jsonSoftware)
       jsonUpdate.name = softwareWithOldName.name
       jsonUpdate.id = -99
       jsonSoftware = jsonUpdate.toString()
       def result = SoftwareAPI.update(-99, jsonSoftware, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 404 == result.code
   }
 
   void testUpdateSoftwareWithNameAlreadyExist() {
       Software softwareWithOldName = BasicInstanceBuilder.getSoftware()
       Software softwareWithNewName = BasicInstanceBuilder.getSoftwareNotExist(true)
       Software softwareToEdit = Software.get(softwareWithNewName.id)
       def jsonSoftware = softwareToEdit.encodeAsJSON()
       def jsonUpdate = JSON.parse(jsonSoftware)
       jsonUpdate.name = softwareWithOldName.name
       jsonSoftware = jsonUpdate.toString()
       def result = SoftwareAPI.update(softwareToEdit.id, jsonSoftware, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 409 == result.code
   }
     
     void testEditSoftwareWithBadName() {
         Software softwareToAdd = BasicInstanceBuilder.getSoftware()
         Software softwareToEdit = Software.get(softwareToAdd.id)
         def jsonSoftware = softwareToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonSoftware)
         jsonUpdate.name = null
         jsonSoftware = jsonUpdate.toString()
         def result = SoftwareAPI.update(softwareToAdd.id, jsonSoftware, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 400 == result.code
     }
 
   void testDeleteSoftware() {
       def softwareToDelete = BasicInstanceBuilder.getSoftwareNotExist(true)
       def id = softwareToDelete.id
       def result = SoftwareAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 200 == result.code
 
       def showResult = SoftwareAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 404 == showResult.code
   }
 
   void testDeleteSoftwareNotExist() {
       def result = SoftwareAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 404 == result.code
   }
 
//   void testDeleteSoftwareWithProject() {
//       def softwareProject = BasicInstanceBuilder.getSoftwareProject()
//       def result = SoftwareAPI.delete(softwareProject.software.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
//       assert 200 == result.code
//   }

    void testDeleteSoftwareWithJob() {

        //TODO: implement this

//    log.info("create software")
//    //create project and try to delete his software
//    def project = BasicInstanceBuilder.getProject()
//    def softwareToDelete = project.software
//    assert softwareToDelete.save(flush:true)!=null
//    String jsonSoftware = softwareToDelete.encodeAsJSON()
//    int idSoftware = softwareToDelete.id
//    log.info("delete software:"+jsonSoftware.replace("\n",""))
//    String URL = Infos.CYTOMINEURL+"api/software/"+idSoftware+".json"
//    HttpClient client = new HttpClient()
//    client.connect(URL,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
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
        Software softwareToAdd = BasicInstanceBuilder.getSoftwareNotExist()
        def result = SoftwareAPI.create(softwareToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idSoftware = result.data.id

        /*
        * test add software parameter N
        */
        log.info("create softwareparameter")
        def softwareparameterToAdd = BasicInstanceBuilder.getSoftwareParameterNotExist()
        softwareparameterToAdd.software = Software.read(idSoftware)
        softwareparameterToAdd.name = "N"
        softwareparameterToAdd.type = "String"
        println("softwareparameterToAdd.version=" + softwareparameterToAdd.version)
        String jsonSoftwareparameter = softwareparameterToAdd.encodeAsJSON()
        result = SoftwareParameterAPI.create(jsonSoftwareparameter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        /*
        * test add software parameter T
        */
        log.info("create softwareparameter")
        softwareparameterToAdd = BasicInstanceBuilder.getSoftwareParameterNotExist()
        softwareparameterToAdd.software = Software.read(idSoftware)
        softwareparameterToAdd.name = "T"
        softwareparameterToAdd.type = "String"
        println("softwareparameterToAdd.version=" + softwareparameterToAdd.version)
        jsonSoftwareparameter = softwareparameterToAdd.encodeAsJSON()
        result = SoftwareParameterAPI.create(jsonSoftwareparameter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        /*
        * test add software parameter project x
        */
        def SoftwareProjectToAdd = BasicInstanceBuilder.getSoftwareProjectNotExist()
        result = SoftwareProjectAPI.create(SoftwareProjectToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idSoftwareProject = result.data.id
        /*
        * test add software parameter project y
        */
        SoftwareProjectToAdd = BasicInstanceBuilder.getSoftwareProjectNotExist()
        result = SoftwareProjectAPI.create(SoftwareProjectToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        idSoftwareProject = result.data.id
    }
}
