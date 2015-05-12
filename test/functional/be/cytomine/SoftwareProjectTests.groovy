package be.cytomine

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.processing.Job
import be.cytomine.processing.SoftwareProject
import be.cytomine.test.BasicInstanceBuilder
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
class SoftwareProjectTests  {

    void testListSoftwareProjectWithCredential() {
         def result = SoftwareProjectAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray
     }
 
     void testListSoftwareByProject() {
         SoftwareProject softwareProject = BasicInstanceBuilder.getSoftwareProject()
         def result = SoftwareProjectAPI.listByProject(softwareProject.project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray

         result = SoftwareProjectAPI.listByProject(-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == result.code
     }

    void testListSoftwareProjectByProject() {
        SoftwareProject softwareProject = BasicInstanceBuilder.getSoftwareProject()
        def result = SoftwareProjectAPI.listSoftwareProjectByProject(softwareProject.project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = SoftwareProjectAPI.listSoftwareProjectByProject(-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
 
    void testListSoftwareProjectBySoftware() {
        SoftwareProject softwareProject = BasicInstanceBuilder.getSoftwareProject()
        def result = SoftwareProjectAPI.listBySoftware(softwareProject.software.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testStatsSoftwareProject() {
        Job job = BasicInstanceBuilder.getJob()
        def result = SoftwareProjectAPI.stats(job.project.id,job.software.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
    }

    void testStatsSoftwareProjectNotExist() {
        SoftwareProject softwareProject = BasicInstanceBuilder.getSoftwareProject()
        def result = SoftwareProjectAPI.stats(-99,softwareProject.software.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
        result = SoftwareProjectAPI.stats(softwareProject.project.id,-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

     void testAddSoftwareProjectCorrect() {
         def SoftwareProjectToAdd = BasicInstanceBuilder.getSoftwareProjectNotExist()
         def result = SoftwareProjectAPI.create(SoftwareProjectToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
         int idSoftwareProject = result.data.id
   
         result = SoftwareProjectAPI.show(idSoftwareProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
     }
 
     void testAddSoftwareProjectWithBadSoftware() {
         SoftwareProject SoftwareProjectToAdd = BasicInstanceBuilder.getSoftwareProject()
         SoftwareProject SoftwareProjectToEdit = SoftwareProject.get(SoftwareProjectToAdd.id)
         def jsonSoftwareProject = SoftwareProjectToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonSoftwareProject)
         jsonUpdate.software = -99
         jsonSoftwareProject = jsonUpdate.toString()
         def result = SoftwareProjectAPI.create(jsonSoftwareProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 400 == result.code
     }

    void testAddSoftwareProjectWithBadProject() {
        SoftwareProject SoftwareProjectToAdd = BasicInstanceBuilder.getSoftwareProject()
        SoftwareProject SoftwareProjectToEdit = SoftwareProject.get(SoftwareProjectToAdd.id)
        def jsonSoftwareProject = SoftwareProjectToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonSoftwareProject)
        jsonUpdate.project = -99
        jsonSoftwareProject = jsonUpdate.toString()
        def result = SoftwareProjectAPI.create(jsonSoftwareProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

     void testDeleteSoftwareProject() {
         def SoftwareProjectToDelete = BasicInstanceBuilder.getSoftwareProjectNotExist(BasicInstanceBuilder.getSoftwareNotExist(true),BasicInstanceBuilder.getProjectNotExist(true),true)
         def id = SoftwareProjectToDelete.id
         def result = SoftwareProjectAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
 
         def showResult = SoftwareProjectAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == showResult.code
     }
 
     void testDeleteSoftwareProjectNotExist() {
         def result = SoftwareProjectAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == result.code
     }
}
