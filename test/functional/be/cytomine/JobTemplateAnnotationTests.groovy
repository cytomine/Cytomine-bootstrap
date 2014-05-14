package be.cytomine

import be.cytomine.processing.JobTemplateAnnotation
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.JobTemplateAnnotationAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class JobTemplateAnnotationTests {

    void testShowJobTemplateAnnotation() {
        JobTemplateAnnotation jobTemplateAnnotation = BasicInstanceBuilder.getJobTemplateAnnotation()
        def result = JobTemplateAnnotationAPI.show(jobTemplateAnnotation.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = JobTemplateAnnotationAPI.show(-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

 
     void testListJobTemplateAnnotation() {
         JobTemplateAnnotation jobTemplateAnnotation = BasicInstanceBuilder.getJobTemplateAnnotation()
         def result = JobTemplateAnnotationAPI.list(jobTemplateAnnotation.jobTemplate.id,jobTemplateAnnotation.annotationIdent,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray
         assert JobTemplateAnnotationAPI.containsInJSONList(jobTemplateAnnotation.id,json)

         result = JobTemplateAnnotationAPI.list(-99,-99,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == result.code
     }

     void testAddJobTemplateAnnotationCorrect() {
         def JobTemplateAnnotationToAdd = BasicInstanceBuilder.getJobTemplateAnnotationNotExist()
         def result = JobTemplateAnnotationAPI.create(JobTemplateAnnotationToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
         int idJobTemplateAnnotation = result.data.id
   
         result = JobTemplateAnnotationAPI.show(idJobTemplateAnnotation, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
     }
 
     void testAddJobTemplateAnnotationWithBadJobTemplate() {
         JobTemplateAnnotation JobTemplateAnnotationToAdd = BasicInstanceBuilder.getJobTemplateAnnotation()
         JobTemplateAnnotation JobTemplateAnnotationToEdit = JobTemplateAnnotation.get(JobTemplateAnnotationToAdd.id)
         def jsonJobTemplateAnnotation = JobTemplateAnnotationToEdit.encodeAsJSON()
         def jsonUpdate = JSON.parse(jsonJobTemplateAnnotation)
         jsonUpdate.jobTemplate = -99
         jsonJobTemplateAnnotation = jsonUpdate.toString()
         def result = JobTemplateAnnotationAPI.create(jsonJobTemplateAnnotation, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 400 == result.code
     }

    void testAddJobTemplateAnnotationWithBadAnnotation() {
        JobTemplateAnnotation JobTemplateAnnotationToAdd = BasicInstanceBuilder.getJobTemplateAnnotation()
        JobTemplateAnnotation JobTemplateAnnotationToEdit = JobTemplateAnnotation.get(JobTemplateAnnotationToAdd.id)
        def jsonJobTemplateAnnotation = JobTemplateAnnotationToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonJobTemplateAnnotation)
        jsonUpdate.annotationIdent = -99
        jsonJobTemplateAnnotation = jsonUpdate.toString()
        def result = JobTemplateAnnotationAPI.create(jsonJobTemplateAnnotation, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

     void testDeleteJobTemplateAnnotation() {
         def JobTemplateAnnotationToDelete = BasicInstanceBuilder.getJobTemplateAnnotationNotExist()
         assert JobTemplateAnnotationToDelete.save(flush: true)!= null
         def id = JobTemplateAnnotationToDelete.id
         def result = JobTemplateAnnotationAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code
 
         def showResult = JobTemplateAnnotationAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == showResult.code
     }
 
     void testDeleteJobTemplateAnnotationNotExist() {
         def result = JobTemplateAnnotationAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 404 == result.code
     }
}
