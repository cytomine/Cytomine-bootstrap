package be.cytomine

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.ontology.Annotation
import be.cytomine.test.Infos
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.test.http.AnnotationAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTests extends functionaltestplugin.FunctionalTestCase {

    void testGetAnnotationWithCredential() {
        def annotation = BasicInstance.createOrGetBasicAnnotation()
        def result = AnnotationAPI.show(annotation.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListAnnotationsWithCredential() {
        BasicInstance.createOrGetBasicAnnotation()
        def result = AnnotationAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListAnnotationsByImageWithCredential() {
        Annotation annotation = BasicInstance.createOrGetBasicAnnotation()
        def result = AnnotationAPI.listByImage(annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListAnnotationsByImageAndUserWithCredential() {
        Annotation annotation = BasicInstance.createOrGetBasicAnnotation()
        def result = AnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testAddAnnotationCorrect() {
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        def result = AnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        int idAnnotation = result.data.id

        result = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = AnnotationAPI.undo()
        assertEquals(200, result.code)

        result = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)

        result = AnnotationAPI.redo()
        assertEquals(200, result.code)

        result = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddAnnotationCorrectWithTerm() {
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        Long idTerm1 = BasicInstance.createOrGetBasicTerm().id
        Long idTerm2 = BasicInstance.createOrGetAnotherBasicTerm().id

        def annotationWithTerm = JSON.parse((String)annotationToAdd.encodeAsJSON())
        annotationWithTerm.term = [idTerm1, idTerm2]

        def result = AnnotationAPI.create(annotationWithTerm.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        int idAnnotation = result.data.id

        result = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = AnnotationAPI.undo()
        assertEquals(200, result.code)

        result = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)

        result = AnnotationAPI.redo()
        assertEquals(200, result.code)

        result = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddAnnotationBadGeom() {
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POINT(BAD GEOMETRY)'

        Long idTerm1 = BasicInstance.createOrGetBasicTerm().id
        Long idTerm2 = BasicInstance.createOrGetAnotherBasicTerm().id
        updateAnnotation.term = [idTerm1, idTerm2]

        def result = AnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testAddAnnotationBadGeomEmpty() {
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POLYGON EMPTY'
        def result = AnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testAddAnnotationImageNotExist() {
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.image = -99
        def result = AnnotationAPI.create(updateAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testEditAnnotation() {
        Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        def result = AnnotationAPI.update(annotationToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAnnotation = json.annotation.id

        def showResult = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareAnnotation(result.mapNew, json)

        showResult = AnnotationAPI.undo()
        assertEquals(200, result.code)
        showResult = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstance.compareAnnotation(result.mapOld, JSON.parse(showResult.data))

        showResult = AnnotationAPI.redo()
        assertEquals(200, result.code)
        showResult = AnnotationAPI.show(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        BasicInstance.compareAnnotation(result.mapNew, JSON.parse(showResult.data))
    }

    void testEditAnnotationNotExist() {
        Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
        def jsonAnnotation = JSON.parse((String)annotationToEdit.encodeAsJSON())
        jsonAnnotation.id = "-99"
        def result = AnnotationAPI.update(annotationToAdd.id, jsonAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testEditAnnotationWithBadGeometry() {
        Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        def jsonAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        jsonAnnotation.location = "POINT (BAD GEOMETRY)"
        def result = AnnotationAPI.update(annotationToAdd.id, jsonAnnotation.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testDeleteAnnotation() {
        def annotationToDelete = BasicInstance.getBasicAnnotationNotExist()
        assert annotationToDelete.save(flush: true)  != null
        def id = annotationToDelete.id
        def result = AnnotationAPI.delete(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        def showResult = AnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, showResult.code)

        result = AnnotationAPI.undo()
        assertEquals(200, result.code)

        result = AnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = AnnotationAPI.redo()
        assertEquals(200, result.code)

        result = AnnotationAPI.show(id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteAnnotationNotExist() {
        def result = AnnotationAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteAnnotationWithData() {
        def annotTerm = BasicInstance.createOrGetBasicAnnotationTerm()
        def annotationToDelete = annotTerm.annotation
        def result = AnnotationAPI.delete(annotationToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

}
