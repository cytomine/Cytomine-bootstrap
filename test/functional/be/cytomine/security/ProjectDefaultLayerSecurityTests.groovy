package be.cytomine.security

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
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.ProjectDefaultLayerAPI

/**
 * Created by hoyoux on 13.11.14.
 */
class ProjectDefaultLayerSecurityTests extends SecurityTestsAbstract {

    void testSecurityForCytomineAdmin() {
        //create new layer
        def layer = BasicInstanceBuilder.getProjectDefaultLayerNotExist()
        def result = ProjectDefaultLayerAPI.create(layer.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        //check if create == good HTTP Code
        assert 200 == result.code

        Long id = result.data.id
        Long idProject = result.data.project.id

        //check if show == good HTTP Code
        result = ProjectDefaultLayerAPI.show(id, idProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //check if update == good HTTP Code
        ProjectDefaultLayer projLayer = ProjectDefaultLayer.get(id);
        //projLayer.hideByDefault = !projLayer.hideByDefault
        result = ProjectDefaultLayerAPI.update(id, projLayer.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //check if delete == good HTTP Code
        result = ProjectDefaultLayerAPI.delete(id, idProject, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testSecurityForProjectCreator() {
        //init user
        def USERNAME1 = "user1";
        def PASSWORD1 = "password";


        //Get user1
        User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

        //Create new project (user1)
        def result = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
        assert 200 == result.code
        Project project = result.data


        //create new layer
        def layer = new ProjectDefaultLayer(project: project, user: user1, hideByDefault: false)
        result = ProjectDefaultLayerAPI.create(layer.encodeAsJSON(), USERNAME1, PASSWORD1)
        //check if create == good HTTP Code
        assert 200 == result.code

        Long id = result.data.id
        Long idProject = result.data.project.id

        ProjectDefaultLayer projLayer = result.data;

        //check if show == good HTTP Code
        result = ProjectDefaultLayerAPI.show(id, idProject, USERNAME1, PASSWORD1)
        assert 200 == result.code

        //check if update == good HTTP Code
        //projLayer.hideByDefault = !projLayer.hideByDefault
        result = ProjectDefaultLayerAPI.update(id, projLayer.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //check if delete == good HTTP Code
        result = ProjectDefaultLayerAPI.delete(id, idProject, USERNAME1, PASSWORD1)
        assert 200 == result.code
    }

    void testSecurityForSimpleUser() {
        //init user creator
        def USERNAME2 = "user2";
        def PASSWORD2 = "password";

        User creator = BasicInstanceBuilder.getUser2()
        //init user tester
        def USERNAME1 = "user1";
        def PASSWORD1 = "password";

        User user = BasicInstanceBuilder.getUser1()

        //Create new project (user1)
        def resultProject = ProjectAPI.create(BasicInstanceBuilder.getProjectNotExist().encodeAsJSON(),USERNAME2,PASSWORD2)

        Project project = Project.get(resultProject.data.id)

        //create new layer
        def layer = new ProjectDefaultLayer(project: project, user: user, hideByDefault: false)
        def result = ProjectDefaultLayerAPI.create(layer.encodeAsJSON(), USERNAME1, PASSWORD1)

        //check if create == good HTTP Code
        assert 403 == result.code

        //create new layer by the project owner
        result = ProjectDefaultLayerAPI.create(layer.encodeAsJSON(), USERNAME2, PASSWORD2)

        //check if create == good HTTP Code
        assert 200 == result.code

        Long id = result.data.id
        Long idProject = result.data.project.id

        //check if show == good HTTP Code
        result = ProjectDefaultLayerAPI.show(id, idProject, USERNAME1, PASSWORD1)
        assert 403 == result.code

        //check if update == good HTTP Code
        ProjectDefaultLayer projLayer = ProjectDefaultLayer.get(id);
        //projLayer.hideByDefault = !projLayer.hideByDefault
        result = ProjectDefaultLayerAPI.update(id, projLayer.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //check if delete == good HTTP Code
        result = ProjectDefaultLayerAPI.delete(id, idProject, USERNAME1, PASSWORD1)
        assert 403 == result.code
    }

    void testSecurityForAnonymous() {
        //create new layer
        def layer = BasicInstanceBuilder.getProjectDefaultLayerNotExist()
        def result = ProjectDefaultLayerAPI.create(layer.encodeAsJSON(), Infos.BADLOGIN, Infos.BADPASSWORD)

        //check if create == good HTTP Code
        assert 401 == result.code

        //create a layer for the end of the test
        layer = BasicInstanceBuilder.getProjectDefaultLayer()
        log.info("lalala")

        Long id = layer.id
        Long idProject = layer.project.id

        //check if show == good HTTP Code
        result = ProjectDefaultLayerAPI.show(id, idProject, Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code

        //check if update == good HTTP Code
        //layer.hideByDefault = !layer.hideByDefault
        result = ProjectDefaultLayerAPI.update(id, layer.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //check if delete == good HTTP Code
        result = ProjectDefaultLayerAPI.delete(id, idProject, Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }
}