package be.cytomine.security

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.SearchEngineTests
import be.cytomine.image.AbstractImage
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.SearchAPI
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
