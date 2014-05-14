package be.cytomine

import be.cytomine.processing.RoiAnnotation
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.RoiAnnotationAPI
import be.cytomine.utils.JSONUtils
import be.cytomine.utils.UpdateData
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 8/02/11
 * Time: 9:01
 * To change this template use File | Settings | File Templates.
 */
class RoiAnnotationTests {

    void testGetRoiAnnotationWithCredential() {
        def annotation = BasicInstanceBuilder.getRoiAnnotation()
        def result = RoiAnnotationAPI.show(annotation.id, Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddRoiAnnotationCorrect() {
        def annotationToAdd = BasicInstanceBuilder.getRoiAnnotation()
        def result = RoiAnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idAnnotation = result.data.id

        result = RoiAnnotationAPI.show(idAnnotation, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = RoiAnnotationAPI.undo()
        assert 200 == result.code

        result = RoiAnnotationAPI.show(idAnnotation, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = RoiAnnotationAPI.redo()
        assert 200 == result.code

        result = RoiAnnotationAPI.show(idAnnotation, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddRoiAnnotationMultipleCorrect() {
        def annotationToAdd1 = BasicInstanceBuilder.getRoiAnnotation()
        def annotationToAdd2 = BasicInstanceBuilder.getRoiAnnotation()
        def annotations = []
        annotations << JSON.parse(annotationToAdd1.encodeAsJSON())
        annotations << JSON.parse(annotationToAdd2.encodeAsJSON())
        def result = RoiAnnotationAPI.create(JSONUtils.toJSONString(annotations), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddRoiAnnotationBadGeom() {
        def annotationToAdd = BasicInstanceBuilder.getRoiAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POINT(BAD GEOMETRY)'

        Long idTerm1 = BasicInstanceBuilder.getTerm().id
        Long idTerm2 = BasicInstanceBuilder.getAnotherBasicTerm().id
        updateAnnotation.term = [idTerm1, idTerm2]

        def result = RoiAnnotationAPI.create(updateAnnotation.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

    void testAddRoiAnnotationBadGeomEmpty() {
        def annotationToAdd = BasicInstanceBuilder.getRoiAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.location = 'POLYGON EMPTY'
        def result = RoiAnnotationAPI.create(updateAnnotation.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

    void testAddRoiAnnotationImageNotExist() {
        def annotationToAdd = BasicInstanceBuilder.getRoiAnnotation()
        def updateAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        updateAnnotation.image = -99
        def result = RoiAnnotationAPI.create(updateAnnotation.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

    void testEditRoiAnnotation() {
        RoiAnnotation annotationToAdd = BasicInstanceBuilder.getRoiAnnotation()
        def data = UpdateData.createUpdateSet(
                BasicInstanceBuilder.getRoiAnnotation(),
                [location: [new WKTReader().read("POLYGON ((2107 2160, 2047 2074, 1983 2168, 1983 2168, 2107 2160))"),new WKTReader().read("POLYGON ((1983 2168, 2107 2160, 2047 2074, 1983 2168, 1983 2168))")]]
        )

        def result = RoiAnnotationAPI.update(annotationToAdd.id, data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idAnnotation = json.roiannotation.id

        def showResult = RoiAnnotationAPI.show(idAnnotation, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)

        showResult = RoiAnnotationAPI.undo()
        assert 200 == result.code
        showResult = RoiAnnotationAPI.show(idAnnotation, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))

        showResult = RoiAnnotationAPI.redo()
        assert 200 == result.code
        showResult = RoiAnnotationAPI.show(idAnnotation, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
    }

    void testEditRoiAnnotationNotExist() {
        RoiAnnotation annotationToAdd = BasicInstanceBuilder.getRoiAnnotation()
        RoiAnnotation annotationToEdit = RoiAnnotation.get(annotationToAdd.id)
        def jsonAnnotation = JSON.parse((String)annotationToEdit.encodeAsJSON())
        jsonAnnotation.id = "-99"
        def result = RoiAnnotationAPI.update(annotationToAdd.id, jsonAnnotation.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testEditRoiAnnotationWithBadGeometry() {
        RoiAnnotation annotationToAdd = BasicInstanceBuilder.getRoiAnnotation()
        def jsonAnnotation = JSON.parse((String)annotationToAdd.encodeAsJSON())
        jsonAnnotation.location = "POINT (BAD GEOMETRY)"
        def result = RoiAnnotationAPI.update(annotationToAdd.id, jsonAnnotation.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

    void testDeleteRoiAnnotation() {
        def annotationToDelete = BasicInstanceBuilder.getRoiAnnotationNotExist()
        assert annotationToDelete.save(flush: true)  != null
        def id = annotationToDelete.id
        def result = RoiAnnotationAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def showResult = RoiAnnotationAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code

        result = RoiAnnotationAPI.undo()
        assert 200 == result.code

        result = RoiAnnotationAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = RoiAnnotationAPI.redo()
        assert 200 == result.code

        result = RoiAnnotationAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testDeleteRoiAnnotationNotExist() {
        def result = RoiAnnotationAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testListAlgoAnnotationByImageAndUser() {
        RoiAnnotation annotation = BasicInstanceBuilder.getRoiAnnotation()
        RoiAnnotation annotationWith2Term = BasicInstanceBuilder.getRoiAnnotation()


        def result = RoiAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray


        //very small bbox, hight annotation number
        String bbox = "1,1,100,100"
        result = RoiAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, bbox, true,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray


        result = RoiAnnotationAPI.listByImageAndUser(-99, annotation.user.id, bbox, false,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
        result = RoiAnnotationAPI.listByImageAndUser(annotation.image.id, -99, bbox, false,null,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
}
