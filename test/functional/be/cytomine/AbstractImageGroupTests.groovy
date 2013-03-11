package be.cytomine

import be.cytomine.image.AbstractImage

import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AbstractImageGroupAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 22/02/11
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
class AbstractImageGroupTests {


    void testListAbstractImageGroupByAbstractImageWithCredential() {
        AbstractImage abstractImage = BasicInstanceBuilder.getAbstractImage()
        def result = AbstractImageGroupAPI.listByImage(abstractImage.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListAbstractImageGroupByAbstractImageWithAbstractImageNotExist() {
        def result = AbstractImageGroupAPI.listByImage(-99,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 404 == result.code
    }

//    void testListAbstractImageGroupByGroupWithCredential() {
//      Group group = BasicInstanceBuilder.getGroup()
//      def result = AbstractImageGroupAPI.listByGroup(group.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
//      assert 200 == result.code
//      def json = JSON.parse(result.data)
//      assert json.collection instanceof JSONArray
//    }

    void testListAbstractImageGroupByGroupWithGroupNotExist() {
        def result = AbstractImageGroupAPI.listByGroup(-99,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testGetAbstractImageGroupWithCredential() {
        def abstractImageGroupToAdd = BasicInstanceBuilder.getAbstractImageGroup()
        def result = AbstractImageGroupAPI.show(abstractImageGroupToAdd.abstractImage.id,abstractImageGroupToAdd.group.id,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testGetAbstractImageGroupWithCredentialNotExist() {
        def abstractImageGroupToAdd = BasicInstanceBuilder.getAbstractImageGroup()
        def result = AbstractImageGroupAPI.show( abstractImageGroupToAdd.abstractImage.id,-99,Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testAddAbstractImageGroupCorrect() {
        def abstractImageGroupToAdd = BasicInstanceBuilder.getAbstractImageGroupNotExist()
        abstractImageGroupToAdd.discard()
        String json = abstractImageGroupToAdd.encodeAsJSON()
        def result = AbstractImageGroupAPI.create(abstractImageGroupToAdd.abstractImage.id,abstractImageGroupToAdd.group.id,json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = AbstractImageGroupAPI.show(abstractImageGroupToAdd.abstractImage.id,abstractImageGroupToAdd.group.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testAddAbstractImageGroupAlreadyExist() {
        def abstractImageGroupToAdd = BasicInstanceBuilder.getAbstractImageGroupNotExist()
        abstractImageGroupToAdd.save(flush:true)
        abstractImageGroupToAdd.discard()
        String json = abstractImageGroupToAdd.encodeAsJSON()
        def result = AbstractImageGroupAPI.create(abstractImageGroupToAdd.abstractImage.id,abstractImageGroupToAdd.group.id,json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }

    void testAddAbstractImageGroupWithAbstractImageNotExist() {
        def abstractImageGroupToAdd = BasicInstanceBuilder.getAbstractImageGroupNotExist()
        abstractImageGroupToAdd.discard()
        def jsonUpdate = JSON.parse(abstractImageGroupToAdd.encodeAsJSON())
        jsonUpdate.abstractImage = -99
        def result = AbstractImageGroupAPI.create(-99,abstractImageGroupToAdd.group.id,jsonUpdate.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testAddAbstractImageGroupWithGroupNotExist() {
        def abstractImageGroupToAdd = BasicInstanceBuilder.getAbstractImageGroupNotExist()
        abstractImageGroupToAdd.discard()
        def jsonUpdate = JSON.parse(abstractImageGroupToAdd.encodeAsJSON())
        jsonUpdate.group = -99
        def result = AbstractImageGroupAPI.create(abstractImageGroupToAdd.abstractImage.id,-99,jsonUpdate.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    void testDeleteAbstractImageGroup() {
        def abstractImageGroupToDelete = BasicInstanceBuilder.getAbstractImageGroupNotExist()
        assert abstractImageGroupToDelete.save(flush: true)  != null
        def result = AbstractImageGroupAPI.delete(abstractImageGroupToDelete.abstractImage.id,abstractImageGroupToDelete.group.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def showResult = AbstractImageGroupAPI.show(abstractImageGroupToDelete.abstractImage.id,abstractImageGroupToDelete.group.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code
    }

    void testDeleteAbstractImageGroupNotExist() {
        def abstractImageGroupToDelete = BasicInstanceBuilder.getAbstractImageGroupNotExist()
        assert abstractImageGroupToDelete.save(flush: true)  != null
        def result = AbstractImageGroupAPI.delete(-99,-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
}
