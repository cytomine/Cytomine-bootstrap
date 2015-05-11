package be.cytomine

import be.cytomine.security.User
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.SearchEngineFilterAPI
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * User: rhoyoux
 * Date: 30/10/14
 * Time: 10:55
 */
class SearchEngineFilterTests {

    void testListAllSearchEngineFilterWithCredential() {
        def result = SearchEngineFilterAPI.listAll(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListAllSearchEngineFilterWithoutCredential() {
        def result = SearchEngineFilterAPI.listAll(Infos.BADLOGIN, Infos.BADPASSWORD)
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
        log.info(JSON.parse(filterToAdd.filters).words)
        User user = BasicInstanceBuilder.getUser()

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

    void testAddSearchEngineFilterIncorrect() {
        def filterToAdd = BasicInstanceBuilder.getSearchEngineFilterNotExist()
        JSONObject filters = JSON.parse(filterToAdd.filters)
        def words =  JSONUtils.getJSONList(JSONUtils.getJSONAttrStr(filters, "words", false))
        log.info("filter words " + words)
        words.each { it -> log.info(it)}

        filters.put("words", [] as JSON)
        filterToAdd.name = filterToAdd.name+2
        filterToAdd.filters = filters.toString()
        log.info("filter name " + filterToAdd.name)
        log.info("filter filters " + filterToAdd.filters)

        User user = BasicInstanceBuilder.getUser()
        def result = SearchEngineFilterAPI.create(filterToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code // cannot search with no words

        filters.put("projects", [64] as JSON)
        filterToAdd.name = filterToAdd.name+2
        filterToAdd.filters = filters.toString()
        log.info("filter name " + filterToAdd.name)
        log.info("filter filters " + filterToAdd.filters)

        result = SearchEngineFilterAPI.create(filterToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code // cannot search non existing project
    }

    void testAddSearchEngineFilterAlreadyExist() {
        def filterToAdd = BasicInstanceBuilder.getSearchEngineFilter()
        User user = BasicInstanceBuilder.getUser()
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
