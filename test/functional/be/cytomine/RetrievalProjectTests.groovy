package be.cytomine

import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
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
class RetrievalProjectTests  {

    void testListRetrievalProjectWithCredential() {
        Project project = BasicInstanceBuilder.getProject()
        def result = ProjectAPI.listRetrieval(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListRetrievalProjecNotExist() {
        def result = ProjectAPI.listRetrieval(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testAddProjectRetrievalWithoutFlag() {
        def projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = null
        json.retrievalAllOntology = null
        json.retrievalProjects = null

        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        assert true==project.retrievalAllOntology
        assert false==project.retrievalDisable
    }

    void testAddProjectRetrievalWithRetrievalDisable() {
        def projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = true
        json.retrievalAllOntology = false
        json.retrievalProjects = null

        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        assert false==project.retrievalAllOntology
        assert true==project.retrievalDisable
    }

    void testAddProjectRetrievalWithRetrievalAllOntology() {
        def projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = false
        json.retrievalAllOntology = true
        json.retrievalProjects = null

        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        assert true==project.retrievalAllOntology
        assert false==project.retrievalDisable
    }

    void testAddProjectRetrievalWithRetrievalSomeProject() {
        def projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = false
        json.retrievalAllOntology = false
        //json.retrievalProjects = new JSONArray([BasicInstanceBuilder.getProject().id])
        json.retrievalProjects = new JSONArray("["+BasicInstanceBuilder.getProject().id+"]")

        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ProjectAPI.listRetrieval(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert ProjectAPI.containsInJSONList(BasicInstanceBuilder.getProject().id,json)
    }

    void testAddProjectRetrievalWithoutConstistency() {

        def projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = true
        json.retrievalAllOntology = true
        json.retrievalProjects = null
        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code

        projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = true
        json.retrievalAllOntology = false
        json.retrievalProjects = new JSONArray("["+BasicInstanceBuilder.getProject().id+"]")
        result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code

        projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = false
        json.retrievalAllOntology = true
        json.retrievalProjects = new JSONArray("["+BasicInstanceBuilder.getProject().id+"]")
        result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 400 == result.code

    }

    void testAddProjectRetrievalAndDeleteProjectDependency() {
        def projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.retrievalDisable = false
        json.retrievalAllOntology = false
        def projectRetrieval = BasicInstanceBuilder.getProjectNotExist()
        assert projectRetrieval.save(flush: true)

        json.retrievalProjects = new JSONArray("["+projectRetrieval.id+"]")

        //create project
        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        Project project = result.data

        //list retrieval project and check that project is there
        result = ProjectAPI.listRetrieval(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert ProjectAPI.containsInJSONList(projectRetrieval.id,json)

        //delete 1 retrieval project
        result = ProjectAPI.delete(projectRetrieval.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        //list retrieval project and check that project is not there
        result = ProjectAPI.listRetrieval(project.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert !ProjectAPI.containsInJSONList(projectRetrieval.id,json)

    }

    void testUpdateProjectRetrievalAddProjectInsteadOfAllOntology() {
        //project with AO=T
        Project projectToAdd = BasicInstanceBuilder.getProject()
        projectToAdd.retrievalAllOntology = true;
        projectToAdd.retrievalDisable = false;
        projectToAdd.save(flush: true)

        //add project json
        def jsonProject = JSON.parse(projectToAdd.encodeAsJSON())
        jsonProject.retrievalDisable = false
        jsonProject.retrievalAllOntology = false
        def projectRetrieval = BasicInstanceBuilder.getProjectNotExist()
        projectRetrieval.save(flush: true)

        jsonProject.retrievalProjects = new JSONArray("["+projectRetrieval.id+"]")

        def result = ProjectAPI.update(projectToAdd.id,jsonProject.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
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
        Project projectToAdd = BasicInstanceBuilder.getProject()
        projectToAdd.retrievalAllOntology = false;
        projectToAdd.retrievalDisable = false;
        projectToAdd.retrievalProjects?.clear()
        projectToAdd.save(flush: true)
        def projectRetrieval1 = BasicInstanceBuilder.getProjectNotExist()
        projectRetrieval1.save(flush: true)

        projectToAdd.addToRetrievalProjects(projectRetrieval1)
        projectToAdd.save(flush: true)

        //add project json
        def jsonProject = JSON.parse(projectToAdd.encodeAsJSON())
        jsonProject.retrievalDisable = false
        jsonProject.retrievalAllOntology = false
        def projectRetrieval2 = BasicInstanceBuilder.getProjectNotExist()
        projectRetrieval2.save(flush: true)

        jsonProject.retrievalProjects = new JSONArray("["+projectRetrieval1.id+"," + projectRetrieval2.id +"]")

        def result = ProjectAPI.update(projectToAdd.id,jsonProject.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
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
        Project projectToAdd = BasicInstanceBuilder.getProject()
        projectToAdd.retrievalAllOntology = false;
        projectToAdd.retrievalDisable = false;
        projectToAdd.retrievalProjects?.clear()
        projectToAdd.save(flush: true)
        def projectRetrieval1 = BasicInstanceBuilder.getProjectNotExist()
        projectRetrieval1.save(flush: true)

        def projectRetrieval2 = BasicInstanceBuilder.getProjectNotExist()
        projectRetrieval2.save(flush: true)

        projectToAdd.addToRetrievalProjects(projectRetrieval1)
        projectToAdd.addToRetrievalProjects(projectRetrieval2)
        projectToAdd.save(flush: true)

        //add project json
        def jsonProject = JSON.parse(projectToAdd.encodeAsJSON())
        jsonProject.retrievalDisable = false
        jsonProject.retrievalAllOntology = false
        jsonProject.retrievalProjects = new JSONArray("["+projectRetrieval1.id+"]")

        def result = ProjectAPI.update(projectToAdd.id,jsonProject.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
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
        Project projectToAdd = BasicInstanceBuilder.getProject()
        projectToAdd.retrievalAllOntology = false;
        projectToAdd.retrievalDisable = false;
        projectToAdd.retrievalProjects?.clear()
        projectToAdd.save(flush: true)
        def projectRetrieval1 = BasicInstanceBuilder.getProjectNotExist()
        projectRetrieval1.save(flush: true)

        def projectRetrieval2 = BasicInstanceBuilder.getProjectNotExist()
        projectRetrieval2.save(flush: true)

        projectToAdd.addToRetrievalProjects(projectRetrieval1)
        projectToAdd.addToRetrievalProjects(projectRetrieval2)
        projectToAdd.save(flush: true)

        //add project json
        def jsonProject = JSON.parse(projectToAdd.encodeAsJSON())
        jsonProject.retrievalDisable = true
        jsonProject.retrievalAllOntology = false
        jsonProject.retrievalProjects = new JSONArray("[]")

        def result = ProjectAPI.update(projectToAdd.id,jsonProject.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
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

    void testDeleteProjectRetrievalWithRetrievalProject() {
        def projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        assert projectToAdd.save(flush: true)

        def projectRetrieval = BasicInstanceBuilder.getProjectNotExist()
        assert projectRetrieval.save(flush: true)

        projectToAdd.refresh()
        projectToAdd.addToRetrievalProjects(projectRetrieval)
        assert projectToAdd.save(flush: true)

        //delete 1 retrieval project
        def result = ProjectAPI.delete(projectToAdd.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }
}
