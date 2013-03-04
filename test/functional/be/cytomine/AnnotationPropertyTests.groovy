package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationProperty
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationPropertyAPI
import be.cytomine.utils.UpdateData
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

class AnnotationPropertyTests {

    void testShowAnnotationProperty() {
        def annotationProperty = BasicInstance.createOrGetBasicAnnotationProperty()
        def result = AnnotationPropertyAPI.show(annotationProperty.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.id == annotationProperty.id
    }

    void testShowAnnotationPropertyNotExist() {
        def result = AnnotationPropertyAPI.show(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListByAnnotation() {
        def result = AnnotationPropertyAPI.listByAnnotation(BasicInstance.createOrGetBasicAnnotationProperty().retrieveAnnotationDomain().id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListByAnnotationNotExist() {
        def result = AnnotationPropertyAPI.listByAnnotation(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    //Test ListKey
    void testListKeyWithProject () {
        Project project = BasicInstance.createOrGetBasicProject()
        UserAnnotation userAnnotation = BasicInstance.createUserAnnotation(project)
        AnnotationProperty annotationProperty = BasicInstance.createAnnotationProperty(userAnnotation)

        def result = AnnotationPropertyAPI.listKeyWithProject(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert AnnotationPropertyAPI.containsStringInJSONList(annotationProperty.key,json);
        println("JSON - project: " + json)
    }

    void testListKeyWithProjectNotExist () {
        def result = AnnotationPropertyAPI.listKeyWithProject(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListKeyWithImage () {
        def result = AnnotationPropertyAPI.listKeyWithImage((BasicInstance.createOrGetBasicImageInstance()).id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        println("JSON - image: " + json)
    }

    void testListKeyWithImageNotExist () {
        def result = AnnotationPropertyAPI.listKeyWithImage(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteAnnotationProperty() {
        def annotationPropertyToDelete = BasicInstance.getBasicAnnotationPropertyNotExist()
        assert annotationPropertyToDelete.save(flush: true) != null

        def id = annotationPropertyToDelete.id
        def key = annotationPropertyToDelete.key
        def result = AnnotationPropertyAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        //UNDO & REDO

        result = AnnotationPropertyAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = AnnotationPropertyAPI.undo()
        assert 200 == result.code

        result = AnnotationPropertyAPI.show(id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = AnnotationPropertyAPI.redo()
        assert 200 == result.code

        result = AnnotationPropertyAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

    }

    void testDeleteAnnotationPropertyNotExist() {
        def result = AnnotationPropertyAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testAddAnnotationCorrect() {
        def annotationPropertyToAdd = BasicInstance.getBasicAnnotationPropertyNotExist()
        def result = AnnotationPropertyAPI.create( annotationPropertyToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def id =  result.data.id

        //UNDO & REDO

        result = AnnotationPropertyAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = AnnotationPropertyAPI.undo()
        assert 200 == result.code

        result = AnnotationPropertyAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = AnnotationPropertyAPI.redo()
        assert 200 == result.code

        result = AnnotationPropertyAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testAddAnnotationPropertyAlreadyExist() {
        def annotationPropertyToAdd = BasicInstance.createOrGetBasicAnnotationProperty()
        def result = AnnotationPropertyAPI.create(annotationPropertyToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }

    void testUpdateAnnotationPropertyCorrect() {
        AnnotationProperty annotationPropertyToAdd = BasicInstance.createOrGetBasicAnnotationProperty()
        def data = UpdateData.createUpdateSet(annotationPropertyToAdd)

        println annotationPropertyToAdd.annotationIdent + "-" + annotationPropertyToAdd.key

        def result = AnnotationPropertyAPI.update(data.oldData.id, data.newData, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        BasicInstance.compareAnnotationProperty(data.mapNew,json.annotationproperty)
    }

    void testUpdateAnnotationPropertyNotExist() {
        AnnotationProperty annotationPropertyOld = BasicInstance.createOrGetBasicAnnotationProperty()
        AnnotationProperty annotationPropertyNew = BasicInstance.getBasicAnnotationPropertyNotExist()
        annotationPropertyNew.save(flush: true)
        AnnotationProperty annotationPropertyToEdit = AnnotationProperty.get(annotationPropertyNew.id)
        def jsonAnnotationProperty = annotationPropertyToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotationProperty)
        jsonUpdate.key = annotationPropertyOld.key
        jsonUpdate.id = -99
        jsonAnnotationProperty = jsonUpdate.encodeAsJSON()
        def result = AnnotationPropertyAPI.update(-99, jsonAnnotationProperty, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testSelectCenterAnnotationCorrect() {
        AnnotationProperty annotationProperty = BasicInstance.createOrGetBasicAnnotationProperty()
        def user = BasicInstance.createOrGetBasicUser()
        def image = BasicInstance.createOrGetBasicImageInstance()

        def annotation = BasicInstance.getBasicUserAnnotationNotExist()
        annotation.location = new WKTReader().read("POLYGON ((0 0, 0 1000, 1000 1000, 1000 0, 0 0))")
        annotation.user = user
        annotation.image = image
        annotationProperty.annotation = annotation;
        annotationProperty.key = "TestCytomine"
        annotationProperty.value = "ValueTestCytomine"
        assert annotationProperty.save(flush: true) != null
        assert annotation.save(flush: true)  != null

        def result = AnnotationPropertyAPI.listAnnotationCenterPosition(user.id, image.id, "0,0,1000,1000","TestCytomine", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        println result
    }

    void testSelectCenterAnnotationNotCorrect() {
        AnnotationProperty annotationProperty = BasicInstance.createOrGetBasicAnnotationProperty()
        def user = BasicInstance.createOrGetBasicUser()
        def image = BasicInstance.createOrGetBasicImageInstance()

        def annotation = BasicInstance.getBasicUserAnnotationNotExist()
        annotation.location = new WKTReader().read("POLYGON ((0 0, 0 1000, 1000 1000, 1000 0, 0 0))")
        annotation.user = user
        annotation.image = image
        annotationProperty.annotation = annotation;
        annotationProperty.key = "TestCytomine"
        annotationProperty.value = "ValueTestCytomine"
        assert annotationProperty.save(flush: true) != null
        assert annotation.save(flush: true)  != null

        //Error IdUser
        def result = AnnotationPropertyAPI.listAnnotationCenterPosition(-99, image.id, "0,0,1000,1000","TestCytomine", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        //Error IdImage
        result = AnnotationPropertyAPI.listAnnotationCenterPosition(user.id, -99, "0,0,1000,1000","TestCytomine", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

}
