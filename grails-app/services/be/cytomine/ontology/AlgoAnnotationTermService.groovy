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
import be.cytomine.command.Transaction
import be.cytomine.security.UserJob
import be.cytomine.security.SecUser

class AlgoAnnotationTermService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def domainService

    boolean saveOnUndoRedoStack = true

    def list() {
        AlgoAnnotationTerm.list()
    }

    def list(Annotation annotation) {
        AlgoAnnotationTerm.findAllByAnnotation(annotation)
    }

    def read(Annotation annotation, Term term, UserJob userJob) {
        AlgoAnnotationTerm.findWhere(annotation: annotation, term: term, userJob: userJob)
    }

    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def delete(def domain,def json) {
        User currentUser = cytomineService.getCurrentUser()
        def result = deleteAlgoAnnotationTerm(json.annotation, json.term, json.userJob, currentUser,null)
        return result
    }

    def update(def domain,def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Delete an annotation term
     */
    def deleteAlgoAnnotationTerm(def idAnnotation, def idTerm, def idUserJob, User currentUser, Transaction transaction) {
        def json = JSON.parse("{annotation: $idAnnotation, term: $idTerm, userJob: $idUserJob}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    /**
     * Delete all term map for annotation
     */
    def deleteAlgoAnnotationTermFromAllUser(Annotation annotation, User currentUser, Transaction transaction) {
        //Delete all annotation term
        def suggestedterm = AlgoAnnotationTerm.findAllByAnnotation(annotation)
        log.info "Delete old suggestedterm= " + suggestedterm.size()

        suggestedterm.each { sugterm ->
            log.info "unlink sugterm:" + sugterm.id
            deleteAlgoAnnotationTerm(sugterm.annotation.id, sugterm.term.id, sugterm.userJob.id, currentUser,transaction)
        }
    }

    /**
     * Delete all term map by user for term
     */
    def deleteAlgoAnnotationTermFromAllUser(Term term, User currentUser,Transaction transaction) {
        //Delete all annotation term
        def algoannotationterm = AlgoAnnotationTerm.findAllByTerm(term)
        log.info "Delete old algoannotationterm= " + algoannotationterm.size()

        algoannotationterm.each { algoterm ->
            log.info "unlink sugterm:" + algoterm.id
            deleteAlgoAnnotationTerm(algoterm.annotation.id, algoterm.term.id, algoterm.userJob.id, currentUser,transaction)
        }
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(AlgoAnnotationTerm.createFromDataWithId(json), printMessage)
    }

    def create(AlgoAnnotationTerm domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.term.name, domain.annotation.id, domain.userJob], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(AlgoAnnotationTerm.get(json.id), printMessage)
    }

    def destroy(AlgoAnnotationTerm domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.term.name, domain.annotation.id, domain.userJob], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new AlgoAnnotationTerm(), json), printMessage)
    }

    def edit(AlgoAnnotationTerm domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.term.name, domain.annotation.id, domain.userJob], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    AlgoAnnotationTerm createFromJSON(def json) {
        return AlgoAnnotationTerm.createFromData(json)
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
        UserJob userJob = UserJob.read(json.userJob)
        AlgoAnnotationTerm domain = AlgoAnnotationTerm.findWhere(annotation: annotation, term: term, userJob: userJob)
        if (!domain) throw new ObjectNotFoundException("SuggestedTerm was not found with annotation:$annotation,term:$term,userJob:$userJob")
        return domain
    }
}
