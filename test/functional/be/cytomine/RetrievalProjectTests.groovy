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

    void testUpdateProjectRetrievalAddProjectInsteadOfAllOntology() {
        //project with AO=T
        Project projectToAdd = BasicInstance.createOrGetBasicProjectWithRight()
        projectToAdd.retrievalAllOntology = true;
        projectToAdd.retrievalDisable = false;
        projectToAdd.save(flush: true)

        //add project json
        def jsonProject = JSON.parse(projectToAdd.encodeAsJSON())
        jsonProject.retrievalDisable = false
        jsonProject.retrievalAllOntology = false
        def projectRetrieval = BasicInstance.getBasicProjectNotExist()
        projectRetrieval.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,projectRetrieval)
        jsonProject.retrievalProjects = new JSONArray("["+projectRetrieval.id+"]")

        def result = ProjectAPI.update(projectToAdd.id,jsonProject.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idProject = json.project.id

        Project checkProject = Project.read(idProject)
        checkProject.refresh()
        assert !checkProject.retrievalAllOntology
        assert !checkProject.retrievalDisable
        assert checkProject.retrievalProjects.contains(projectRetrieval)
    }

    void testUpdateProjectRetrievalAddProject() {
        //project with AO=T
        Project projectToAdd = BasicInstance.createOrGetBasicProjectWithRight()
        projectToAdd.retrievalAllOntology = false;
        projectToAdd.retrievalDisable = false;
        projectToAdd.retrievalProjects.clear()
        projectToAdd.save(flush: true)
        def projectRetrieval1 = BasicInstance.getBasicProjectNotExist()
        projectRetrieval1.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,projectRetrieval1)
        projectToAdd.retrievalProjects.add(projectRetrieval1)
        projectToAdd.save(flush: true)

        //add project json
        def jsonProject = JSON.parse(projectToAdd.encodeAsJSON())
        jsonProject.retrievalDisable = false
        jsonProject.retrievalAllOntology = false
        def projectRetrieval2 = BasicInstance.getBasicProjectNotExist()
        projectRetrieval2.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,projectRetrieval2)
        jsonProject.retrievalProjects = new JSONArray("["+projectRetrieval1.id+"," + projectRetrieval2.id +"]")

        def result = ProjectAPI.update(projectToAdd.id,jsonProject.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idProject = json.project.id

        Project checkProject = Project.read(idProject)
        checkProject.refresh()
        log.info checkProject.retrievalProjects
        assert !checkProject.retrievalAllOntology
        assert !checkProject.retrievalDisable
        assert checkProject.retrievalProjects.contains(projectRetrieval1)
        assert checkProject.retrievalProjects.contains(projectRetrieval2)
    }

    void testUpdateProjectRetrievalRemoveProject() {
        //project with AO=T
        Project projectToAdd = BasicInstance.createOrGetBasicProjectWithRight()
        projectToAdd.retrievalAllOntology = false;
        projectToAdd.retrievalDisable = false;
        projectToAdd.retrievalProjects.clear()
        projectToAdd.save(flush: true)
        def projectRetrieval1 = BasicInstance.getBasicProjectNotExist()
        projectRetrieval1.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,projectRetrieval1)
        def projectRetrieval2 = BasicInstance.getBasicProjectNotExist()
        projectRetrieval2.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,projectRetrieval2)

        projectToAdd.retrievalProjects.add(projectRetrieval1)
        projectToAdd.retrievalProjects.add(projectRetrieval2)
        projectToAdd.save(flush: true)

        //add project json
        def jsonProject = JSON.parse(projectToAdd.encodeAsJSON())
        jsonProject.retrievalDisable = false
        jsonProject.retrievalAllOntology = false
        jsonProject.retrievalProjects = new JSONArray("["+projectRetrieval1.id+"]")

        def result = ProjectAPI.update(projectToAdd.id,jsonProject.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idProject = json.project.id

        Project checkProject = Project.read(idProject)
        checkProject.refresh()
        log.info checkProject.retrievalProjects
        assert !checkProject.retrievalAllOntology
        assert !checkProject.retrievalDisable
        assert checkProject.retrievalProjects.contains(projectRetrieval1)
        assert !checkProject.retrievalProjects.contains(projectRetrieval2)
    }

    void testUpdateProjectRetrievalRemoveProjectAndDisableRetrieval() {
        //project with AO=T
        Project projectToAdd = BasicInstance.createOrGetBasicProjectWithRight()
        projectToAdd.retrievalAllOntology = false;
        projectToAdd.retrievalDisable = false;
        projectToAdd.retrievalProjects.clear()
        projectToAdd.save(flush: true)
        def projectRetrieval1 = BasicInstance.getBasicProjectNotExist()
        projectRetrieval1.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,projectRetrieval1)
        def projectRetrieval2 = BasicInstance.getBasicProjectNotExist()
        projectRetrieval2.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN,projectRetrieval2)

        projectToAdd.retrievalProjects.add(projectRetrieval1)
        projectToAdd.retrievalProjects.add(projectRetrieval2)
        projectToAdd.save(flush: true)

        //add project json
        def jsonProject = JSON.parse(projectToAdd.encodeAsJSON())
        jsonProject.retrievalDisable = true
        jsonProject.retrievalAllOntology = false
        jsonProject.retrievalProjects = new JSONArray("[]")

        def result = ProjectAPI.update(projectToAdd.id,jsonProject.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idProject = json.project.id

        Project checkProject = Project.read(idProject)
        checkProject.refresh()
        assert !checkProject.retrievalAllOntology
        assert checkProject.retrievalDisable
        assert !checkProject.retrievalProjects.contains(projectRetrieval1)
        assert !checkProject.retrievalProjects.contains(projectRetrieval2)
    }
}
