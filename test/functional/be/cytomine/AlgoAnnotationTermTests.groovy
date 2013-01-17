package be.cytomine

import be.cytomine.security.UserJob
import be.cytomine.utils.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationTermAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:31
 * To change this template use File | Settings | File Templates.
 */
class AlgoAnnotationTermTests extends functionaltestplugin.FunctionalTestCase {

    void testListAlgoAnnotationTerm() {
        def annotationTermToAdd = BasicInstance.createOrGetBasicAlgoAnnotationTerm()
        def result = AnnotationTermAPI.listAnnotationTerm(annotationTermToAdd.retrieveAnnotationDomain().id,annotationTermToAdd.userJob.username,"PasswordUserJob")
        assertEquals(200,result.code)
        def json = JSON.parse(result.data)
    }


    void testGetAlgoAnnotationTermWithCredential() {
        def annotationTermToAdd = BasicInstance.createOrGetBasicAlgoAnnotationTerm()
        def result = AnnotationTermAPI.showAnnotationTerm(annotationTermToAdd.retrieveAnnotationDomain().id,annotationTermToAdd.term.id,annotationTermToAdd.userJob.id,annotationTermToAdd.userJob.username,"PasswordUserJob")
        assertEquals(200,result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }


    void testGetAlgoAnnotationTerm() {
        def annotationTermToAdd = BasicInstance.createOrGetBasicAlgoAnnotationTerm()
        def result = AnnotationTermAPI.showAnnotationTerm(annotationTermToAdd.retrieveAnnotationDomain().id,annotationTermToAdd.term.id,null,annotationTermToAdd.userJob.username,"PasswordUserJob")
        assertEquals(200,result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testAddAlgoAnnotationTermCorrect() {
        def annotationTermToAdd = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        UserJob currentUserJob = annotationTermToAdd.userJob
        Infos.addUserRight(currentUserJob.user,annotationTermToAdd.retrieveAnnotationDomain().project)
        annotationTermToAdd.discard()
        String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
        def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,annotationTermToAdd.userJob.username,"PasswordUserJob")
        assertEquals(200,result.code)
        log.info "1="+annotationTermToAdd.retrieveAnnotationDomain().id
        log.info "2="+annotationTermToAdd.term.id
        log.info "3="+annotationTermToAdd.userJob.id
        result = AnnotationTermAPI.showAnnotationTerm(
                annotationTermToAdd.retrieveAnnotationDomain().id,
                annotationTermToAdd.term.id,
                annotationTermToAdd.userJob.id,
                annotationTermToAdd.userJob.username,
                "PasswordUserJob"
        )
        assertEquals(200,result.code)
    }
}
