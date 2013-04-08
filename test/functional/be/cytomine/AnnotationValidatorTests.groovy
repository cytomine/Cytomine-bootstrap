package be.cytomine

import be.cytomine.image.AbstractImage
import be.cytomine.image.AbstractImageGroup
import be.cytomine.image.AbstractImageService
import be.cytomine.image.ImagePropertiesService
import be.cytomine.image.server.ImageProperty
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.security.Group
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AbstractImageAPI
import be.cytomine.test.http.UserAnnotationAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/02/11
 * Time: 13:49
 * To change this template use File | Settings | File Templates.
 */
class AnnotationValidatorTests {

   public static SELF_INTERSECT = "POLYGON((13688 75041,13687 75040,13688 75041,13689 75041,13688 75041))"

   public static GEOMETRY_COLLECTION = "GEOMETRYCOLLECTION(POLYGON((14512 10384,14480 10384,14464 10400,14472 10400,14480 10408,14488 10400,14496 10400,14512 10384)),LINESTRING(14512 10384,14520 10384))"

   public static LINE_STRING = "LINESTRING(885.55108264715 1319.0620040002,885.55108264715 1321.0620040002)"

   public static MULTI_LINE_STRING = "MULTILINESTRING((33064 25416,33056 25408),(33056 25408,33048 25408),(33064 25416,33080 25416))"

   public static EMPTY_COLLECTION = "GEOMETRYCOLLECTION EMPTY"

    public static EMPTY_POLYGON = "POLYGON EMPTY"

    public void testAnnotationValid() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def json = annotationToAdd.encodeAsJSON()
        def result = UserAnnotationAPI.create(json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    public void testAnnotationNotValid() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.location = SELF_INTERSECT
        def result = UserAnnotationAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    public void testAnnotationGeometry() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.location = GEOMETRY_COLLECTION
        def result = UserAnnotationAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    public void testAnnotationGeometryCollectionEmpty() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.location = EMPTY_COLLECTION
        def result = UserAnnotationAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    public void testAnnotationGeometryPolygonEmpty() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.location = EMPTY_POLYGON
        def result = UserAnnotationAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    public void testAnnotationGeometryLineString() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.location = LINE_STRING
        def result = UserAnnotationAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }

    public void testAnnotationGeometryMultiLineString() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        def json = JSON.parse(annotationToAdd.encodeAsJSON())
        json.location = MULTI_LINE_STRING
        def result = UserAnnotationAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code
    }
}