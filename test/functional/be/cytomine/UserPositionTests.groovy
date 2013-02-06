package be.cytomine

import be.cytomine.test.Infos

import be.cytomine.test.BasicInstance

import grails.converters.JSON

import be.cytomine.test.http.UserPositionAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class UserPositionTests  {

    static def create(Long idImage, def json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/imageinstance/$idImage/position.json"
        def result = doPOST(URL,json,username,password)
        return result
    }

    void testListByUser() {
        def image = BasicInstance.createOrGetBasicImageInstance()
       def result = UserPositionAPI.listLastByUser(image.id,BasicInstance.newUser.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
   }

    void testListByProject() {
        def image = BasicInstance.createOrGetBasicImageInstance()
       def result = UserPositionAPI.listLastByProject(image.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
   }

    void testListByImage() {
        def image = BasicInstance.createOrGetBasicImageInstance()
       def result = UserPositionAPI.listLastByImage(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
       assert 200 == result.code
   }

    void testAddPosition() {
        def image = BasicInstance.createOrGetBasicImageInstance()
        def json = JSON.parse("{image:${image.id},lon:100,lat:100}")

        def result = UserPositionAPI.create(image.id, json.toString(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = UserPositionAPI.listLastByUser(image.id,BasicInstance.newUser.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = UserPositionAPI.listLastByProject(image.project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = UserPositionAPI.listLastByImage(image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

}
