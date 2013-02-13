package be.cytomine.dependency

import be.cytomine.test.Infos

import be.cytomine.test.BasicInstance

import be.cytomine.project.Project
import be.cytomine.ontology.Term
import be.cytomine.ontology.RelationTerm
import be.cytomine.ontology.Relation
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.AlgoAnnotationTerm

import be.cytomine.test.http.TermAPI
import be.cytomine.ontology.Ontology
import be.cytomine.test.http.OntologyAPI
import be.cytomine.ontology.ReviewedAnnotation

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class TermDependencyTests  {

//Service TermService must implement deleteDependentAlgoAnnotationTerm(Term,transaction)!!!
//Service TermService must implement deleteDependentAlgoAnnotationTerm(Term,transaction)!!!
//Service TermService must implement deleteDependentAnnotationTerm(Term,transaction)!!!
//Service TermService must implement deleteDependentRelationTerm(Term,transaction)!!!
//Service TermService must implement deleteDependentRelationTerm(Term,transaction)!!!
//    Service termService must implement deleteDependentHasManyAnnotationFilter(AnnotationFilter,transaction)!!!
//    Service termService must implement deleteDependentHasManyReviewedAnnotation(ReviewedAnnotation,transaction)!!!





    void testTermDependency() {
        //create a term and all its dependence domain
        def dependentDomain = createTermWithDependency(BasicInstance.createBasicProjectNotExist())
        def term = dependentDomain.first()
        BasicInstance.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (200 == TermAPI.delete(term.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstance.checkIfDomainsNotExist(dependentDomain)

        //undo op (re create)
        assert (200 == TermAPI.undo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)


        //check if all dependency are aivalable
        BasicInstance.checkIfDomainsExist(dependentDomain)

        //redo op (re-delete)
        assert (200 == TermAPI.redo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstance.checkIfDomainsNotExist(dependentDomain)
    }

    void testTermDependencyWithAnnotation() {
        //create a term and all its dependence domain
        def dependentDomain = createTermWithDependencyRefuse(BasicInstance.createBasicProjectNotExist())
        def term = dependentDomain.first()
        BasicInstance.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (400 == TermAPI.delete(term.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)
    }

    void testTermDependencyWithReviewedAnnotation() {
        Project project = BasicInstance.createBasicProjectNotExist()
        def dependentDomain = createTermWithDependency(project)
        Term term = dependentDomain.first()
        //change ontology for project with this ontology (cannot delete an ontology with project)
        project.ontology = BasicInstance.createOrGetBasicOntology()
        BasicInstance.saveDomain(project)

        BasicInstance.saveDomain(term)

        ReviewedAnnotation annotation = BasicInstance.createOrGetBasicReviewedAnnotation()
        annotation.project = project
        annotation.addToTerms(term)
        BasicInstance.saveDomain(annotation)

        BasicInstance.checkIfDomainsExist(dependentDomain)

        assert(400 == TermAPI.delete(term.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        BasicInstance.checkIfDomainsExist(dependentDomain)
    }

    void testOntologyDependency() {
        //create a term and all its dependence domain
        def dependentDomain = createOntologyWithDependency(BasicInstance.createBasicProjectNotExist())
        def ontology = dependentDomain.first()
        //change ontology for project with this ontology (cannot delete an ontology with project)
        Project.findAllByOntology(ontology).each {
            it.ontology = BasicInstance.createOrGetBasicOntology()
            BasicInstance.saveDomain(it)
        }
        BasicInstance.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (200 == OntologyAPI.delete(ontology.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstance.checkIfDomainsNotExist(dependentDomain)

        //undo op (re create)
        assert (200 == OntologyAPI.undo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)


        //check if all dependency are aivalable
        BasicInstance.checkIfDomainsExist(dependentDomain)

        //redo op (re-delete)
        assert (200 == OntologyAPI.redo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstance.checkIfDomainsNotExist(dependentDomain)
    }



    private def createTermWithDependency(Project project) {

        //create a term x, link with ontology
        Term term = BasicInstance.getBasicTermNotExist()
        term.ontology = project.ontology
        BasicInstance.saveDomain(term)

        //create a relation with term x and another term y
        Term anotherTerm1 = BasicInstance.getBasicTermNotExist()
        anotherTerm1.ontology = project.ontology
        BasicInstance.saveDomain(anotherTerm1)

        RelationTerm rt1 = new RelationTerm(term1: term, term2: anotherTerm1, relation: Relation.findByName(RelationTerm.names.PARENT))
        BasicInstance.saveDomain(rt1)


        //create a relation with another term y and term x
        Term anotherTerm2 = BasicInstance.getBasicTermNotExist()
        anotherTerm1.ontology = project.ontology
        BasicInstance.saveDomain(anotherTerm1)

        RelationTerm rt2 = new RelationTerm(term2: term, term1: anotherTerm1, relation: Relation.findByName(RelationTerm.names.PARENT))
        BasicInstance.saveDomain(rt2)

        return [term,rt1,rt2]
    }

    private def createTermWithDependencyRefuse(Project project) {

        //create a term x, link with ontology
        Term term = BasicInstance.getBasicTermNotExist()
        term.ontology = project.ontology
        BasicInstance.saveDomain(term)

        //create a relation with term x and another term y
        Term anotherTerm1 = BasicInstance.getBasicTermNotExist()
        anotherTerm1.ontology = project.ontology
        BasicInstance.saveDomain(anotherTerm1)

        RelationTerm rt1 = new RelationTerm(term1: term, term2: anotherTerm1, relation: Relation.findByName(RelationTerm.names.PARENT))
        BasicInstance.saveDomain(rt1)


        //create a relation with another term y and term x
        Term anotherTerm2 = BasicInstance.getBasicTermNotExist()
        anotherTerm1.ontology = project.ontology
        BasicInstance.saveDomain(anotherTerm1)

        RelationTerm rt2 = new RelationTerm(term2: term, term1: anotherTerm1, relation: Relation.findByName(RelationTerm.names.PARENT))
        BasicInstance.saveDomain(rt2)

        //create an annotation with this term
        AnnotationTerm annotationTerm = BasicInstance.getBasicAnnotationTermNotExist("")
        annotationTerm.term = term
        annotationTerm.userAnnotation.project = project
        BasicInstance.saveDomain(annotationTerm.userAnnotation)
        BasicInstance.saveDomain(annotationTerm)

        //create an algo annotation term for this term
        AlgoAnnotationTerm algoAnnotationTerm1 = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        algoAnnotationTerm1.term = anotherTerm1
        algoAnnotationTerm1.expectedTerm = term
        algoAnnotationTerm1.retrieveAnnotationDomain().project = project
        BasicInstance.saveDomain(algoAnnotationTerm1.retrieveAnnotationDomain())
        BasicInstance.saveDomain(algoAnnotationTerm1)

        //create an algo annotation term for this term (expected term)
        AlgoAnnotationTerm algoAnnotationTerm2 = BasicInstance.getBasicAlgoAnnotationTermNotExist()
        algoAnnotationTerm2.term = term
        algoAnnotationTerm2.expectedTerm = anotherTerm1
        algoAnnotationTerm2.retrieveAnnotationDomain().project = project
        BasicInstance.saveDomain(algoAnnotationTerm2.retrieveAnnotationDomain())
        BasicInstance.saveDomain(algoAnnotationTerm2)

        return [term,rt1,rt2,annotationTerm,algoAnnotationTerm1,algoAnnotationTerm2]
    }



    private def createOntologyWithDependency(Project project) {
        //create a term x, link with ontology
        Ontology ontology = project.ontology
        def domains = []
        domains.add(ontology)
        domains.addAll(createTermWithDependency(project))
        return domains
    }
}
