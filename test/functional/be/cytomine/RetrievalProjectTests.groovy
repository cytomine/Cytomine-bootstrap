package be.cytomine

import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 17/02/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
class RetrievalProjectTests extends functionaltestplugin.FunctionalTestCase {

    void testListRetrievalProjectWithCredential() {
        Project project = BasicInstance.createOrGetBasicProject()
        def result = ProjectAPI.listRetrieval(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListRetrievalProjecNotExist() {
        def result = ProjectAPI.listRetrieval(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testAddProjectRetrievalWithoutFlag() {
        def projectToAdd = BasicInstance.getBasicProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = null
        json.retrievalAllOntology = null
        json.retrievalProjects = null

        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        assertEquals(true, project.retrievalAllOntology)
        assertEquals(false, project.retrievalDisable)
    }

    void testAddProjectRetrievalWithRetrievalDisable() {
        def projectToAdd = BasicInstance.getBasicProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = true
        json.retrievalAllOntology = false
        json.retrievalProjects = null

        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        assertEquals(false, project.retrievalAllOntology)
        assertEquals(true, project.retrievalDisable)
    }

    void testAddProjectRetrievalWithRetrievalAllOntology() {
        def projectToAdd = BasicInstance.getBasicProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = false
        json.retrievalAllOntology = true
        json.retrievalProjects = null

        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        assertEquals(true, project.retrievalAllOntology)
        assertEquals(false, project.retrievalDisable)
    }

    void testAddProjectRetrievalWithRetrievalSomeProject() {
        def projectToAdd = BasicInstance.getBasicProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = false
        json.retrievalAllOntology = false
        //json.retrievalProjects = new JSONArray([BasicInstance.createOrGetBasicProjectWithRight().id])
        json.retrievalProjects = new JSONArray("["+BasicInstance.createOrGetBasicProjectWithRight().id+"]")

        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        result = ProjectAPI.listRetrieval(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert ProjectAPI.containsInJSONList(BasicInstance.createOrGetBasicProjectWithRight().id,json)
    }

    void testAddProjectRetrievalWithoutConstistency() {

        def projectToAdd = BasicInstance.getBasicProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = true
        json.retrievalAllOntology = true
        json.retrievalProjects = null
        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)

        projectToAdd = BasicInstance.getBasicProjectNotExist()
        json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = true
        json.retrievalAllOntology = false
        json.retrievalProjects = new JSONArray("["+BasicInstance.createOrGetBasicProjectWithRight().id+"]")
        result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)

        projectToAdd = BasicInstance.getBasicProjectNotExist()
        json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = false
        json.retrievalAllOntology = true
        json.retrievalProjects = new JSONArray("["+BasicInstance.createOrGetBasicProjectWithRight().id+"]")
        result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)

    }

    void testAddProjectRetrievalAndDeleteProjectDependency() {
        def projectToAdd = BasicInstance.getBasicProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = false
        json.retrievalAllOntology = false
        def projectRetrieval = BasicInstance.getBasicProjectNotExist()
        assert projectRetrieval.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,projectRetrieval)
        json.retrievalProjects = new JSONArray("["+projectRetrieval.id+"]")

        //create project
        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        Project project = result.data

        //list retrieval project and check that project is there
        result = ProjectAPI.listRetrieval(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert ProjectAPI.containsInJSONList(projectRetrieval.id,json)

        //delete 1 retrieval project
        result = ProjectAPI.delete(projectRetrieval.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)

        //list retrieval project and check that project is not there
        result = ProjectAPI.listRetrieval(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        json = JSON.parse(result.data)
        assert json instanceof JSONArray
        assert !ProjectAPI.containsInJSONList(projectRetrieval.id,json)

    }

}
