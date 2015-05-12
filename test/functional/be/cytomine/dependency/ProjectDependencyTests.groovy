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

import be.cytomine.image.ImageInstance
import be.cytomine.ontology.*
import be.cytomine.processing.ImageFilterProject
import be.cytomine.processing.Job
import be.cytomine.processing.SoftwareProject
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.ProjectAPI
import be.cytomine.test.http.TaskAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class ProjectDependencyTests  {

    void testProjectDependency() {
        //create a term and all its dependence domain
        def dependentDomain = createProjectWithDependency()
        def project = dependentDomain.first()
        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (200 == ProjectAPI.delete(project.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code)

        //TODO: uncomment this after implementing full softdelete
//        //check if all dependency are not aivalable
//        BasicInstanceBuilder.checkIfDomainsNotExist(dependentDomain)
//
//        //undo op (re create) => CANNOT UNDO DELETE PROJECT!
//        assert (404 == ProjectAPI.undo(Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code)
    }

    void testProjectDependencyWithImage() {
        //create a term and all its dependence domain
        def project = BasicInstanceBuilder.getProjectNotExist(true)
        def image = BasicInstanceBuilder.getImageInstanceNotExist(project,true)

        BasicInstanceBuilder.checkIfDomainExist(project)
        BasicInstanceBuilder.checkIfDomainExist(image)

        //try to delete term
        assert (200 == ProjectAPI.delete(project.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code)

        BasicInstanceBuilder.checkIfDomainExist(project,false)
        BasicInstanceBuilder.checkIfDomainExist(image,false)
    }


    void testProjectDependencyWithTask() {



        //create a term and all its dependence domain
        def dependentDomain = createProjectWithDependency()
        def project = dependentDomain.first()

        def result = TaskAPI.create(BasicInstanceBuilder.getProject().id, Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        //JSON.parse(result.data).task.id
        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (200 == ProjectAPI.delete(project.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD,JSON.parse(result.data).task.id).code)

        //TODO: uncomment this after implementing full softdelete
//        //check if all dependency are not aivalable
//        BasicInstanceBuilder.checkIfDomainsNotExist(dependentDomain)
//
//        //undo op (re create) => CANNOT UNDO DELETE PROJECT!
//        assert (404 == ProjectAPI.undo(Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code)
//
//        task = task.getFromDatabase(task.id)
//        println "###############################################"
//        println task.getLastComments(9999).join("\n")
//        println "###############################################"

    }

//
//    void testProjectDependencyWithErrorRecovering() {
//        //create a term and all its dependence domain
//        def dependentDomain = createProjectWithDependency()
//        def project = dependentDomain.first()
//        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)
//
//        UploadedFile file = new UploadedFile(user:User.findByUsername(Infos.SUPERADMINLOGIN), project: project,filename:"x",originalFilename:"y",convertedFilename:"z",convertedExt:"a",ext:"b",path:"c",contentType:"d")
//        BasicInstanceBuilder.saveDomain(file)
//
//        BasicInstanceBuilder.checkIfDomainsExist([file])
//
//        //try to delete term
//        assert (ConstraintException.CODE==ProjectAPI.delete(project.id,Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD).code)
//
//        //check if all dependency are not aivalable
//        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)
//        BasicInstanceBuilder.checkIfDomainsExist([file])
//    }



//    Service ProjectService must exist and must contains deleteDependentHasManyRetrievalProject(Project,transaction)!!!


    private def createProjectWithDependency() {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        Term term = BasicInstanceBuilder.getTermNotExist()
        term.ontology = project.ontology
        BasicInstanceBuilder.saveDomain(term)

        //Add algo annotation
        AlgoAnnotation algoAnnotation =  BasicInstanceBuilder.getAlgoAnnotationNotExist()
        algoAnnotation.project = project
        BasicInstanceBuilder.saveDomain(algoAnnotation)
        algoAnnotation.project = project
        BasicInstanceBuilder.saveDomain(algoAnnotation)

        //create an algo annotation term for this term
        AlgoAnnotationTerm algoAnnotationTerm1 = BasicInstanceBuilder.getAlgoAnnotationTermNotExist()
        algoAnnotationTerm1.term = term
        algoAnnotationTerm1.expectedTerm = term
        algoAnnotationTerm1.annotation = algoAnnotation
        BasicInstanceBuilder.saveDomain(algoAnnotationTerm1)

        //create an annotation with this term
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTermNotExist()
        annotationTerm.term = term
        annotationTerm.userAnnotation.project = project
        BasicInstanceBuilder.saveDomain(annotationTerm.userAnnotation)
        BasicInstanceBuilder.saveDomain(annotationTerm)

        //create annotation filter
        AnnotationFilter af = BasicInstanceBuilder.getAnnotationFilterNotExist()
        af.project = project
        BasicInstanceBuilder.saveDomain(af)

        //craete image filter poroject
        ImageFilterProject ifp = BasicInstanceBuilder.getImageFilterProjectNotExist()
        ifp.project = project
        BasicInstanceBuilder.saveDomain(ifp)

        ImageInstance ia = BasicInstanceBuilder.getImageInstanceNotExist()
        ia.project = project
        BasicInstanceBuilder.saveDomain(ia)

        Job job = BasicInstanceBuilder.getJobNotExist()
        job.project = project
        BasicInstanceBuilder.saveDomain(job)

        ReviewedAnnotation ra = BasicInstanceBuilder.getReviewedAnnotationNotExist()
        ra.project = project
        ra.putParentAnnotation(algoAnnotation)
        ra.terms?.clear()
        ra.addToTerms(term)
        BasicInstanceBuilder.saveDomain(ra)
        ra.project = project
        BasicInstanceBuilder.saveDomain(ra)
        log.info "*********************"
        log.info project.id+""
        log.info ra.project.id+""

        SoftwareProject sp = BasicInstanceBuilder.getSoftwareProjectNotExist()
        sp.project = project
        BasicInstanceBuilder.saveDomain(sp)

        project.retrievalProjects?.clear()
        project.addToRetrievalProjects(project)
        project.addToRetrievalProjects(BasicInstanceBuilder.getProjectNotExist())

        return [project, algoAnnotation, algoAnnotationTerm1,annotationTerm.userAnnotation,annotationTerm,af,ifp,ia,job,ra,sp]
    }


}
