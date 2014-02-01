package be.cytomine.dependency

import be.cytomine.ontology.*
import be.cytomine.project.Project
import be.cytomine.test.BasicInstanceBuilder
import be.cytomine.test.Infos
import be.cytomine.test.http.OntologyAPI
import be.cytomine.test.http.TermAPI

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
        def dependentDomain = createTermWithDependency(BasicInstanceBuilder.getProjectNotExist(true))
        def term = dependentDomain.first()
        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (200 == TermAPI.delete(term.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstanceBuilder.checkIfDomainsNotExist(dependentDomain)

        //undo op (re create)
        assert (200 == TermAPI.undo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)


        //check if all dependency are aivalable
        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)

        //redo op (re-delete)
        assert (200 == TermAPI.redo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstanceBuilder.checkIfDomainsNotExist(dependentDomain)
    }

    void testTermDependencyWithAnnotation() {
        //create a term and all its dependence domain
        def dependentDomain = createTermWithDependencyRefuse(BasicInstanceBuilder.getProjectNotExist(true))
        def term = dependentDomain.first()
        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (400 == TermAPI.delete(term.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)
    }

    void testTermDependencyWithReviewedAnnotation() {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        def dependentDomain = createTermWithDependency(project)
        Term term = dependentDomain.first()
        //change ontology for project with this ontology (cannot delete an ontology with project)
        project.ontology = BasicInstanceBuilder.getOntology()
        BasicInstanceBuilder.saveDomain(project)

        BasicInstanceBuilder.saveDomain(term)

        ReviewedAnnotation annotation = BasicInstanceBuilder.getReviewedAnnotation()
        annotation.project = project
        annotation.addToTerms(term)
        BasicInstanceBuilder.saveDomain(annotation)

        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)

        assert(400 == TermAPI.delete(term.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)
    }

    void testOntologyDependency() {
        //create a term and all its dependence domain
        def dependentDomain = createOntologyWithDependency(BasicInstanceBuilder.getProjectNotExist(true))
        def ontology = dependentDomain.first()
        //change ontology for project with this ontology (cannot delete an ontology with project)
        Project.findAllByOntology(ontology).each {
            it.ontology = BasicInstanceBuilder.getOntology()
            BasicInstanceBuilder.saveDomain(it)
        }
        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)

        //try to delete term
        assert (200 == OntologyAPI.delete(ontology.id,Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstanceBuilder.checkIfDomainsNotExist(dependentDomain)

        //undo op (re create)
        def res = OntologyAPI.undo(Infos.GOODLOGIN,Infos.GOODPASSWORD)
        assert (200 == res.code)
        println "*************** coco"
        println res.data


        //check if all dependency are aivalable
        BasicInstanceBuilder.checkIfDomainsExist(dependentDomain)

        //redo op (re-delete)
        assert (200 == OntologyAPI.redo(Infos.GOODLOGIN,Infos.GOODPASSWORD).code)

        //check if all dependency are not aivalable
        BasicInstanceBuilder.checkIfDomainsNotExist(dependentDomain)
    }



    private def createTermWithDependency(Project project) {

        //create a term x, link with ontology
        Term term = BasicInstanceBuilder.getTermNotExist()
        term.ontology = project.ontology
        BasicInstanceBuilder.saveDomain(term)

        //create a relation with term x and another term y
        Term anotherTerm1 = BasicInstanceBuilder.getTermNotExist()
        anotherTerm1.ontology = project.ontology
        BasicInstanceBuilder.saveDomain(anotherTerm1)

        RelationTerm rt1 = new RelationTerm(term1: term, term2: anotherTerm1, relation: Relation.findByName(RelationTerm.names.PARENT))
        BasicInstanceBuilder.saveDomain(rt1)


        //create a relation with another term y and term x
        Term anotherTerm2 = BasicInstanceBuilder.getTermNotExist()
        anotherTerm1.ontology = project.ontology
        BasicInstanceBuilder.saveDomain(anotherTerm1)

        RelationTerm rt2 = new RelationTerm(term2: term, term1: anotherTerm1, relation: Relation.findByName(RelationTerm.names.PARENT))
        BasicInstanceBuilder.saveDomain(rt2)

        return [term,rt1,rt2]
    }

    private def createTermWithDependencyRefuse(Project project) {

        //create a term x, link with ontology
        Term term = BasicInstanceBuilder.getTermNotExist()
        term.ontology = project.ontology
        BasicInstanceBuilder.saveDomain(term)

        //create a relation with term x and another term y
        Term anotherTerm1 = BasicInstanceBuilder.getTermNotExist()
        anotherTerm1.ontology = project.ontology
        BasicInstanceBuilder.saveDomain(anotherTerm1)

        RelationTerm rt1 = new RelationTerm(term1: term, term2: anotherTerm1, relation: Relation.findByName(RelationTerm.names.PARENT))
        BasicInstanceBuilder.saveDomain(rt1)


        //create a relation with another term y and term x
        Term anotherTerm2 = BasicInstanceBuilder.getTermNotExist()
        anotherTerm1.ontology = project.ontology
        BasicInstanceBuilder.saveDomain(anotherTerm1)

        RelationTerm rt2 = new RelationTerm(term2: term, term1: anotherTerm1, relation: Relation.findByName(RelationTerm.names.PARENT))
        BasicInstanceBuilder.saveDomain(rt2)

        //create an annotation with this term
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTermNotExist()
        annotationTerm.term = term
        annotationTerm.userAnnotation.project = project
        BasicInstanceBuilder.saveDomain(annotationTerm.userAnnotation)
        BasicInstanceBuilder.saveDomain(annotationTerm)

        //create an algo annotation term for this term
        AlgoAnnotationTerm algoAnnotationTerm1 = BasicInstanceBuilder.getAlgoAnnotationTermNotExist()
        algoAnnotationTerm1.term = anotherTerm1
        algoAnnotationTerm1.expectedTerm = term
        algoAnnotationTerm1.retrieveAnnotationDomain().project = project
        BasicInstanceBuilder.saveDomain(algoAnnotationTerm1.retrieveAnnotationDomain())
        BasicInstanceBuilder.saveDomain(algoAnnotationTerm1)

        //create an algo annotation term for this term (expected term)
        AlgoAnnotationTerm algoAnnotationTerm2 = BasicInstanceBuilder.getAlgoAnnotationTermNotExist()
        algoAnnotationTerm2.term = term
        algoAnnotationTerm2.expectedTerm = anotherTerm1
        algoAnnotationTerm2.retrieveAnnotationDomain().project = project
        BasicInstanceBuilder.saveDomain(algoAnnotationTerm2.retrieveAnnotationDomain())
        BasicInstanceBuilder.saveDomain(algoAnnotationTerm2)

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
