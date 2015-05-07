package be.cytomine

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ConfigAPI
import be.cytomine.utils.Config
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by hoyoux on 06.05.15.
 */
class ConfigTests {

    //TEST SHOW
    void testShow() {
        def result = ConfigAPI.show("-1", Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        def config = BasicInstanceBuilder.getConfigNotExist()
        result = ConfigAPI.create(config.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        String key =  result.data.key

        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    //TEST LIST
    void testList() {
        def result = ConfigAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    //TEST DELETE
    void testDelete() {
        def configToDelete = BasicInstanceBuilder.getConfigNotExist()
        assert configToDelete.save(flush: true) != null

        def key = configToDelete.key
        def result = ConfigAPI.delete(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //UNDO & REDO
        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = ConfigAPI.undo()
        assert 200 == result.code

        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = ConfigAPI.redo()
        assert 200 == result.code

        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    //TEST ADD
    void testAddCorrect() {
        def configToAdd = BasicInstanceBuilder.getConfigNotExist()

        def result = ConfigAPI.create(configToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        String key =  result.data.key

        //UNDO & REDO
        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = ConfigAPI.undo()
        assert 200 == result.code

        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = ConfigAPI.redo()
        assert 200 == result.code

        result = ConfigAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddAlreadyExist() {
        def configToAdd = BasicInstanceBuilder.getConfig()
        def result = ConfigAPI.create(configToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert (409 == result.code) || (400 == result.code)
    }

    //TEST UPDATE
    void testUpdateCorrect() {
        Config configToUpdate = BasicInstanceBuilder.getConfig()

        configToUpdate.value = "test2"

        def result = ConfigAPI.update(configToUpdate.key, configToUpdate.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        assert json.config.value== "test2"
    }
}
