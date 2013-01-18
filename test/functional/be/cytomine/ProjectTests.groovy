package be.cytomine

import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.utils.BasicInstance
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.utils.UpdateData

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
        def result = ProjectAPI.create(projectToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
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
        def data = UpdateData.createUpdateSet(projectToAdd)
        def result = ProjectAPI.update(data.oldData.id, data.newData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idProject = json.project.id
        def showResult = ProjectAPI.show(idProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstance.compareProject(data.mapNew, json)
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

        def result = ProjectAPI.delete(projectToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(200, result.code)
        def showResult = ProjectAPI.show(projectToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, showResult.code)
    }

    void testDeleteProjectNotExist() {
        def result = ProjectAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assertEquals(404, result.code)
    }


    void testProjectCounterUserAnnotationCounter() {
        //create project
        Project project = BasicInstance.getBasicProjectNotExist()
        BasicInstance.checkDomain(project)
        BasicInstance.saveDomain(project)
        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.project = project
        BasicInstance.checkDomain(image)
        BasicInstance.saveDomain(image)

        //check if 0 algo annotation
        assert project.countAnnotations == 0
        assert image.countImageAnnotations == 0
        assert project.countJobAnnotations == 0
        assert image.countImageJobAnnotations == 0
        //add algo annotation
        UserAnnotation a1 = BasicInstance.getBasicUserAnnotationNotExist()
        a1.image = image
        a1.project = project
        BasicInstance.checkDomain(a1)
        BasicInstance.saveDomain(a1)

        project.refresh()
        image.refresh()

        //check if 1 algo annotation
        assert project.countAnnotations == 1
        assert image.countImageAnnotations == 1
        assert project.countJobAnnotations == 0
        assert image.countImageJobAnnotations == 0
        //add algo annotation
        UserAnnotation a2 = BasicInstance.getBasicUserAnnotationNotExist()
        a2.image = image
        a2.project = project
        BasicInstance.checkDomain(a2)
        BasicInstance.saveDomain(a2)

        project.refresh()
        image.refresh()

        //check if 2 algo annotation
        assert project.countAnnotations == 2
        assert image.countImageAnnotations == 2
        assert project.countJobAnnotations == 0
        assert image.countImageJobAnnotations == 0
        //remove algo annotation
        a1.delete(flush: true)


        project.refresh()
        image.refresh()

        //check if 1 algo annotation
        assert project.countAnnotations == 1
        assert image.countImageAnnotations == 1
        assert project.countJobAnnotations == 0
        assert image.countImageJobAnnotations == 0
        //remove algo annotation
        a2.delete(flush: true)

        project.refresh()
        image.refresh()

        //check if 1 algo annotation
        assert project.countAnnotations == 0
        assert image.countImageAnnotations == 0
        assert project.countJobAnnotations == 0
        assert image.countImageJobAnnotations == 0
    }



    void testProjectCounterAlgoAnnotationCounter() {
        //create project
        Project project = BasicInstance.getBasicProjectNotExist()
        BasicInstance.saveDomain(project)
        ImageInstance image = BasicInstance.getBasicImageInstanceNotExist()
        image.project = project
        BasicInstance.saveDomain(image)

        //check if 0 algo annotation
        project.refresh()
        image.refresh()
        assert project.countJobAnnotations == 0
        assert image.countImageJobAnnotations == 0
        assert project.countAnnotations == 0
        assert image.countImageAnnotations == 0

        //add algo annotation
        AlgoAnnotation a1 = BasicInstance.getBasicAlgoAnnotationNotExist()
        a1.image = image
        a1.project = project
        BasicInstance.checkDomain(a1)
        BasicInstance.saveDomain(a1)

        //check if 1 algo annotation
        project.refresh()
        image.refresh()
        assert project.countJobAnnotations == 1
        assert image.countImageJobAnnotations == 1
        assert project.countAnnotations == 0
        assert image.countImageAnnotations == 0

        //add algo annotation
        AlgoAnnotation a2 = BasicInstance.getBasicAlgoAnnotationNotExist()
        a2.image = image
        a2.project = project
        BasicInstance.checkDomain(a2)
        BasicInstance.saveDomain(a2)

        //check if 2 algo annotation
        project.refresh()
        image.refresh()
        assert project.countJobAnnotations == 2
        assert image.countImageJobAnnotations == 2
        assert project.countAnnotations == 0
        assert image.countImageAnnotations == 0

        //remove algo annotation
        a1.delete(flush: true)

        //check if 1 algo annotation
        project.refresh()
        image.refresh()
        assert project.countJobAnnotations == 1
        assert image.countImageJobAnnotations == 1
        assert project.countAnnotations == 0
        assert image.countImageAnnotations == 0

        //remove algo annotation
        a2.delete(flush: true)

        //check if 1 algo annotation
        project.refresh()
        image.refresh()
        assert project.countJobAnnotations == 0
        assert image.countImageJobAnnotations == 0
        assert project.countAnnotations == 0
        assert image.countImageAnnotations == 0
    }


}
