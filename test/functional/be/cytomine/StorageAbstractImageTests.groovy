package be.cytomine

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
