package be.cytomine

import be.cytomine.processing.JobTemplate
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.JobAPI
import be.cytomine.test.http.JobParameterAPI
import be.cytomine.test.http.JobTemplateAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 9:11
 * To change this template use File | Settings | File Templates.
 */
class JobTemplateTests {

    void testGetJobTemplateWithCredential() {
        def template = BasicInstanceBuilder.getJobTemplate()
        def result = JobTemplateAPI.show(template.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.id.toString()== template.id.toString()

        result = JobAPI.show(template.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.id.toString()== template.id.toString()
    }

    void testListTemplateByProjectAndSoftware() {
        def template = BasicInstanceBuilder.getJobTemplate()
        def result = JobTemplateAPI.list(template.project.id, template.software.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert JobTemplateAPI.containsInJSONList(template.id,json)

        result = JobTemplateAPI.list(null, template.software.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = JobTemplateAPI.list(template.project.id, null, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        assert JobTemplateAPI.containsInJSONList(template.id,json)

        result = JobTemplateAPI.list(null, null, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = JobTemplateAPI.list(-99,null, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = JobTemplateAPI.list(null,-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code        
    }



    void testAddJobTemplateCorrect() {
        def template = BasicInstanceBuilder.getJobTemplateNotExist()
        def result = JobTemplateAPI.create(template.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        template = result.data
        Long idTemplate = template.id

        result = JobTemplateAPI.show(idTemplate, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = JobTemplateAPI.undo()
        assert 200 == result.code

        result = JobTemplateAPI.show(idTemplate, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = JobTemplateAPI.redo()
        assert 200 == result.code

        result = JobTemplateAPI.show(idTemplate, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

    }


    void testAddJobTemplateAlreadyExist() {
        def jobTemplate = BasicInstanceBuilder.getJobTemplateNotExist(true)
        log.info jobTemplate.encodeAsJSON()
        def result = JobTemplateAPI.create(jobTemplate.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }

    void testEditJobTemplate() {

        def template = BasicInstanceBuilder.getJobTemplate()
        def data = UpdateData.createUpdateSet(template,[project: [BasicInstanceBuilder.getProject(),BasicInstanceBuilder.getProjectNotExist(true)]])

        def result = JobTemplateAPI.update(template.id,data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idJobTemplate = json.jobtemplate.id
        def showResult = JobTemplateAPI.show(idJobTemplate,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)
    }

    void testDeleteJobTemplate() {
        def jobTemplateToDelete = BasicInstanceBuilder.getJobTemplateNotExist()
        assert jobTemplateToDelete.save(flush: true) != null
        def idTemplate = jobTemplateToDelete.id

        def result = JobTemplateAPI.delete(jobTemplateToDelete.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def showResult = JobTemplateAPI.show(jobTemplateToDelete.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code

        result = JobTemplateAPI.undo()
        assert 200 == result.code

        result = JobTemplateAPI.show(idTemplate,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = JobTemplateAPI.redo()
        assert 200 == result.code

        result = JobTemplateAPI.show(idTemplate,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteJobTemplateNoExist() {
        def jobTemplateToDelete = BasicInstanceBuilder.getJobTemplateNotExist()
        def result = JobTemplateAPI.delete(jobTemplateToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testAddJobTemplateCorrectWorkflow() {
        def template = BasicInstanceBuilder.getJobTemplateNotExist()
        def result = JobTemplateAPI.create(template.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        template = result.data
        Long idTemplate = template.id

        result = JobTemplateAPI.show(idTemplate, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def parameter = BasicInstanceBuilder.getJobParameterNotExist()
        def json = parameter.encodeAsJSON()
        def jsonUpdate = JSON.parse(json)
        jsonUpdate.job = idTemplate
        json = jsonUpdate.toString()
        log.info json
        result = JobParameterAPI.create(json, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = JobTemplateAPI.show(idTemplate, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.jobParameters.size()==1
    }


}
