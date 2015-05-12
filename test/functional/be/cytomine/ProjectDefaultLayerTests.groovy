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

import be.cytomine.project.Project
import be.cytomine.project.ProjectDefaultLayer
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectDefaultLayerAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by hoyoux on 13.11.14.
 */
class ProjectDefaultLayerTests {

    void testListAllProjectDefaultLayerByProjectWithCredential() {
        ProjectDefaultLayer layer = BasicInstanceBuilder.getProjectDefaultLayer();
        def result = ProjectDefaultLayerAPI.list(layer.project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListAllProjectDefaultLayerByProjectWithoutCredential() {
        Project project = BasicInstanceBuilder.getProjectDefaultLayer().getProject();
        def result = ProjectDefaultLayerAPI.list(project.id, Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }

    void testShowProjectDefaultLayerWithCredential() {
        ProjectDefaultLayer layer = BasicInstanceBuilder.getProjectDefaultLayer()
        def result = ProjectDefaultLayerAPI.show(layer.id, layer.project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddProjectDefaultLayerCorrect() {
        def layerToAdd = BasicInstanceBuilder.getProjectDefaultLayerNotExist()

        def result = ProjectDefaultLayerAPI.create(layerToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idLayer = result.data.id

        result = ProjectDefaultLayerAPI.show(idLayer, layerToAdd.project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = ProjectDefaultLayerAPI.undo()
        assert 200 == result.code

        result = ProjectDefaultLayerAPI.show(idLayer, layerToAdd.project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = ProjectDefaultLayerAPI.redo()
        assert 200 == result.code

        result = ProjectDefaultLayerAPI.show(idLayer, layerToAdd.project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddProjectDefaultLayerAlreadyExist() {
        def layerToAdd = BasicInstanceBuilder.getProjectDefaultLayerNotExist(false, true);

        def result = ProjectDefaultLayerAPI.create(layerToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code // first creation

        //layerToAdd.hideByDefault = !layerToAdd.hideByDefault

        result = ProjectDefaultLayerAPI.create(layerToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code // cannot create if already exist
    }

    void testDeleteProjectDefaultLayer() {
        def layerToDelete = BasicInstanceBuilder.getProjectDefaultLayerNotExist()
        assert layerToDelete.save(flush: true)!= null
        def id = layerToDelete.id
        def idProject = layerToDelete.project.id
        def result = ProjectDefaultLayerAPI.delete(id, idProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def showResult = ProjectDefaultLayerAPI.show(id, idProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code

        result = ProjectDefaultLayerAPI.undo()
        assert 200 == result.code

        result = ProjectDefaultLayerAPI.show(id, idProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = ProjectDefaultLayerAPI.redo()
        assert 200 == result.code

        result = ProjectDefaultLayerAPI.show(id, idProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testUpdateProjectDefaultLayer() {
        def layerToUpdate = BasicInstanceBuilder.getProjectDefaultLayerNotExist()
        assert layerToUpdate.save(flush: true)!= null
        def id = layerToUpdate.id
        def idProject = layerToUpdate.project.id

        def json

        def showResult = ProjectDefaultLayerAPI.show(id, idProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == showResult.code

        json = JSON.parse(showResult.data)

        assert json.hideByDefault == false

        //layerToUpdate.hideByDefault = ! layerToUpdate.hideByDefault

        json = JSON.parse(layerToUpdate.encodeAsJSON())
        json.hideByDefault = !json.hideByDefault

        def result = ProjectDefaultLayerAPI.update(id, json.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        showResult = ProjectDefaultLayerAPI.show(id, idProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == showResult.code
        json = JSON.parse(showResult.data)
        assert json.hideByDefault == true

        result = ProjectDefaultLayerAPI.undo()
        assert 200 == result.code

        showResult = ProjectDefaultLayerAPI.show(id, idProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(showResult.data)
        assert json.hideByDefault == false

        result = ProjectDefaultLayerAPI.redo()
        assert 200 == result.code

        showResult = ProjectDefaultLayerAPI.show(id, idProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(showResult.data)
        assert json.hideByDefault == true
    }

    void testDeleteProjectDefaultLayerNotExist() {
        def result = ProjectDefaultLayerAPI.delete(-99, -99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

}
