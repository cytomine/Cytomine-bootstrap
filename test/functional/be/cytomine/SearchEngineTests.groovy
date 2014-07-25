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

  void testCreateSet() {
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

      //SEARCH domain = Project, OPERATOR = AND, Project = null, SearchOn = ALL, keys= "hello world"
        //=> dprojectA, projectB

      def results = SearchAPI.search(["hello","world"],Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
      assert 200 ==results.code
      assert SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
      assert SearchAPI.containsInJSONList(projectB.id,JSON.parse(results.data))
      assert !SearchAPI.containsInJSONList(projectC.id,JSON.parse(results.data))



  }

    private Project createProject(String name) {
        Project projectA = BasicInstanceBuilder.getProjectNotExist(true)
        projectA.name = "$name ${new Date().getTime()}"
        BasicInstanceBuilder.saveDomain(projectA)
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
