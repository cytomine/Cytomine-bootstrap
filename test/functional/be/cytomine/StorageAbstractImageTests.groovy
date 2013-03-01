package be.cytomine

import be.cytomine.image.server.StorageAbstractImage
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.StorageAPI
import be.cytomine.test.http.StorageAbstractImageAPI
import grails.converters.JSON

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 20/02/13
 * Time: 10:45
 */
class StorageAbstractImageTests {


    void testAddStorageAbstractImage() {
        def storage = BasicInstance.createOrGetBasicStorage()
        def abstractImage = BasicInstance.createOrGetBasicAbstractImage()
        String json = [ abstractimage : abstractImage.id, storage : storage.id].encodeAsJSON()

        def result =  StorageAbstractImageAPI.create(json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testDeleteStorageAbstractImage() {
        def storageAbstractImage = BasicInstance.createOrGetBasicStorageAbstractImage()

        def result =  StorageAbstractImageAPI.delete(storageAbstractImage.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

}
