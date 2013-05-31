package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AnnotationFilter
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.sql.AnnotationListing
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationFilterAPI
import be.cytomine.test.http.UserAnnotationAPI
import be.cytomine.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class AnnotationListingTests {


    void testListAnnotationPropertyShow() {

        def dataSet = createAnnotationSet()

        def result = UserAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,null)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        println json
        println json.collection.get(0)
        checkForProperties(json.collection.get(0),['id','term','created','area','project','location'])

        def expectedProp = ['showBasic', 'showWKT']
        println "expectedProp=$expectedProp"
        result = UserAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,expectedProp)
        json = (JSON.parse(result.data))
        println "x=" + json
        println "x=" + json.collection

        println  json.collection
        checkForProperties(json.collection.get(0),['id',"location"],['term','created','area','project'])

        expectedProp = ['showAll', 'hideWKT']
        result = UserAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,expectedProp)
        json = (JSON.parse(result.data))
        checkForProperties(json.collection.get(0),['id','term','created','area','project'],['location'])

        expectedProp = ['showWKT', 'hideWKT']
        result = UserAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,expectedProp)
        assert 404 == result.code
    }


    static def checkForProperties(JSONObject jsonObject, def expectedProperties = null, def unexpectedProperties = null) {

        expectedProperties.each {
            assert jsonObject.containsKey(it)
        }
        if(unexpectedProperties) {
            unexpectedProperties.each {
                assert !jsonObject.containsKey(it)
            }
        }
    }

    def createAnnotationSet() {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist(project, true)
        User me = User.findByUsername(Infos.GOODLOGIN)
        Term term =  BasicInstanceBuilder.getTermNotExist(project.ontology,true)

        UserAnnotation a1 =  BasicInstanceBuilder.getUserAnnotationNotExist(image, me, term)
        UserAnnotation a2 =  BasicInstanceBuilder.getUserAnnotationNotExist(image, me, term)
        UserAnnotation a3 =  BasicInstanceBuilder.getUserAnnotationNotExist(image, me, term)

        UserAnnotation a4 =  BasicInstanceBuilder.getUserAnnotationNotExist(image, me, null)

        return [project:project,image:image,user:me,term:term,annotations:[a1,a2,a3,a4]]
    }


  void testListAnnotationSearchByImage() {

      def dataSet = createAnnotationSet()

      def result = UserAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
      assert json.collection.size()==dataSet.annotations.size()

      dataSet.annotations[2].image = BasicInstanceBuilder.getImageInstanceNotExist( dataSet.project,true)
      BasicInstanceBuilder.saveDomain(dataSet.annotations[2])

      result = UserAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert JSON.parse(result.data).collection instanceof JSONArray
      assert JSON.parse(result.data).collection.size()==dataSet.annotations.size() -1

  }


    void testListAnnotationSearchByMultipleTerm() {

        def dataSet = createAnnotationSet()

        def result = UserAnnotationAPI.listByProjectAndUsersSeveralTerm(dataSet.project.id,dataSet.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()==0

        AnnotationTerm at = BasicInstanceBuilder.getAnnotationTermNotExist(dataSet.annotations[2],true)
        BasicInstanceBuilder.saveDomain(at)

        result = UserAnnotationAPI.listByProjectAndUsersSeveralTerm(dataSet.project.id,dataSet.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert JSON.parse(result.data).collection instanceof JSONArray
        assert JSON.parse(result.data).collection.size()==1

    }


    void testListAnnotationSearchByNoTerm() {
        def dataSet = createAnnotationSet()
        def result = UserAnnotationAPI.listByProjectAndUsersWithoutTerm(dataSet.project.id,dataSet.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()==1

    }

    void testListAnnotationSearchByProjectTerm() {
        def dataSet = createAnnotationSet()
        def result = UserAnnotationAPI.listByProjectAndUsers(dataSet.project.id,dataSet.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()==dataSet.annotations.size()

    }

}
