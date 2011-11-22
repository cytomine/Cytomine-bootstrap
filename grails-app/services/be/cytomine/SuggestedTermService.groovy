package be.cytomine

import be.cytomine.ontology.Annotation
import be.cytomine.ontology.SuggestedTerm
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.command.suggestedTerm.AddSuggestedTermCommand
import grails.converters.JSON
import be.cytomine.command.suggestedTerm.DeleteSuggestedTermCommand

class SuggestedTermService {

    static transactional = true
    def cytomineService
    def commandService

    def list() {
        SuggestedTerm.list()
    }

    def list(Annotation annotation) {
        SuggestedTerm.findAllByAnnotation(annotation)
    }

    def read(Annotation annotation,Term term, Job job) {
        SuggestedTerm.findWhere(annotation: annotation, term: term, job: job)
    }

    def listWorst(Project project, def max) {
        List<SuggestedTerm> results = new ArrayList<SuggestedTerm>()
        List<SuggestedTerm> suggest = SuggestedTerm.findAllByProject(project, [sort: "rate", order: "desc"])

        for (int i = 0; i < suggest.size() && max > results.size(); i++) {
            if (suggest.get(i).annotationMapWithBadTerm())
                results.add(suggest.get(i));
        }
        return results

    }

    def listWorstTerm(Project project, def max) {
        Map<Term, Integer> termMap = new HashMap<Term, Integer>()
        List<Term> termList = Term.findAllByOntology(project.ontology)
        termList.each {termMap.put(it, 0)}

        List<SuggestedTerm> suggest = SuggestedTerm.findAllByProject(project, [sort: "rate", order: "desc"])

        for (int i = 0; i < suggest.size(); i++) {
            if (suggest.get(i).annotationMapWithBadTerm()) {
                Term term = suggest.get(i).term
                termMap.put(term, termMap.get(term) + 1);
            }
        }
        termList.clear()
        termMap.each {  key, value ->
            key.rate = value
            termList.add(key)
        }
        return termList
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        commandService.processCommand(new AddSuggestedTermCommand(user: currentUser), json)
    }

    def delete(def idannotation, def idterm, def idjob) {
        User currentUser = cytomineService.getCurrentUser()
        def result = deleteSuggestedTerm(idannotation, idterm, idjob, currentUser)
        return result
    }

    /**
     * Delete an annotation term
     */
    def deleteSuggestedTerm(def idAnnotation, def idTerm, def idJob, User currentUser) {
        def json = JSON.parse("{annotation: $idAnnotation, term: $idTerm, job: $idJob}")
        def result = commandService.processCommand(new DeleteSuggestedTermCommand(user: currentUser), json)
        return result
    }

    /**
     * Delete all term map for annotation
     */
    def deleteSuggestedTermFromAllUser(Annotation annotation, User currentUser) {
        //Delete all annotation term
        def suggestedterm = SuggestedTerm.findAllByAnnotation(annotation)
        log.info "Delete old suggestedterm= " + suggestedterm.size()

        suggestedterm.each { sugterm ->
            log.info "unlink sugterm:" + sugterm.id
            deleteSuggestedterm(sugterm.annotation.id, sugterm.term.id, sugterm.job.id, currentUser)
        }
    }

    /**
     * Delete all term map by user for term
     */
    def deleteSuggestedTermFromAllUser(Term term, User currentUser) {
        //Delete all annotation term
        def suggestedterm = SuggestedTerm.findAllByTerm(term)
        log.info "Delete old suggestedterm= " + suggestedterm.size()

        suggestedterm.each { sugterm ->
            log.info "unlink sugterm:" + sugterm.id
            deleteSuggestedterm(sugterm.annotation.id, sugterm.term.id, sugterm.job.id, currentUser)
        }
    }
}
