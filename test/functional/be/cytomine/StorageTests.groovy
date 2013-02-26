package be.cytomine

import be.cytomine.image.server.Storage
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.StorageAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 7/02/13
 * Time: 15:05
 */
public class StorageTests {

    void testListStorageWithCredential() {
        def result = StorageAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListStorageWithoutCredential() {
        def result = StorageAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }

    void testShowStorageWithCredential() {
        def result = StorageAPI.show(BasicInstance.createOrGetBasicStorage().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddStorageCorrect() {
        def storageToAdd = BasicInstance.getBasicStorageNotExist()
        def json = storageToAdd.encodeAsJSON()

        def result = StorageAPI.create(json, Infos.GOODLOGIN, Infos.GOODPASSWORD)

        assert 200 == result.code
        int idStorage = result.data.id

        result = StorageAPI.show(idStorage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = StorageAPI.undo()
        assert 200 == result.code

        result = StorageAPI.show(idStorage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = StorageAPI.redo()
        assert 200 == result.code

        result = StorageAPI.show(idStorage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testUpdateStorageCorrect() {
        Storage storageToAdd = BasicInstance.createOrGetBasicStorage()

        def data = UpdateData.createUpdateSet(storageToAdd)
        def result = StorageAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        println "result : $result"
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idStorage = json.storage.id

        def showResult = StorageAPI.show(idStorage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareStorage(data.mapNew, json)

        showResult = StorageAPI.undo()
        assert 200 == result.code
        showResult = StorageAPI.show(idStorage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstance.compareStorage(data.mapOld, JSON.parse(showResult.data))

        showResult = StorageAPI.redo()
        assert 200 == result.code
        showResult = StorageAPI.show(idStorage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstance.compareStorage(data.mapNew, JSON.parse(showResult.data))
    }

    void testUpdateStorageNotExist() {
        Storage storageWithOldName = BasicInstance.createOrGetBasicStorage()
        Storage storageWithNewName = BasicInstance.getBasicStorageNotExist()
        storageWithNewName.save(flush: true)
        Storage storageToEdit = Storage.get(storageWithNewName.id)
        def jsonStorage = storageToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonStorage)
        jsonUpdate.name = storageWithOldName.name
        jsonUpdate.id = -99
        jsonStorage = jsonUpdate.encodeAsJSON()
        def result = StorageAPI.update(-99, jsonStorage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteStorage() {
        def storageToDelete = BasicInstance.getBasicStorageNotExist()
        assert storageToDelete.save(flush: true)!= null
        def id = storageToDelete.id
        def result = StorageAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def showResult = StorageAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code

        result = StorageAPI.undo()
        assert 200 == result.code

        result = StorageAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = StorageAPI.redo()
        assert 200 == result.code

        result = StorageAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testStorageNotExist() {
        def result = StorageAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
}
