package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.*
import be.cytomine.processing.Job
import be.cytomine.processing.RoiAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.AnnotationDomainAPI
import be.cytomine.test.http.DomainAPI
import be.cytomine.test.http.RoiAnnotationAPI
import com.vividsolutions.jts.io.WKTReader
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
class RoiAnnotationListingTests {


    void testListAnnotationPropertyShow() {

        def dataSet = createAnnotationSet()

        def result = RoiAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,null)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        println json
        println json.collection.get(0)
        checkForProperties(json.collection.get(0),['id','created','project','image'])

        def expectedProp = ['showBasic', 'showWKT']
        println "expectedProp=$expectedProp"
        result = RoiAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,expectedProp)
        json = (JSON.parse(result.data))
        println "x=" + json
        println "x=" + json.collection

        println  json.collection
        checkForProperties(json.collection.get(0),['id',"location"],['created','area','project'])

        expectedProp = ['showDefault', 'hideMeta']
        result = RoiAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,expectedProp)
        json = (JSON.parse(result.data))
        checkForProperties(json.collection.get(0),['id'],['location','created','project'])

        expectedProp = ['showBasic', 'showImage']
        result = RoiAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,expectedProp)
        json = (JSON.parse(result.data))
        checkForProperties(json.collection.get(0),['id','originalfilename'],['location'])

        expectedProp = ['showWKT', 'hideWKT','hideBasic','hideMeta']
        result = RoiAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD,expectedProp)
        assert 404 == result.code
    }




  void testListAnnotationSearchByImage() {

      def dataSet = createAnnotationSet()

      def result = RoiAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
      assert json.collection.size()==dataSet.annotations.size()
      //generic way test
      checkRoiAnnotationResultNumber("image=${dataSet.image.id}",dataSet.annotations.size())

      dataSet.annotations[2].image = BasicInstanceBuilder.getImageInstanceNotExist( dataSet.project,true)
      BasicInstanceBuilder.saveDomain(dataSet.annotations[2])

      result = RoiAnnotationAPI.listByImage(dataSet.image.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
      assert JSON.parse(result.data).collection instanceof JSONArray
      assert JSON.parse(result.data).collection.size()==dataSet.annotations.size() -1
       //generic way test
      checkRoiAnnotationResultNumber("image=${dataSet.image.id}",dataSet.annotations.size()-1)
  }

    void testListAnnotationSearchByImageAndUser() {

        def dataSet = createAnnotationSet()

        def result = RoiAnnotationAPI.listByImageAndUser(dataSet.image.id,dataSet.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()==dataSet.annotations.size()

        //change image and user
        dataSet.annotations[2].image = BasicInstanceBuilder.getImageInstanceNotExist( dataSet.project,true)
        BasicInstanceBuilder.saveDomain(dataSet.annotations[2])
        dataSet.annotations[3].user = BasicInstanceBuilder.getUserNotExist(true)
        BasicInstanceBuilder.saveDomain(dataSet.annotations[3])

        result = RoiAnnotationAPI.listByImageAndUser(dataSet.image.id,dataSet.user.id,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert JSON.parse(result.data).collection instanceof JSONArray
        assert JSON.parse(result.data).collection.size()==dataSet.annotations.size() - 2 //we change 1 for image and 1 for user

    }



    void testListAnnotationSearchByImageAndUserAndBBox() {

        def dataSet = createAnnotationSet()

        def a = "POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))"
        def b = "POLYGON ((1 3, 2 3, 2 5, 1 5, 1 3))"
        def c = "POLYGON ((3 1, 5 1,  5 3, 3 3, 3 1))"
        def d = "POLYGON ((4 4,8 4, 8 7,4 7,4 4))"
         //e intersect a,b and c
        def e = "POLYGON ((2 2, 3 2, 3 4, 2 4, 2 2))"

        dataSet.annotations[0].location = new WKTReader().read(a)
        dataSet.annotations[1].location = new WKTReader().read(b)
        dataSet.annotations[2].location = new WKTReader().read(c)
        dataSet.annotations[3].location = new WKTReader().read(d)

        dataSet.annotations.each {
            BasicInstanceBuilder.saveDomain(it)
        }

        def result = RoiAnnotationAPI.listByImageAndUser(dataSet.image.id, dataSet.user.id, e, true,null,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert json.collection.size()==3 //a,b,c
         //generic way test
        checkRoiAnnotationResultNumber("notReviewedOnly=true&user=${dataSet.user.id}&image=${dataSet.image.id}&bbox=${e.replace(" ","%20")}",3)

        result = RoiAnnotationAPI.listByImageAndUser(dataSet.image.id, dataSet.user.id, e, true,1,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        result = RoiAnnotationAPI.listByImageAndUser(dataSet.image.id, dataSet.user.id, e, true,2,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        result = RoiAnnotationAPI.listByImageAndUser(dataSet.image.id, dataSet.user.id, e, true,3,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

    }

    void testListAnnotationSearchGeneric() {

         def dataSet = createAnnotationSet()

         def a = "POLYGON ((1 1, 2 1, 2 2, 1 2, 1 1))"
         def b = "POLYGON ((1 3, 2 3, 2 5, 1 5, 1 3))"
         def c = "POLYGON ((3 1, 5 1,  5 3, 3 3, 3 1))"
         def d = "POLYGON ((4 4,8 4, 8 7,4 7,4 4))"
          //e intersect a,b and c
         def e = "POLYGON ((2 2, 3 2, 3 4, 2 4, 2 2))"

         dataSet.annotations[0].location = new WKTReader().read(a)
         dataSet.annotations[1].location = new WKTReader().read(b)
         dataSet.annotations[2].location = new WKTReader().read(c)
         dataSet.annotations[3].location = new WKTReader().read(d)

         dataSet.annotations.each {
             BasicInstanceBuilder.saveDomain(it)
         }

        checkRoiAnnotationResultNumber("project=${dataSet.project.id}&hideMeta=true",4)

        dataSet.annotations[0].image = BasicInstanceBuilder.getImageInstanceNotExist(dataSet.project,true)
        BasicInstanceBuilder.saveDomain(dataSet.annotations[0])

        checkRoiAnnotationResultNumber("image=${dataSet.image.id}",3)

        dataSet.annotations[1].user = BasicInstanceBuilder.getUserNotExist(true)
        BasicInstanceBuilder.saveDomain(dataSet.annotations[1])

        checkRoiAnnotationResultNumber("user=${dataSet.user.id}&image=${dataSet.image.id}&project=${dataSet.project.id}",2)

        checkRoiAnnotationResultNumber("users=${dataSet.user.id}&images=${dataSet.image.id}&project=${dataSet.project.id}",2)

     }

    private static void checkRoiAnnotationResultNumber(String url,int expectedResult) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?roi=true&$url"
        def result = DomainAPI.doGET(URL, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection.size()==expectedResult
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

        RoiAnnotation a1 =  BasicInstanceBuilder.getRoiAnnotationNotExist(image, me, true)
        RoiAnnotation a2 =  BasicInstanceBuilder.getRoiAnnotationNotExist(image, me,true)
        RoiAnnotation a3 =  BasicInstanceBuilder.getRoiAnnotationNotExist(image, me,true)

        RoiAnnotation a4 =  BasicInstanceBuilder.getRoiAnnotationNotExist(image, me,true)

        return [project:project,image:image,user:me,annotations:[a1,a2,a3,a4]]
    }



    void testListRoiAnnotationByImageWithCredential() {
        RoiAnnotation annotation = BasicInstanceBuilder.getRoiAnnotation()
        def result = RoiAnnotationAPI.listByImage(annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testListRoiAnnotationByImageNotExistWithCredential() {
        def result = RoiAnnotationAPI.listByImage(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListRoiAnnotationByProjectWithCredential() {
        RoiAnnotation annotation = BasicInstanceBuilder.getRoiAnnotation()
        def result = RoiAnnotationAPI.listByProject(annotation.project.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = RoiAnnotationAPI.listByProject(annotation.project.id, true,Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
       json = JSON.parse(result.data)
    }

    void testListRoiAnnotationByProjectNotExistWithCredential() {
        def result = RoiAnnotationAPI.listByProject(-99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListRoiAnnotationByProjecImageAndUsertWithCredential() {
        RoiAnnotation annotation = BasicInstanceBuilder.getRoiAnnotation()
        def result = RoiAnnotationAPI.listByProject(annotation.project.id, annotation.user.id, annotation.image.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }


    void testListRoiAnnotationByImageAndUserWithCredential() {
        RoiAnnotation annotation = BasicInstanceBuilder.getRoiAnnotation()
        def result = RoiAnnotationAPI.listByImageAndUser(annotation.image.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        result = RoiAnnotationAPI.listByImageAndUser(-99, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
        result = RoiAnnotationAPI.listByImageAndUser(annotation.image.id, -99, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 404 == result.code
    }

    void testListRoiAnnotationyProjectAndUsersWithCredential() {
        RoiAnnotation annotation = BasicInstanceBuilder.getRoiAnnotation()
        def result = RoiAnnotationAPI.listByProjectAndUsers(annotation.project.id, annotation.user.id, Infos.GOODLOGIN, Infos.GOODPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        //assert json.collection instanceof JSONArray
    }


}
