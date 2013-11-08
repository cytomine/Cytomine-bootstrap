package be.cytomine

import be.cytomine.ontology.Ontology
import be.cytomine.processing.Software

import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.HttpClient
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.UserAnnotationAPI
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
class ProjectTests  {

    void testListProjectWithCredential() {
        def result = ProjectAPI.list(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListProjectWithoutCredential() {
        def result = ProjectAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }

    void testShowProjectWithCredential() {
        Project project = BasicInstanceBuilder.getProject()
        def result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListProjectByUser() {
        Project project = BasicInstanceBuilder.getProject()
        User user = BasicInstanceBuilder.getUser()
        def result = ProjectAPI.listByUser(user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListProjectByUserLight() {

        def projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        def result = ProjectAPI.create(projectToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def user1 = User.findByUsername(Infos.GOODLOGIN)
        def user2 = BasicInstanceBuilder.getUser2()

        assert ProjectAPI.containsInJSONList(project.id,ProjectAPI.listByUserLight(user1.id,'creator',Infos.GOODLOGIN, Infos.GOODPASSWORD).data)
        assert !ProjectAPI.containsInJSONList(project.id,ProjectAPI.listByUserLight(user2.id,'creator',Infos.GOODLOGIN, Infos.GOODPASSWORD).data)

        assert ProjectAPI.containsInJSONList(project.id,ProjectAPI.listByUserLight(user1.id,'admin',Infos.GOODLOGIN, Infos.GOODPASSWORD).data)
        assert !ProjectAPI.containsInJSONList(project.id,ProjectAPI.listByUserLight(user2.id,'admin',Infos.GOODLOGIN, Infos.GOODPASSWORD).data)

        assert ProjectAPI.containsInJSONList(project.id,ProjectAPI.listByUserLight(user1.id,'user',Infos.GOODLOGIN, Infos.GOODPASSWORD).data)
        assert !ProjectAPI.containsInJSONList(project.id,ProjectAPI.listByUserLight(user2.id,'user',Infos.GOODLOGIN, Infos.GOODPASSWORD).data)

    }






    void testListProjectByUserNotExist() {
        def result = ProjectAPI.listByUser(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testListProjectByOntology() {
        Ontology ontology = BasicInstanceBuilder.getOntology()
        def result = ProjectAPI.listByOntology(ontology.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListProjectByOntologyNotExist() {
        def result = ProjectAPI.listByOntology(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListProjectBySoftware() {
        Software software = BasicInstanceBuilder.getSoftware()
        User user = BasicInstanceBuilder.getUser()
        def result = ProjectAPI.listBySoftware(software.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListProjectBySoftwareNotExist() {
        def result = ProjectAPI.listBySoftware(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testAddProjectCorrect() {
        def projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        def result = ProjectAPI.create(projectToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        assert ProjectAPI.containsInJSONList(User.findByUsername(Infos.GOODLOGIN).id,JSON.parse(ProjectAPI.listUser(project.id,"admin",Infos.GOODLOGIN, Infos.GOODPASSWORD).data))
    }

    void testAddProjectWithUser() {
        def projectToAdd = BasicInstanceBuilder.getProjectNotExist()
        def user =  BasicInstanceBuilder.getUser()
        def json = JSON.parse(projectToAdd.encodeAsJSON())
        json.users = [user.id]
        json.admins = [user.id]
        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        Project project = result.data
        result = ProjectAPI.show(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        assert ProjectAPI.containsInJSONList(User.findByUsername(Infos.GOODLOGIN).id,JSON.parse(ProjectAPI.listUser(project.id,"admin",Infos.GOODLOGIN, Infos.GOODPASSWORD).data))
        assert ProjectAPI.containsInJSONList(user.id,JSON.parse(ProjectAPI.listUser(project.id,"admin",Infos.GOODLOGIN, Infos.GOODPASSWORD).data))
        assert ProjectAPI.containsInJSONList(user.id,JSON.parse(ProjectAPI.listUser(project.id,"user",Infos.GOODLOGIN, Infos.GOODPASSWORD).data))
    }



    void testAddProjectWithNameAlreadyExist() {
        def projectToAdd = BasicInstanceBuilder.getProject()
        String jsonProject = projectToAdd.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        def result = ProjectAPI.create(jsonUpdate.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }

    void testEditProjectCorrect() {

        def project = BasicInstanceBuilder.getProject()
        def data = UpdateData.createUpdateSet(project,[name: ["OLDNAME","NEWNAME"]])

        def result = ProjectAPI.update(project.id, data.postData,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idProject = json.project.id
        def showResult = ProjectAPI.show(idProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)
    }

    void testEditProjectCorrectWithUser() {
        def creator = User.findByUsername(Infos.GOODLOGIN)
        def user1 =  BasicInstanceBuilder.getUserNotExist(true)
        def user2 =  BasicInstanceBuilder.getUserNotExist(true)
        def user3 =  BasicInstanceBuilder.getUserNotExist(true)
        def user4 =  BasicInstanceBuilder.getUserNotExist(true)
        println "creator=${creator.id}"
        println "user1=${user1.id}"
        println "user2=${user2.id}"
        println "user3=${user3.id}"
        println "user4=${user4.id}"

        def project = BasicInstanceBuilder.getProjectNotExist(false)

        def json = JSON.parse(project.encodeAsJSON())
        json.users = [creator.id,user2.id,user4.id]
        json.admins = [creator.id,user1.id]

        def result = ProjectAPI.create(json.toString(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        project = result.data
        Infos.printRight(project)
        def usersList = JSON.parse(ProjectAPI.listUser(project.id,"user",Infos.GOODLOGIN, Infos.GOODPASSWORD).data)
        def adminsList = JSON.parse(ProjectAPI.listUser(project.id,"admin",Infos.GOODLOGIN, Infos.GOODPASSWORD).data)

        assert ProjectAPI.containsInJSONList(creator.id,usersList)
        assert ProjectAPI.containsInJSONList(creator.id,adminsList)
        assert ProjectAPI.containsInJSONList(user1.id,usersList)
        assert ProjectAPI.containsInJSONList(user1.id,adminsList)
        assert ProjectAPI.containsInJSONList(user2.id,usersList)
        assert ProjectAPI.containsInJSONList(user4.id,usersList)



        json = JSON.parse(project.encodeAsJSON())
        json.users = [creator.id,user3.id,user4.id,user1.id]
        json.admins = [creator.id,user3.id]

        result = ProjectAPI.update(project.id,json.toString(),Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        usersList = JSON.parse(ProjectAPI.listUser(project.id,"user",Infos.GOODLOGIN, Infos.GOODPASSWORD).data)
        adminsList = JSON.parse(ProjectAPI.listUser(project.id,"admin",Infos.GOODLOGIN, Infos.GOODPASSWORD).data)

        assert ProjectAPI.containsInJSONList(creator.id,usersList)
        assert ProjectAPI.containsInJSONList(creator.id,adminsList)
        Infos.printRight(project)
        assert ProjectAPI.containsInJSONList(user1.id,usersList)
        assert !ProjectAPI.containsInJSONList(user1.id,adminsList)
        assert !ProjectAPI.containsInJSONList(user2.id,usersList)
        assert ProjectAPI.containsInJSONList(user3.id,adminsList)
        assert ProjectAPI.containsInJSONList(user4.id,usersList)

    }

    void testEditProjectWithNameAlreadyExist() {
        Project projectWithOldName = BasicInstanceBuilder.getProject()
        Project projectWithNewName = BasicInstanceBuilder.getProjectNotExist()
        projectWithNewName.save(flush: true)

        Project projectToEdit = Project.get(projectWithNewName.id)
        def jsonProject = projectToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = projectWithOldName.name
        jsonProject = jsonUpdate.encodeAsJSON()
        def result = ProjectAPI.update(projectToEdit.id, jsonProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }

    void testEditProjectNotExist() {
        Project projectWithOldName = BasicInstanceBuilder.getProject()
        Project projectWithNewName = BasicInstanceBuilder.getProjectNotExist()
        projectWithNewName.save(flush: true)
        Project projectToEdit = Project.get(projectWithNewName.id)
        def jsonProject = projectToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonProject)
        jsonUpdate.name = projectWithOldName.name
        jsonUpdate.id = -99
        jsonProject = jsonUpdate.encodeAsJSON()
        def result = ProjectAPI.update(-99, jsonProject, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteProject() {
        def projectToDelete = BasicInstanceBuilder.getProjectNotExist()
        assert projectToDelete.save(flush: true) != null

        def result = ProjectAPI.delete(projectToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def showResult = ProjectAPI.show(projectToDelete.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == showResult.code
    }

    void testDeleteProjectNotExist() {
        def result = ProjectAPI.delete(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }


    void testProjectCounterUserAnnotationCounter() {
        //create project
        Project project = BasicInstanceBuilder.getProjectNotExist()
        BasicInstanceBuilder.checkDomain(project)
        BasicInstanceBuilder.saveDomain(project)
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.project = project
        BasicInstanceBuilder.checkDomain(image)
        BasicInstanceBuilder.saveDomain(image)

        //check if 0 algo annotation
        assert project.countAnnotations == 0
        assert image.countImageAnnotations == 0
        assert project.countJobAnnotations == 0
        assert image.countImageJobAnnotations == 0
        //add algo annotation
        UserAnnotation a1 = BasicInstanceBuilder.getUserAnnotationNotExist()
        a1.image = image
        a1.project = project
        BasicInstanceBuilder.checkDomain(a1)
        BasicInstanceBuilder.saveDomain(a1)

        project.refresh()
        image.refresh()

        //check if 1 algo annotation
        assert project.countAnnotations == 1
        assert image.countImageAnnotations == 1
        assert project.countJobAnnotations == 0
        assert image.countImageJobAnnotations == 0
        //add algo annotation
        UserAnnotation a2 = BasicInstanceBuilder.getUserAnnotationNotExist()
        a2.image = image
        a2.project = project
        BasicInstanceBuilder.checkDomain(a2)
        BasicInstanceBuilder.saveDomain(a2)

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
        Project project = BasicInstanceBuilder.getProjectNotExist()
        BasicInstanceBuilder.saveDomain(project)
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.project = project
        BasicInstanceBuilder.saveDomain(image)

        //check if 0 algo annotation
        project.refresh()
        image.refresh()
        assert project.countJobAnnotations == 0
        assert image.countImageJobAnnotations == 0
        assert project.countAnnotations == 0
        assert image.countImageAnnotations == 0

        //add algo annotation
        AlgoAnnotation a1 = BasicInstanceBuilder.getAlgoAnnotationNotExist()
        a1.image = image
        a1.project = project
        BasicInstanceBuilder.checkDomain(a1)
        BasicInstanceBuilder.saveDomain(a1)

        //check if 1 algo annotation
        project.refresh()
        image.refresh()
        assert project.countJobAnnotations == 1
        assert image.countImageJobAnnotations == 1
        assert project.countAnnotations == 0
        assert image.countImageAnnotations == 0

        //add algo annotation
        AlgoAnnotation a2 = BasicInstanceBuilder.getAlgoAnnotationNotExist()
        a2.image = image
        a2.project = project
        BasicInstanceBuilder.checkDomain(a2)
        BasicInstanceBuilder.saveDomain(a2)

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


    void testProjectCounterImageInstanceCounter() {
        //create project
        Project project = BasicInstanceBuilder.getProjectNotExist(true)

        assert project.countImageInstance() == 0

        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.project = project
        BasicInstanceBuilder.saveDomain(image)

        project.refresh()
        assert project.countImageInstance() == 1

        image.delete(flush: true)

        project.refresh()
        assert project.countImageInstance() == 0

    }

    void testLastOpened() {
        Project project1 = BasicInstanceBuilder.getProjectNotExist(true)
        Project project2 = BasicInstanceBuilder.getProjectNotExist(true)
        Project project3 = BasicInstanceBuilder.getProjectNotExist(true)

        def result = ProjectAPI.doPing(project1.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        result = ProjectAPI.doPing(project2.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ProjectAPI.listLastOpened(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

    }

    void testListCommandHistoryByProject() {
        def annotationToAdd = BasicInstanceBuilder.getUserAnnotation()
        annotationToAdd.project = BasicInstanceBuilder.getProjectNotExist(true)
        def result = UserAnnotationAPI.create(annotationToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ProjectAPI.listCommandHistory(annotationToAdd.project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = ProjectAPI.listCommandHistory(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

    }

}
