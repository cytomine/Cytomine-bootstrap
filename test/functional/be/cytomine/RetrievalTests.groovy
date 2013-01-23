package be.cytomine

import be.cytomine.test.Infos

import grails.converters.JSON

import be.cytomine.test.http.RetrievalAPI
import be.cytomine.utils.RetrievalHttpUtils
import be.cytomine.utils.BasicInstance

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 */
class RetrievalTests extends functionaltestplugin.FunctionalTestCase {


    //Mock doesn't work with bamboo sometimes...

//    void testRetrievalResult() {
//        def project = BasicInstance.createOrGetBasicProject()
//        def annotation1 = BasicInstance.createUserAnnotation(project)
//        def annotation2 = BasicInstance.createUserAnnotation(project)
//        def annotation3 = BasicInstance.createUserAnnotation(project)
//
//        //mock retrieval response with empty JSON
//        RetrievalHttpUtils.metaClass.'static'.getPostSearchResponse  = {String URL, String resource, AnnotationDomain annotation, String urlAnnotation, List<Long> projectsSearch-> // stuff }
//            println "getPostSearchResponse mocked"
//            def a1 = '{"id":'+annotation1.id+',"sim":0.10}'
//            def a2 = '{"id":'+annotation2.id+',"sim":0.05}'
//            def a3 = '{"id":'+annotation3.id+',"sim":0.02}'
//            return "[$a1,$a2,$a3]"
//        }
//
//        def result = RetrievalAPI.getResults(annotation1.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(200,result.code)
//        println result.data
//        def json = JSON.parse(result.data)
//        assertEquals(3-1,json.annotation.size())
//    }
//
//    void testRetrievalNoResult() {
//        //mock retrieval response with empty JSON
//        RetrievalHttpUtils.metaClass.'static'.getPostSearchResponse  = {String URL, String resource, AnnotationDomain annotation, String urlAnnotation, List<Long> projectsSearch-> // stuff }
//            println "getPostSearchResponse mocked"
//            return "[]"
//        }
//        def annotation = BasicInstance.createOrGetBasicUserAnnotation()
//        def result = RetrievalAPI.getResults(annotation.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
//        assertEquals(200,result.code)
//        println result.data
//        def json = JSON.parse(result.data)
//        assertEquals(0,json.annotation.size())
//    }

    void testRetrievalWithAnnotationNotExist() {
        def result = RetrievalAPI.getResults(-99,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404,result.code)
    }
}
