package be.cytomine

import be.cytomine.image.AbstractImage
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


class SearchEngineTests {

  void testProjectSearchComplex() {
      //word 1 = hello
      //word 2 = world
      //word 3 = cytomine

      //projectA: create a project with name "hello" with description with data "blabla cytomine world"
      Project projectA = createProject("hello")
      Description descriptionA = createDescriptionForDomain(projectA,"blabla cytomine world")

      //projectB: create a project with name "cytomine" with property value "hello world"
      Project projectB = createProject("cytomine")
      Property propertyB = createPropertyForDomain(projectB,"hello world hello world")
      Property propertyB2 = createPropertyForDomain(projectB,"xxxx eeeee")

      //projectC: create a project with name "test"
      Project projectC = createProject("test")


      //test search with "hello" AND "world"
      def results = SearchAPI.search(["hello","world"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 ==results.code
      assert SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))

      results = SearchAPI.searchResults([projectA.id,projectB.id],["hello","world"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 ==results.code
      def json = JSON.parse(results.data)
      def item1 = json.collection[0]
      assert item1["id"]==projectB.id
      assert item1["matching"][0].type== "property"
      def item2 = json.collection[1]
      assert item2["id"]==projectA.id
      assert item2["matching"][0].type== "description" || item2["matching"][1].type== "description"
      assert item2["matching"][0].value.contains("blabla cytomine world") || item2["matching"][1].value.contains("blabla cytomine world")

      //test search with "hello world" expression, only in property from B
      results = SearchAPI.search(["hello world"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 ==results.code
      assert !SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))
  }


    void testAnnotationSearch() {
        Project projectA = createProject("annotation")

        Description descriptionA = createDescriptionForDomain(projectA,"blabla cytomine world")

        Description descriptionA = createDescriptionForDomain(projectA,"blabla cytomine world")
    }

    void testImageSearch() {

    }

    void testMixSearch() {

    }


    void testLimitPerProject() {

    }


    void testMaxWordsLimit() {
        def results = SearchAPI.search(["not","too","much"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        results = SearchAPI.search(["there","is","one","word","too","much"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
        results = SearchAPI.searchResults([1,2,3],["there","is","one","word","too","much"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code

    }

    void testMinWordsLimit() {
        def results = SearchAPI.search(["siz","e is","ok!"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        results = SearchAPI.search(["aa","is too ","short"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
        results = SearchAPI.search([""],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
        results = SearchAPI.searchResults([1,2,3],["aa","is too ","short"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
    }

    void testMinResuls() {
        def results = SearchAPI.searchResults([],["hello"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
    }

    void testAvoidSpecialChar() {
        def results = SearchAPI.search(["*****"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
        results = SearchAPI.search(["%%%%%%%%"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
        results = SearchAPI.search(["______"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 400 ==results.code
    }

    private Project createProject(String name) {
        Project projectA = BasicInstanceBuilder.getProjectNotExist(true)
        projectA.name = "$name ${new Date().getTime()}"
        BasicInstanceBuilder.saveDomain(projectA)
    }

    private UserAnnotation createAnnotation(Project project) {
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(project,true)
        return annotation
    }

    private ImageInstance createImageInstance(Project project) {
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)
        return image
    }

    private AbstractImage createAbstractImage() {
        AbstractImage image = BasicInstanceBuilder.getAbstractImageNotExist(true)
        return image
    }

    private Description createDescriptionForDomain(CytomineDomain domain, String data) {
        Description description = BasicInstanceBuilder.getDescriptionNotExist(domain,true)
        description.data = data
        BasicInstanceBuilder.saveDomain(description)
    }

    private Property createPropertyForDomain(CytomineDomain domain, String data) {
        Property property = new Property(domain: domain, key: 'key '+new Date().getTime(), value:data)
        BasicInstanceBuilder.saveDomain(property)
    }


}
