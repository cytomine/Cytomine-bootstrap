package be.cytomine

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.test.BasicInstance
import be.cytomine.ontology.Annotation
import be.cytomine.test.Infos
import be.cytomine.test.HttpClient
import com.vividsolutions.jts.io.WKTReader
import be.cytomine.security.User
import be.cytomine.image.AbstractImage
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.image.ImageInstance
import be.cytomine.test.http.AnnotationAPI
import be.cytomine.security.SecUser
import org.codehaus.groovy.grails.commons.ApplicationHolder

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTests extends functionaltestplugin.FunctionalTestCase {
    def springSecurityService
    void testGetAnnotationWithCredential() {
        Annotation annotation = BasicInstance.createOrGetBasicAnnotation()
        SecUser user = SecUser.findByUsername(Infos.GOODLOGIN)
        def springSecurityService = ApplicationHolder.application.getMainContext().getBean("springSecurityService")
        def result = AnnotationAPI.showAnnotation(annotation.id, Infos.GOODLOGIN,Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListAnnotationsWithCredential() {
        log.info("create annotation")
        Annotation annotation = BasicInstance.createOrGetBasicAnnotation()
        def result = AnnotationAPI.listAnnotation(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListAnnotationsByImageWithCredential() {
        log.info("create annotation")
        Annotation annotation = BasicInstance.createOrGetBasicAnnotation()
        ImageInstance image = BasicInstance.createOrGetBasicImageInstance()
        def result = AnnotationAPI.listAnnotationByImage(image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListAnnotationsByImageAndUserWithCredential() {
        log.info("create annotation")
        Annotation annotation = BasicInstance.createOrGetBasicAnnotation()
        ImageInstance image = BasicInstance.createOrGetBasicImageInstance()
        User user = BasicInstance.createOrGetBasicUser()
        def result = AnnotationAPI.listAnnotationByImageAndUser(image.id, user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testAddAnnotationCorrect() {
        log.info("create annotation")
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        String jsonAnnotation = annotationToAdd.encodeAsJSON()
        def result = AnnotationAPI.createAnnotation(jsonAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(200, result.code)
        int idAnnotation = result.data.id
        log.info("check if object " + idAnnotation + " exist in DB")
        result = AnnotationAPI.showAnnotation(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        log.info("test undo")
        result = AnnotationAPI.undo()
        assertEquals(200, result.code)
        log.info("check if object " + idAnnotation + " not exist in DB")
        result = AnnotationAPI.showAnnotation(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
        log.info("test redo")
        result = AnnotationAPI.redo()
        assertEquals(200, result.code)
        log.info("check if object " + idAnnotation + " exist in DB")
        result = AnnotationAPI.showAnnotation(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddAnnotationCorrectWithTerm() {
        log.info("create annotation")
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        String jsonAnnotation = annotationToAdd.encodeAsJSON()

        def annotationWithTerm = JSON.parse(jsonAnnotation)

        Long idTerm1 = BasicInstance.createOrGetBasicTerm().id
        Long idTerm2 = BasicInstance.createOrGetAnotherBasicTerm().id

        annotationWithTerm.term = [idTerm1, idTerm2]
        jsonAnnotation = annotationWithTerm.encodeAsJSON()

        log.info("START")
        def result = AnnotationAPI.createAnnotation(jsonAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("END")
        log.info("check response")
        assertEquals(200, result.code)
        int idAnnotation = result.data.id
        log.info("check if object " + idAnnotation + " exist in DB")
        result = AnnotationAPI.showAnnotation(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        log.info("test undo")
        result = AnnotationAPI.undo()
        assertEquals(200, result.code)
        log.info("check if object " + idAnnotation + " not exist in DB")
        result = AnnotationAPI.showAnnotation(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
        log.info("test redo")
        result = AnnotationAPI.redo()
        assertEquals(200, result.code)
        log.info("check if object " + idAnnotation + " exist in DB")
        result = AnnotationAPI.showAnnotation(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddAnnotationBadGeom() {
        log.info("create annotation")
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        String jsonAnnotation = annotationToAdd.encodeAsJSON()
        def updateAnnotation = JSON.parse(jsonAnnotation)
        updateAnnotation.location = 'POINT(BAD GEOMETRY)'
        Long idTerm1 = BasicInstance.createOrGetBasicTerm().id
        Long idTerm2 = BasicInstance.createOrGetAnotherBasicTerm().id
        updateAnnotation.term = [idTerm1, idTerm2]

        jsonAnnotation = updateAnnotation.encodeAsJSON()

        def result = AnnotationAPI.createAnnotation(jsonAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testAddAnnotationBadGeomEmpty() {

        log.info("create annotation")
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        String jsonAnnotation = annotationToAdd.encodeAsJSON()
        def updateAnnotation = JSON.parse(jsonAnnotation)
        updateAnnotation.location = 'POLYGON EMPTY'
        jsonAnnotation = updateAnnotation.encodeAsJSON()

        def result = AnnotationAPI.createAnnotation(jsonAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)

    }

    void testAddAnnotationScanNotExist() {

        log.info("create annotation")
        def annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        def jsonAnnotation = annotationToAdd.encodeAsJSON()
        def updateAnnotation = JSON.parse(jsonAnnotation)
        updateAnnotation.image = -99
        jsonAnnotation = updateAnnotation.encodeAsJSON()

        def result = AnnotationAPI.createAnnotation(jsonAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)
    }

    void testEditAnnotation() {

        Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        def result = AnnotationAPI.updateAnnotation(annotationToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check responsex:" + result)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAnnotation = json.annotation.id
        def showResult = AnnotationAPI.showAnnotation(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareAnnotation(result.mapNew, json)

        log.info("test undo")
        showResult = AnnotationAPI.undo()
        assertEquals(200, result.code)
        showResult = AnnotationAPI.showAnnotation(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)

        BasicInstance.compareAnnotation(result.mapOld, json)

        log.info("test redo")
        showResult = AnnotationAPI.redo()
        assertEquals(200, result.code)
        showResult = AnnotationAPI.showAnnotation(idAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareAnnotation(result.mapNew, json)
    }

    void testEditAnnotationNotExist() {

        String oldGeom = "POLYGON ((548 1611, 716 1737, 678 1527, 548 1611))"
        String newGeom = "POLYGON ((548 1611, 678 1527, 716 1737, 548 1611))"

        /* Create a old annotation with point 1111 1111 */
        log.info("create annotation")
        Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        annotationToAdd.location = new WKTReader().read(oldGeom)
        annotationToAdd.save(flush: true)

        /* Encode a niew annotation with point 9999 9999 */
        Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
        def jsonAnnotation = annotationToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotation)
        jsonUpdate.location = newGeom
        jsonUpdate.id = "-99"
        jsonAnnotation = jsonUpdate.encodeAsJSON()

        def result = AnnotationAPI.updateAnnotation(annotationToAdd.id, jsonAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)

        log.info("check response")
        assertEquals(404, result.code)

    }

    void testEditAnnotationWithBadGeometry() {

        String oldGeom = "POLYGON ((548 1611, 716 1737, 678 1527, 548 1611))"
        String newGeom = "POINT (BAD GEOMETRY)"


        log.info("create annotation")
        Annotation annotationToAdd = BasicInstance.createOrGetBasicAnnotation()
        annotationToAdd.location = new WKTReader().read(oldGeom)
        annotationToAdd.save(flush: true)

        Annotation annotationToEdit = Annotation.get(annotationToAdd.id)
        def jsonAnnotation = annotationToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonAnnotation)
        jsonUpdate.location = newGeom
        jsonAnnotation = jsonUpdate.encodeAsJSON()

        def result = AnnotationAPI.updateAnnotation(annotationToAdd.id, jsonAnnotation, Infos.GOODLOGIN, Infos.GOODPASSWORD)

        log.info("check response")
        assertEquals(400, result.code)

    }

    void testDeleteAnnotation() {
        log.info("create annotation")
        def annotationToDelete = BasicInstance.getBasicAnnotationNotExist()
        assert annotationToDelete.save(flush: true) != null
        def idImage = annotationToDelete.id
        log.info("delete project")
        def result = AnnotationAPI.deleteAnnotation(annotationToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def showResult = AnnotationAPI.showAnnotation(annotationToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response:" + response)
        assertEquals(404, showResult.code)

        log.info("test undo")
        result = AnnotationAPI.undo()
        assertEquals(200, result.code)

        log.info("check if object " + idImage + " not exist in DB")
        result = AnnotationAPI.showAnnotation(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        log.info("test redo")
        result = AnnotationAPI.redo()
        assertEquals(200, result.code)

        log.info("check if object " + idImage + " not exist in DB")
        result = AnnotationAPI.showAnnotation(idImage, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteAnnotationNotExist() {
        def result = AnnotationAPI.deleteAnnotation(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteAnnotationWithData() {
        log.info("create annotation")
        def annotTerm = BasicInstance.createOrGetBasicAnnotationTerm()
        def annotationToDelete = annotTerm.annotation
        def result = AnnotationAPI.deleteAnnotation(annotationToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        log.info("check response")
        assertEquals(200, result.code)
    }

}
