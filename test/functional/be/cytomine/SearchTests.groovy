package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Property
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.DomainAPI
import be.cytomine.test.http.SearchAPI
import be.cytomine.utils.Description
import be.cytomine.utils.SearchFilter
import be.cytomine.utils.SearchOperator
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray


class SearchTests {

    //Test LISTPROJECT
    void testListProject () {
        Project project1 = BasicInstanceBuilder.getProjectNotExist(true)
        Project project2 = BasicInstanceBuilder.getProjectNotExist(true)

        Property project1Property1 = BasicInstanceBuilder.getProjectPropertyNotExist()
        project1Property1.value = "Poney"
        project1Property1.domain = project1
        BasicInstanceBuilder.saveDomain(project1Property1)

        Property project1Property2 = BasicInstanceBuilder.getProjectPropertyNotExist()
        project1Property2.value = "Cheval"
        project1Property2.domain = project1
        BasicInstanceBuilder.saveDomain(project1Property2)

        Property project2Property2 = BasicInstanceBuilder.getProjectPropertyNotExist()
        project2Property2.value = "Cheval"
        project2Property2.domain = project2
        BasicInstanceBuilder.saveDomain(project2Property2)

        def result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.OR, SearchFilter.PROJECT, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(project1.id, json)
        assert DomainAPI.containsInJSONList(project2.id, json)

        result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.AND, SearchFilter.PROJECT, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(project1.id, json)
        assert !DomainAPI.containsInJSONList(project2.id, json)

    }

    //Test LISTANNOTATION
    void testListAnnotation () {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        UserAnnotation userAnnotation1 = BasicInstanceBuilder.getUserAnnotationNotExist(project, true)
        UserAnnotation userAnnotation2 = BasicInstanceBuilder.getUserAnnotationNotExist(project, true)

        Property annotation1Property1 = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        annotation1Property1.value = "Poney"
        annotation1Property1.domain = userAnnotation1
        BasicInstanceBuilder.saveDomain(annotation1Property1)

        Property annotation1Property2 = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        annotation1Property2.value = "Cheval"
        annotation1Property2.domain = userAnnotation1
        BasicInstanceBuilder.saveDomain(annotation1Property2)

        Property annotation2Property2 = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        annotation2Property2.value = "Cheval"
        annotation2Property2.domain = userAnnotation2
        BasicInstanceBuilder.saveDomain(annotation2Property2)

        def result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.OR, SearchFilter.ANNOTATION, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(userAnnotation1.id, json)
        assert DomainAPI.containsInJSONList(userAnnotation2.id, json)

        result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.AND, SearchFilter.ANNOTATION, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(userAnnotation1.id, json)
        assert !DomainAPI.containsInJSONList(userAnnotation2.id, json)

    }

    //Test LISTIMAGE
    void testListImage () {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        ImageInstance imageInstance1 = BasicInstanceBuilder.getImageInstanceNotExist(project, true)
        ImageInstance imageInstance2 = BasicInstanceBuilder.getImageInstanceNotExist(project, true)

        Property image1Property1 = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        image1Property1.value = "Poney"
        image1Property1.domain = imageInstance1
        BasicInstanceBuilder.saveDomain(image1Property1)

        Property image1Property2 = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        image1Property2.value = "Cheval"
        image1Property2.domain = imageInstance1
        BasicInstanceBuilder.saveDomain(image1Property2)

        Property image2Property2 = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        image2Property2.value = "Cheval"
        image2Property2.domain = imageInstance2
        BasicInstanceBuilder.saveDomain(image2Property2)

        def result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.OR, SearchFilter.IMAGE, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(imageInstance1.id, json)
        assert DomainAPI.containsInJSONList(imageInstance2.id, json)

        result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.AND, SearchFilter.IMAGE, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code

        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(imageInstance1.id, json)
        assert !DomainAPI.containsInJSONList(imageInstance2.id, json)
    }


    void testListWithDescription() {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        ImageInstance imageInstance1 = BasicInstanceBuilder.getImageInstanceNotExist(project, true)
        ImageInstance imageInstance2 = BasicInstanceBuilder.getImageInstanceNotExist(project, true)

        Property image1Property1 = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        image1Property1.value = "Poney"
        image1Property1.domain = imageInstance1
        BasicInstanceBuilder.saveDomain(image1Property1)

        Description image2Description1 =  BasicInstanceBuilder.getDescriptionNotExist(imageInstance2,true)
        image2Description1.data = "Blablbla Cheval Poney Blablabla"
        BasicInstanceBuilder.saveDomain(image2Description1)

        def result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.OR, SearchFilter.IMAGE, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(imageInstance1.id, json)
        assert DomainAPI.containsInJSONList(imageInstance2.id, json)

        result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.AND, SearchFilter.IMAGE, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert DomainAPI.containsInJSONList(imageInstance2.id, json)

    }
}
