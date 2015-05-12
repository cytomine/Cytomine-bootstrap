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

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ImageInstanceAPI
import be.cytomine.test.http.UserPositionAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class UserPositionTests  {


    void testListByUser() {
        def image = BasicInstanceBuilder.getImageInstance()
       def result = UserPositionAPI.listLastByUser(image.id,BasicInstanceBuilder.user1.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 200 == result.code
   }

    void testListByProject() {
        def image = BasicInstanceBuilder.getImageInstance()
       def result = UserPositionAPI.listLastByProject(image.project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 200 == result.code
   }

    void testListByImage() {
        def image = BasicInstanceBuilder.getImageInstance()
       def result = UserPositionAPI.listLastByImage(image.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
       assert 200 == result.code
   }

    void testAddPosition() {
        def image = BasicInstanceBuilder.getImageInstance()
        def json = JSON.parse("{image:${image.id},lon:100,lat:100, zoom: 1}")

        def result = UserPositionAPI.create(image.id, json.toString(),Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        result = UserPositionAPI.listLastByUser(image.id,BasicInstanceBuilder.user1.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        result = UserPositionAPI.listLastByProject(image.project.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        result = UserPositionAPI.listLastByImage(image.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
         //same position, user don't move
        result = UserPositionAPI.create(image.id, json.toString(),Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }


    void testGetLastOpenedImage() {
        def image = BasicInstanceBuilder.getImageInstance()
        def json = JSON.parse("{image:${image.id},lon:100,lat:100,zoom:1}")

        def result = UserPositionAPI.create(image.id, json.toString(),Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = ImageInstanceAPI.listLastOpened(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert ImageInstanceAPI.containsInJSONList(image.id,json)

    }


}
