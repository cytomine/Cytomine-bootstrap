package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.processing.Job
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class SuggestedTermService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def domainService

    boolean saveOnUndoRedoStack = true

    def list() {
        SuggestedTerm.list()
    }

    def list(Annotation annotation) {
        SuggestedTerm.findAllByAnnotation(annotation)
    }

    def read(Annotation annotation, Term term, Job job) {
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
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def delete(def json) {
        User currentUser = cytomineService.getCurrentUser()
        def result = deleteSuggestedTerm(json.idannotation, json.idterm, json.idjob, currentUser)
        return result
    }

    def update(def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Delete an annotation term
     */
    def deleteSuggestedTerm(def idAnnotation, def idTerm, def idJob, User currentUser) {
        def json = JSON.parse("{annotation: $idAnnotation, term: $idTerm, job: $idJob}")
        return executeCommand(new DeleteCommand(user: currentUser), json)
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

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def restore(JSONObject json, boolean printMessage) {
        restore(SuggestedTerm.createFromDataWithId(json),printMessage)
    }
    def restore(SuggestedTerm domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.term.name, domain.annotation.id, domain.job?.software?.name],printMessage,"Add",domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
         destroy(SuggestedTerm.get(json.id),printMessage)
    }
    def destroy(SuggestedTerm domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.term.name, domain.annotation.id, domain.job?.software?.name],printMessage,"Delete",domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage  print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new SuggestedTerm(),json),printMessage)
    }
    def edit(SuggestedTerm domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.term.name, domain.annotation.id, domain.job?.software?.name],printMessage,"Edit",domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    SuggestedTerm createFromJSON(def json) {
       return SuggestedTerm.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        //Retrieve domain
        Annotation annotation = Annotation.read(json.annotation)
        Term term = Term.read(json.term)
        Job job = Job.read(json.job)
        SuggestedTerm domain = SuggestedTerm.findWhere(annotation: annotation, term: term, job: job)
        if(!domain) throw new ObjectNotFoundException("SuggestedTerm was not found with annotation:$annotation,term:$term,job:$job")
        return domain
    }
}
