package be.cytomine

import be.cytomine.test.BasicInstance
import be.cytomine.test.Infos

import org.codehaus.groovy.grails.web.json.JSONObject
import grails.converters.JSON
import be.cytomine.project.Project
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.security.User
import be.cytomine.test.http.ProjectAPI
import be.cytomine.ontology.Ontology
import be.cytomine.project.Discipline
import be.cytomine.processing.Software
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 17/02/11
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
class ProjectTests extends functionaltestplugin.FunctionalTestCase {

    void testListProjectWithCredential() {
        def result = ProjectAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListProjectWithoutCredential() {
        def result = ProjectAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        assertEquals(401, result.code)
    }

    void testShowProjectWithCredential() {
        Project project = BasicInstance.createOrGetBasicProjectWithRight()
        def result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListProjectByUser() {
        Project project = BasicInstance.createOrGetBasicProject()
        User user = BasicInstance.createOrGetBasicUser()
        def result = ProjectAPI.listByUser(user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListProjectByUserNotExist() {
        def result = ProjectAPI.listByUser(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }


    void testListProjectByOntology() {
        Ontology ontology = BasicInstance.createOrGetBasicOntology()
        def result = ProjectAPI.listByOntology(ontology.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListProjectByOntologyNotExist() {
        def result = ProjectAPI.listByOntology(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }



    void testListProjectByDiscipline() {
        Discipline discipline = BasicInstance.createOrGetBasicDiscipline()
        User user = BasicInstance.createOrGetBasicUser()
        def result = ProjectAPI.listByDiscipline(discipline.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListProjectByDisciplineNotExist() {
        def result = ProjectAPI.listByDiscipline(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testListProjectBySoftware() {
        Software software = BasicInstance.createOrGetBasicSoftware()
        User user = BasicInstance.createOrGetBasicUser()
        def result = ProjectAPI.listBySoftware(software.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONArray
    }

    void testListProjectBySoftwareNotExist() {
        def result = ProjectAPI.listBySoftware(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testAddProjectCorrect() {
        def projectToAdd = BasicInstance.getBasicProjectNotExist()
        def result = ProjectAPI.create(projectToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
    }

    void testAddProjectWithNameAlreadyExist() {
        def projectToAdd = BasicInstance.createOrGetBasicProject()
        String jsonProject = projectToAdd.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        def result = ProjectAPI.create(jsonUpdate.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(409, result.code)
    }

    void testEditProjectCorrect() {
        Project projectToAdd = BasicInstance.createOrGetBasicProjectWithRight()
        def result = ProjectAPI.update(projectToAdd, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idProject = json.project.id
        def showResult = ProjectAPI.show(idProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareProject(result.mapNew, json)
    }

    void testEditProjectWithBadName() {
        Project projectToAdd = BasicInstance.createOrGetBasicProjectWithRight()
        Project projectToEdit = Project.get(projectToAdd.id)
        def jsonProject = projectToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = null
        jsonProject = jsonUpdate.encodeAsJSON()
        def result = ProjectAPI.update(projectToAdd.id, jsonProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(400, result.code)

    }

    void testEditProjectWithNameAlreadyExist() {
        Project projectWithOldName = BasicInstance.createOrGetBasicProjectWithRight()
        Project projectWithNewName = BasicInstance.getBasicProjectNotExist()
        projectWithNewName.save(flush: true)
        Infos.addUserRight(Infos.GOODLOGIN, projectWithNewName)
        Project projectToEdit = Project.get(projectWithNewName.id)
        def jsonProject = projectToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = projectWithOldName.name
        jsonProject = jsonUpdate.encodeAsJSON()
        def result = ProjectAPI.update(projectToEdit.id, jsonProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(409, result.code)
    }

    void testEditProjectNotExist() {
        Project projectWithOldName = BasicInstance.createOrGetBasicProject()
        Project projectWithNewName = BasicInstance.getBasicProjectNotExist()
        projectWithNewName.save(flush: true)
        Project projectToEdit = Project.get(projectWithNewName.id)
        def jsonProject = projectToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = projectWithOldName.name
        jsonUpdate.id = -99
        jsonProject = jsonUpdate.encodeAsJSON()
        def result = ProjectAPI.update(-99, jsonProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }

    void testDeleteProject() {
        def projectToDelete = BasicInstance.getBasicProjectNotExist()
        assert projectToDelete.save(flush: true) != null
        Infos.addUserRight(Infos.GOODLOGIN, projectToDelete)
        def result = ProjectAPI.delete(projectToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def showResult = ProjectAPI.show(projectToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, showResult.code)
    }

    void testDeleteProjectNotExist() {
        def result = ProjectAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }
}
