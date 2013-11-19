package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Property
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.PropertyAPI
import be.cytomine.utils.Keyword
import be.cytomine.utils.UpdateData
import com.vividsolutions.jts.io.WKTReader
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class PropertyTests {

    //TEST SHOW
    void testShowAnnotationProperty() {
        def annotationProperty = BasicInstanceBuilder.getAnnotationProperty()
        def result = PropertyAPI.show(annotationProperty.id, annotationProperty.domainIdent, "annotation", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.id == annotationProperty.id
    }
    void testShowProjectProperty() {
        def projectProperty = BasicInstanceBuilder.getProjectProperty()
        def result = PropertyAPI.show(projectProperty.id, projectProperty.domainIdent, "project", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.id == projectProperty.id
    }
    void testShowImageInstanceProperty() {
        def imageInstanceProperty = BasicInstanceBuilder.getImageInstanceProperty()
        def result = PropertyAPI.show(imageInstanceProperty.id, imageInstanceProperty.domainIdent, "imageinstance", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.id == imageInstanceProperty.id
    }

    void testShowAnnotationPropertyNotExist() {
        def annotation = BasicInstanceBuilder.getUserAnnotation()
        def result = PropertyAPI.show(-99, annotation.id, "annotation", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
    void testShowProjectPropertyNotExist() {
        def project = BasicInstanceBuilder.getProject()
        def result = PropertyAPI.show(-99, project.id, "project", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
    void testShowPropertyPropertyNotExist() {
        def imageInstance = BasicInstanceBuilder.getImageInstance()
        def result = PropertyAPI.show(-99, imageInstance.id, "imageinstance", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    //TEST LISTBYDOMAIN
    void testListByAnnotation() {
        def result = PropertyAPI.listByDomain(BasicInstanceBuilder.getUserAnnotation().id, "annotation", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }
    void testListByProject() {
        def result = PropertyAPI.listByDomain(BasicInstanceBuilder.getProject().id, "project", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }
    void testListByImageInstance() {
        def result = PropertyAPI.listByDomain(BasicInstanceBuilder.getImageInstance().id, "imageinstance", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testListByAnnotationNotExist() {
        def result = PropertyAPI.listByDomain(-99, "annotation", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
    void testListByProjectNotExist() {
        def result = PropertyAPI.listByDomain(-99, "project", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
    void testListByImageInstanceNotExist() {
        def result = PropertyAPI.listByDomain(-99, "imageinstance", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    //TEST LISTKEY FOR ANNOTATION
    void testListKeyWithProject () {
        Project project = BasicInstanceBuilder.getProject()
        UserAnnotation userAnnotation = BasicInstanceBuilder.getUserAnnotationNotExist(project,BasicInstanceBuilder.getImageInstance(),true)

        Property annotationProperty = BasicInstanceBuilder.getAnnotationPropertyNotExist(userAnnotation,true)

        def result = PropertyAPI.listKeyWithProject(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert PropertyAPI.containsStringInJSONList(annotationProperty.key,json);
        println("JSON - project: " + json)
    }



    void testListKeyWithImage () {
        def result = PropertyAPI.listKeyWithImage((BasicInstanceBuilder.getImageInstance()).id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        println("JSON - image: " + json)
    }

  //TEST LISTKEY FOR IMAGEINSTANCE
    void testListKeyForImageInstanceWithProject () {
        Project project = BasicInstanceBuilder.getProject()
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)

        Property annotationProperty = BasicInstanceBuilder.getImageInstancePropertyNotExist(image,true)

        def result = PropertyAPI.listKeyForImageInstanceWithProject(project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert PropertyAPI.containsStringInJSONList(annotationProperty.key,json);
        println("JSON - project: " + json)
    }


    void testListKeyWithProjectNotExist () {
        def result = PropertyAPI.listKeyWithProject(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
    void testListKeyWithImageNotExist () {
        def result = PropertyAPI.listKeyWithImage(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    //TEST DELETE
    void testDeleteAnnotationProperty() {
        def annotationPropertyToDelete = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        assert annotationPropertyToDelete.save(flush: true) != null

        def id = annotationPropertyToDelete.id
        def idDomain = annotationPropertyToDelete.domainIdent
        def key = annotationPropertyToDelete.key
        def result = PropertyAPI.delete(id, idDomain, "annotation" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        //UNDO & REDO
        result = PropertyAPI.show(id, idDomain, "annotation" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = PropertyAPI.undo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "annotation" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = PropertyAPI.redo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "annotation" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
    void testDeleteProjectProperty() {
        def projectPropertyToDelete = BasicInstanceBuilder.getProjectPropertyNotExist()
        assert projectPropertyToDelete.save(flush: true) != null

        def id = projectPropertyToDelete.id
        def idDomain = projectPropertyToDelete.domainIdent
        def key = projectPropertyToDelete.key
        def result = PropertyAPI.delete(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        //UNDO & REDO
        result = PropertyAPI.show(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = PropertyAPI.undo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = PropertyAPI.redo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
    void testDeleteImageInstanceProperty() {
        def imageInstancePropertyToDelete = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        assert imageInstancePropertyToDelete.save(flush: true) != null

        def id = imageInstancePropertyToDelete.id
        def idDomain = imageInstancePropertyToDelete.domainIdent
        def key = imageInstancePropertyToDelete.key
        def result = PropertyAPI.delete(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        //UNDO & REDO
        result = PropertyAPI.show(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = PropertyAPI.undo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = PropertyAPI.redo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testDeleteAnnotationPropertyNotExist() {
        def annotation = BasicInstanceBuilder.getUserAnnotationNotExist()
        def result = PropertyAPI.delete(-99, annotation.id, "annotation", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
    void testDeleteProjectPropertyNotExist() {
        def project = BasicInstanceBuilder.getProjectNotExist()
        def result = PropertyAPI.delete(-99, project.id, "project", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }
    void testDeleteImageInstancePropertyNotExist() {
        def imageInstance = BasicInstanceBuilder.getImageInstanceNotExist()
        def result = PropertyAPI.delete(-99, imageInstance.id, "imageinstance", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    //TEST ADD
    void testAddAnnotationPropertyCorrect() {
        def annotationPropertyToAdd = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        def idDomain = annotationPropertyToAdd.domainIdent

        def result = PropertyAPI.create(idDomain, "annotation" ,annotationPropertyToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def id =  result.data.id

        //UNDO & REDO
        result = PropertyAPI.show(id, idDomain, "annotation" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = PropertyAPI.undo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "annotation" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = PropertyAPI.redo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "annotation" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }
    void testAddProjectPropertyCorrect() {
        def projectPropertyToAdd = BasicInstanceBuilder.getProjectPropertyNotExist()
        def idDomain = projectPropertyToAdd.domainIdent

        def result = PropertyAPI.create(idDomain, "project" ,projectPropertyToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def id =  result.data.id

        //UNDO & REDO
        result = PropertyAPI.show(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = PropertyAPI.undo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = PropertyAPI.redo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "project" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }
    void testAddImageInstancePropertyCorrect() {
        def imageInstancePropertyToAdd = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        def idDomain = imageInstancePropertyToAdd.domainIdent

        def result = PropertyAPI.create(idDomain, "imageinstance" ,imageInstancePropertyToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def id =  result.data.id

        //UNDO & REDO
        result = PropertyAPI.show(id, idDomain, "imageinstance" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        result = PropertyAPI.undo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "imageinstance" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        result = PropertyAPI.redo()
        assert 200 == result.code

        result = PropertyAPI.show(id, idDomain, "imageinstance" ,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
    }

    void testAddAnnotationPropertyAlreadyExist() {
        def annotationPropertyToAdd = BasicInstanceBuilder.getAnnotationProperty()
        def result = PropertyAPI.create(annotationPropertyToAdd.domainIdent, "annotation", annotationPropertyToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }
    void testAddProjectPropertyAlreadyExist() {
        def projectPropertyToAdd = BasicInstanceBuilder.getProjectProperty()
        def result = PropertyAPI.create(projectPropertyToAdd.domainIdent, "project", projectPropertyToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }
    void testAddImageInstancePropertyAlreadyExist() {
        def imageInstancePropertyToAdd = BasicInstanceBuilder.getImageInstanceProperty()
        def result = PropertyAPI.create(imageInstancePropertyToAdd.domainIdent, "imageinstance", imageInstancePropertyToAdd.encodeAsJSON(), Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 409 == result.code
    }

    //TEST UPDATE
    void testUpdateAnnotationPropertyCorrect() {
        Property annotationPropertyToAdd = BasicInstanceBuilder.getAnnotationProperty()
        def data = UpdateData.createUpdateSet(annotationPropertyToAdd,[key: ["OLDKEY","NEWKEY"],value: ["OLDVALUE","NEWVALUE"]])

        println annotationPropertyToAdd.domainIdent + "-" + annotationPropertyToAdd.key

        def result = PropertyAPI.update(annotationPropertyToAdd.id, annotationPropertyToAdd.domainIdent, "annotation", data.postData, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        BasicInstanceBuilder.compare(data.mapNew,json.property)
    }
    void testUpdateProjectPropertyCorrect() {
        Property projectPropertyToAdd = BasicInstanceBuilder.getProjectProperty()
        def data = UpdateData.createUpdateSet( projectPropertyToAdd,[key: ["OLDKEY","NEWKEY"],value: ["OLDVALUE","NEWVALUE"]])

        println  projectPropertyToAdd.domainIdent + "-" +  projectPropertyToAdd.key

        def result = PropertyAPI.update(projectPropertyToAdd.id, projectPropertyToAdd.domainIdent, "project", data.postData, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        BasicInstanceBuilder.compare(data.mapNew,json.property)
    }
    void testUpdateImageInstancePropertyCorrect() {
        Property imageInstancePropertyToAdd = BasicInstanceBuilder.getImageInstanceProperty()
        def data = UpdateData.createUpdateSet(imageInstancePropertyToAdd,[key: ["OLDKEY","NEWKEY"],value: ["OLDVALUE","NEWVALUE"]])

        println imageInstancePropertyToAdd.domainIdent + "-" + imageInstancePropertyToAdd.key

        def result = PropertyAPI.update(imageInstancePropertyToAdd.id, imageInstancePropertyToAdd.domainIdent, "imageinstance", data.postData, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        BasicInstanceBuilder.compare(data.mapNew,json.property)
    }

    //TEST CENTER ANNOTATION
    void testSelectCenterAnnotationCorrect() {
        Property annotationProperty = BasicInstanceBuilder.getAnnotationProperty()
        def user = BasicInstanceBuilder.getUser()
        def image = BasicInstanceBuilder.getImageInstance()

        def annotation = BasicInstanceBuilder.getUserAnnotationNotExist()
        annotation.location = new WKTReader().read("POLYGON ((0 0, 0 1000, 1000 1000, 1000 0, 0 0))")
        annotation.user = user
        annotation.image = image
        annotationProperty.domain = annotation;
        annotationProperty.key = "TestCytomine"
        annotationProperty.value = "ValueTestCytomine"
        assert annotationProperty.save(flush: true) != null
        assert annotation.save(flush: true)  != null

        def result = PropertyAPI.listAnnotationCenterPosition(user.id, image.id, "0,0,1000,1000","TestCytomine", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        println result
    }

    void testSelectCenterAnnotationNotCorrect() {
        Property annotationProperty = BasicInstanceBuilder.getAnnotationProperty()
        def user = BasicInstanceBuilder.getUser()
        def image = BasicInstanceBuilder.getImageInstance()

        def annotation = BasicInstanceBuilder.getUserAnnotationNotExist()
        annotation.location = new WKTReader().read("POLYGON ((0 0, 0 1000, 1000 1000, 1000 0, 0 0))")
        annotation.user = user
        annotation.image = image
        annotationProperty.domain = annotation;
        annotationProperty.key = "TestCytomine"
        annotationProperty.value = "ValueTestCytomine"
        assert annotationProperty.save(flush: true) != null
        assert annotation.save(flush: true)  != null

        //Error IdUser
        def result = PropertyAPI.listAnnotationCenterPosition(-99, image.id, "0,0,1000,1000","TestCytomine", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code

        //Error IdImage
        result = PropertyAPI.listAnnotationCenterPosition(user.id, -99, "0,0,1000,1000","TestCytomine", Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListKeywords() {
        def result = PropertyAPI.listKeywords(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.collection.size()==0

        Keyword key = new Keyword(key: "test")
        assert key.save(flush:true)

        result = PropertyAPI.listKeywords(Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.collection.size()==1
    }
}
