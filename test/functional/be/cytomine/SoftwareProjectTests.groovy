package be.cytomine

import be.cytomine.processing.SoftwareProject
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.SoftwareProjectAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class SoftwareProjectTests extends functionaltestplugin.FunctionalTestCase {

    void testListSoftwareProjectWithCredential() {
         def result = SoftwareProjectAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
         def json = JSON.parse(result.data)
         assert json instanceof JSONArray
     }
 
     void testListSoftwareProjectByProject() {
         SoftwareProject SoftwareProject = BasicInstance.createOrGetBasicSoftwareProject()
         def result = SoftwareProjectAPI.listByProject(SoftwareProject.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
         def json = JSON.parse(result.data)
         assert json instanceof JSONArray
     }
 
    void testListSoftwareProjectBySoftware() {
        SoftwareProject SoftwareProject = BasicInstance.createOrGetBasicSoftwareProject()
        def result = SoftwareProjectAPI.listBySoftware(SoftwareProject.software.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }
 
     void testAddSoftwareProjectCorrect() {
         def SoftwareProjectToAdd = BasicInstance.getBasicSoftwareProjectNotExist()
         def result = SoftwareProjectAPI.create(SoftwareProjectToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
         int idSoftwareProject = result.data.id
   
         result = SoftwareProjectAPI.show(idSoftwareProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
     }
 
     void testAddSoftwareProjectWithBadSoftware() {
         SoftwareProject SoftwareProjectToAdd = BasicInstance.createOrGetBasicSoftwareProject()
         SoftwareProject SoftwareProjectToEdit = SoftwareProject.get(SoftwareProjectToAdd.id)
         def jsonSoftwareProject = SoftwareProjectToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonSoftwareProject)
         jsonUpdate.software = -99
         jsonSoftwareProject = jsonUpdate.encodeAsJSON()
         def result = SoftwareProjectAPI.create(jsonSoftwareProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(400, result.code)
     }

    void testAddSoftwareProjectWithBadProject() {
        SoftwareProject SoftwareProjectToAdd = BasicInstance.createOrGetBasicSoftwareProject()
        SoftwareProject SoftwareProjectToEdit = SoftwareProject.get(SoftwareProjectToAdd.id)
        def jsonSoftwareProject = SoftwareProjectToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSoftwareProject)
        jsonUpdate.project = -99
        jsonSoftwareProject = jsonUpdate.encodeAsJSON()
        def result = SoftwareProjectAPI.create(jsonSoftwareProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }
 

     void testDeleteSoftwareProject() {
         def SoftwareProjectToDelete = BasicInstance.getBasicSoftwareProjectNotExist()
         assert SoftwareProjectToDelete.save(flush: true)!= null
         def id = SoftwareProjectToDelete.id
         def result = SoftwareProjectAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(200, result.code)
 
         def showResult = SoftwareProjectAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(404, showResult.code)
     }
 
     void testDeleteSoftwareProjectNotExist() {
         def result = SoftwareProjectAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
         assertEquals(404, result.code)
     }
}
