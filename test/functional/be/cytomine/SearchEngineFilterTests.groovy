package be.cytomine

import be.cytomine.search.SearchEngineFilter
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.SearchEngineFilterAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * User: rhoyoux
 * Date: 30/10/14
 * Time: 10:55
 */
class SearchEngineFilterTests {

    void testListSearchEngineFilterWithCredential() {
        def result = SearchEngineFilterAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        //assert json['collection'] instanceof JSONArray
    }

    void testListSearchEngineFilterWithoutCredential() {
        def result = SearchEngineFilterAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }

    void testShowSearchEngineFilterWithCredential() {
        def result = SearchEngineFilterAPI.show(BasicInstanceBuilder.getSearchEngineFilter().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddSearchEngineFilterCorrect() {
        def filterToAdd = BasicInstanceBuilder.getSearchEngineFilterNotExist()
        def result = SearchEngineFilterAPI.create(filterToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idFilter = result.data.id

        result = SearchEngineFilterAPI.show(idFilter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = SearchEngineFilterAPI.undo()
        assert 200 == result.code

        result = SearchEngineFilterAPI.show(idFilter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = SearchEngineFilterAPI.redo()
        assert 200 == result.code

        result = SearchEngineFilterAPI.show(idFilter, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddSearchEngineFilterAlreadyExist() {
        def filterToAdd = BasicInstanceBuilder.getSearchEngineFilter()
        def result = SearchEngineFilterAPI.create(filterToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }

    void testDeleteSearchEngineFilter() {
        def filterToDelete = BasicInstanceBuilder.getSearchEngineFilterNotExist()
        assert filterToDelete.save(flush: true)!= null
        def id = filterToDelete.id
        def result = SearchEngineFilterAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def showResult = SearchEngineFilterAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code

        result = SearchEngineFilterAPI.undo()
        assert 200 == result.code

        result = SearchEngineFilterAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = SearchEngineFilterAPI.redo()
        assert 200 == result.code

        result = SearchEngineFilterAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testDeleteSearchEngineFilterNotExist() {
        def result = SearchEngineFilterAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

}
