package be.cytomine.dependency

import be.cytomine.project.Project
import be.cytomine.test.Infos

import be.cytomine.test.BasicInstance
import be.cytomine.ontology.*
import be.cytomine.processing.ImageFilterProject
import be.cytomine.image.ImageInstance
import be.cytomine.processing.Job
import be.cytomine.social.LastConnection
import be.cytomine.security.User
import be.cytomine.processing.SoftwareProject
import be.cytomine.utils.Task
import be.cytomine.social.UserPosition
import be.cytomine.test.http.ProjectAPI

import be.cytomine.image.UploadedFile
import be.cytomine.Exception.ConstraintException

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
        BasicInstance.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (200 == ProjectAPI.delete(project.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstance.checkIfDomainsNotExist(dependentDomain)

        //undo op (re create) => CANNOT UNDO DELETE PROJECT!
        assert (404 == ProjectAPI.undo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)
    }


    void testProjectDependencyWithTask() {



        //create a term and all its dependence domain
        def dependentDomain = createProjectWithDependency()
        def project = dependentDomain.first()
        Task task = new Task(userIdent: User.findByUsername(Infos.GOODLOGIN).id,projectIdent: project.id)
        task = task.saveOnDatabase()

        BasicInstance.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (200 == ProjectAPI.delete(project.id,Infos.GOODLOGIN,Infos.GOODPASSWORD,task).code)

        //check if all dependency are not aivalable
        BasicInstance.checkIfDomainsNotExist(dependentDomain)

        //undo op (re create) => CANNOT UNDO DELETE PROJECT!
        assert (404 == ProjectAPI.undo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        task = task.getFromDatabase(task.id)
        println "###############################################"
        println task.getLastComments(9999).join("\n")
        println "###############################################"

    }

//
//    void testProjectDependencyWithErrorRecovering() {
//        //create a term and all its dependence domain
//        def dependentDomain = createProjectWithDependency()
//        def project = dependentDomain.first()
//        BasicInstance.checkIfDomainsExist(dependentDomain)
//
//        UploadedFile file = new UploadedFile(user:User.findByUsername(Infos.GOODLOGIN), project: project,filename:"x",originalFilename:"y",convertedFilename:"z",convertedExt:"a",ext:"b",path:"c",contentType:"d")
//        BasicInstance.saveDomain(file)
//
//        BasicInstance.checkIfDomainsExist([file])
//
//        //try to delete term
//        assert (ConstraintException.CODE==ProjectAPI.delete(project.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)
//
//        //check if all dependency are not aivalable
//        BasicInstance.checkIfDomainsExist(dependentDomain)
//        BasicInstance.checkIfDomainsExist([file])
//    }



//    Service ProjectService must exist and must contains deleteDependentHasManyRetrievalProject(Project,transaction)!!!


    private def createProjectWithDependency() {
        Project project = BasicInstance.createBasicProjectNotExist()
        Term term = BasicInstance.getBasicTermNotExist()
        term.ontology = project.ontology
        BasicInstance.saveDomain(term)

        //Add algo annotation
        AlgoAnnotation algoAnnotation =  BasicInstance.getBasicAlgoAnnotationNotExist()
        algoAnnotation.project = project
        BasicInstance.saveDomain(algoAnnotation)
        BasicInstance.checkDomain(algoAnnotation)
        algoAnnotation.project = project
        BasicInstance.saveDomain(algoAnnotation)

        //create an algo annotation term for this term
        AlgoAnnotationTerm algoAnnotationTerm1 = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        algoAnnotationTerm1.term = term
        algoAnnotationTerm1.expectedTerm = term
        algoAnnotationTerm1.annotation = algoAnnotation
        BasicInstance.saveDomain(algoAnnotationTerm1)

        //create an annotation with this term
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.term = term
        annotationTerm.userAnnotation.project = project
        BasicInstance.saveDomain(annotationTerm.userAnnotation)
        BasicInstance.saveDomain(annotationTerm)

        //create annotation filter
        AnnotationFilter af = BasicInstance.getBasicAnnotationFilterNotExist()
        af.project = project
        BasicInstance.saveDomain(af)

        //craete image filter poroject
        ImageFilterProject ifp = BasicInstance.getBasicImageFilterProjectNotExist()
        ifp.project = project
        BasicInstance.saveDomain(ifp)

        ImageInstance ia = BasicInstance.getBasicImageInstanceNotExist()
        ia.project = project
        BasicInstance.saveDomain(ia)

        Job job = BasicInstance.getBasicJobNotExist()
        job.project = project
        BasicInstance.saveDomain(job)

        ReviewedAnnotation ra = BasicInstance.getBasicReviewedAnnotationNotExist()
        ra.project = project
        ra.putParentAnnotation(algoAnnotation)
        ra.terms?.clear()
        ra.addToTerms(term)
        BasicInstance.checkDomain(ra)
        BasicInstance.saveDomain(ra)
        ra.project = project
        BasicInstance.saveDomain(ra)
        log.info "*********************"
        log.info project.id+""
        log.info ra.project.id+""

        SoftwareProject sp = BasicInstance.getBasicSoftwareProjectNotExist()
        sp.project = project
        BasicInstance.saveDomain(sp)

        project.retrievalProjects?.clear()
        project.addToRetrievalProjects(project)
        project.addToRetrievalProjects(BasicInstance.createBasicProjectNotExist())

        //Not recoverable domain (cannot retrieve it with undo delete)
        LastConnection lc = new LastConnection(project: project, user:  User.findByUsername(Infos.GOODLOGIN))
        BasicInstance.saveDomain(lc)

        UserPosition up = new UserPosition(project: project, user: User.findByUsername(Infos.GOODLOGIN), image: ia)
        BasicInstance.saveDomain(up)

        return [project, algoAnnotation, algoAnnotationTerm1,annotationTerm.userAnnotation,annotationTerm,af,ifp,ia,job,ra,sp]
    }


}
