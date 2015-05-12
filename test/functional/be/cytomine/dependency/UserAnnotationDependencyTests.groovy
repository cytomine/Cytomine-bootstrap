package be.cytomine.dependency

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

import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.UserAnnotationAPI

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class UserAnnotationDependencyTests  {


    void testUserAnnotationDependency() {

        def dependentDomain = createAnnotationWithDependency(BasicInstanceBuilder.getProjectNotExist(true))

        def annotation = dependentDomain.first()

        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (200 == UserAnnotationAPI.delete(annotation.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstanceBuilder.checkIfDomainsNotExist(dependentDomain)

        //undo op (re create)
        assert (200 == UserAnnotationAPI.undo(Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code)


        //check if all dependency are aivalable
        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)

        //redo op (re-delete)
        assert (200 == UserAnnotationAPI.redo(Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstanceBuilder.checkIfDomainsNotExist(dependentDomain)
    }





    private def createAnnotationWithDependency(Project project) {
        //create a annotation and all its dependence domain

        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist(project,BasicInstanceBuilder.getImageInstance(),true)

        AnnotationTerm at =  BasicInstanceBuilder.getAnnotationTermNotExist(annotation,true)

        AlgoAnnotationTerm algoAnnotationTerm1 = BasicInstanceBuilder.getAlgoAnnotationTermNotExist()
        algoAnnotationTerm1.term = at.term
        algoAnnotationTerm1.expectedTerm = at.term
        algoAnnotationTerm1.annotation = annotation
        algoAnnotationTerm1.term.ontology = annotation.project.ontology
        BasicInstanceBuilder.saveDomain(algoAnnotationTerm1.term)
        BasicInstanceBuilder.saveDomain(algoAnnotationTerm1)
//
//        ReviewedAnnotation ra = BasicInstanceBuilder.getReviewedAnnotationNotExist()
//        ra.project = project
//        ra.putParentAnnotation(annotation)
//        ra.terms?.clear()
//        ra.addToTerms(at.term)
//        BasicInstanceBuilder.checkDomain(ra)
//        BasicInstanceBuilder.saveDomain(ra)
//        ra.project = project
//        ra.putParentAnnotation(annotation)
//        BasicInstanceBuilder.saveDomain(ra)

        return [annotation,at,algoAnnotationTerm1]
    }








}
