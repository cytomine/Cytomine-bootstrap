package be.cytomine

import be.cytomine.security.User
import be.cytomine.test.Infos
import be.cytomine.test.BasicInstance
import be.cytomine.test.http.AnnotationTermAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.ontology.Annotation
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.ontology.Term
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.security.UserJob

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 10/02/11
 * Time: 9:31
 * To change this template use File | Settings | File Templates.
 */
class AlgoAnnotationTermTests extends functionaltestplugin.FunctionalTestCase {

    void testGetAlgoAnnotationTermWithCredential() {
       def annotationTermToAdd = BasicInstance.createOrGetBasicAlgoAnnotationTerm()
       def result = AnnotationTermAPI.showAnnotationTerm(annotationTermToAdd.annotation.id,annotationTermToAdd.term.id,annotationTermToAdd.userJob.id,annotationTermToAdd.userJob.username,"PasswordUserJob")
       assertEquals(200,result.code)
       def json = JSON.parse(result.data)
       assert json instanceof JSONObject
     }

     void testAddAlgoAnnotationTermCorrect() {
       def annotationTermToAdd = BasicInstance.getBasicAlgoAnnotationTermNotExist()
       UserJob currentUserJob = annotationTermToAdd.userJob
       Infos.addUserRight(currentUserJob.user,annotationTermToAdd.annotation.project)
       annotationTermToAdd.discard()
       String jsonAnnotationTerm = annotationTermToAdd.encodeAsJSON()
       def result = AnnotationTermAPI.createAnnotationTerm(jsonAnnotationTerm,annotationTermToAdd.userJob.username,"PasswordUserJob")

       assertEquals(200,result.code)
   
       result = AnnotationTermAPI.showAnnotationTerm(annotationTermToAdd.annotation.id,annotationTermToAdd.term.id,annotationTermToAdd.userJob.id,annotationTermToAdd.userJob.username,"PasswordUserJob")
       assertEquals(200,result.code)
   
     }
}
