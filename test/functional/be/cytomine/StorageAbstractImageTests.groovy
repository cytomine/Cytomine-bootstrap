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
import be.cytomine.test.http.StorageAbstractImageAPI
import be.cytomine.utils.JSONUtils

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 20/02/13
 * Time: 10:45
 */
class StorageAbstractImageTests {


    void testAddStorageAbstractImage() {
        def storage = BasicInstanceBuilder.getStorage()
        def abstractImage = BasicInstanceBuilder.getAbstractImage()
        String json = JSONUtils.toJSONString([ abstractimage : abstractImage.id, storage : storage.id])

        def result =  StorageAbstractImageAPI.create(json, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testDeleteStorageAbstractImage() {
        def storageAbstractImage = BasicInstanceBuilder.getStorageAbstractImage()

        def result =  StorageAbstractImageAPI.delete(storageAbstractImage.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

}
