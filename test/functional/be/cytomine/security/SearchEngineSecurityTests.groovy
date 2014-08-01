package be.cytomine.security

import be.cytomine.CytomineDomain
import be.cytomine.SearchEngineTests
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.*
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.SearchAPI
import be.cytomine.utils.Description
import grails.converters.JSON

class SearchEngineSecurityTests {


    void testMixSearch() {
        //PROJECT
        Project projectA = SearchEngineTests.createProject("mix")
        SearchEngineTests.createDescriptionForDomain(projectA,"blabla mix world")

        //ABSTRRACIMAGE
        AbstractImage abstractImage1 = SearchEngineTests.createAbstractImage()
        abstractImage1.originalFilename = "blablablmixmix_World.jpg"
        BasicInstanceBuilder.saveDomain(abstractImage1)

        //IMAGEINSTANCE
        ImageInstance image1 = SearchEngineTests.createImageInstance(projectA)
        SearchEngineTests.createDescriptionForDomain(image1,"mix mix")

        //REVIEWEDANNOTATION
        ReviewedAnnotation annotation3 = SearchEngineTests.createReviewedAnnotation(projectA)
        SearchEngineTests.createDescriptionForDomain(annotation3,"mix")

        //USERANNOTATION
        UserAnnotation annotation1 = SearchEngineTests.createUserAnnotation(projectA)
        SearchEngineTests.createDescriptionForDomain(annotation1,"mix")

        //REVIEWEDANNOTATION
        AlgoAnnotation annotation2 = SearchEngineTests.createAlgoAnnotation(projectA)
        SearchEngineTests.createPropertyForDomain(annotation2,"mix")

        def results = SearchAPI.search(["mix"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        assert SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))

        results = SearchAPI.searchResults([projectA.id,abstractImage1.id,image1.id,annotation1.id,annotation2.id,annotation3.id],["mix"],null,null,null,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 ==results.code
        def json = JSON.parse(results.data)
        assert SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))

        User user = BasicInstanceBuilder.getUser("a_user_with_righ","password")


        results = SearchAPI.search(["mix"],null,null,null,user.username,"password")
        assert 200 ==results.code
        assert !SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))

        results = SearchAPI.searchResults([projectA.id,abstractImage1.id,image1.id,annotation1.id,annotation2.id,annotation3.id],["mix"],null,null,null,user.username,"password")
        assert 200 ==results.code
        assert !SearchAPI.containsInJSONList(projectA.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(abstractImage1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(image1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation3.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation1.id,JSON.parse(results.data))
        assert !SearchAPI.containsInJSONList(annotation2.id,JSON.parse(results.data))
    }




}
