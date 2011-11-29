package be.cytomine.ontology

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class TermService extends ModelService {

    static transactional = true
    def springSecurityService
    def transactionService
    def commandService
    def cytomineService
    def annotationTermService
    def suggestedTermService
    def relationTermService
    def domainService

    boolean saveOnUndoRedoStack = true

    def list() {
        return Term.list()
    }

    Term read(def id) {
        return Term.read(id)
    }

    Term get(def id) {
        return Term.get(id)
    }

    def list(Ontology ontology) {
        return ontology?.leafTerms()
    }

    def list(Project project) {
        return project?.ontology?.terms()
    }

    def list(ImageInstance image) {
        return image?.terms()
    }


    def list(Annotation annotation, User user) {
        return AnnotationTerm.findAllByUserAndAnnotation(user, annotation).collect {it.term.id}
    }

    def statProject(Term term) {
        log.debug "term=" + term.name
        def projects = Project.findAllByOntology(term.ontology)

        log.debug "There are " + projects.size() + " projects for this ontology " + term.ontology.name
        def count = [:]
        def percentage = [:]

        //init list
        projects.each { project ->
            println "project=" + project.name
            count[project.name] = 0
            percentage[project.name] = 0
        }

        projects.each { project ->
            def annotations = project.annotations();
            annotations.each { annotation ->
                if (annotation.terms().contains(term)) {
                    count[project.name] = count[project.name] + 1;
                }
            }
        }

        //convert data map to list and merge term name and color
        return convertHashToList(count)
    }

    List convertHashToList(HashMap<String, Integer> map) {
        def list = []
        map.each {
            list << ["key": it.key, "value": it.value]
        }
        list
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def json) {
        User currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def json) throws CytomineException {
        User currentUser = cytomineService.getCurrentUser()
        return deleteTerm(json.id, currentUser)
    }

    def deleteTerm(def idTerm, User currentUser) throws CytomineException {
        return deleteTerm(idTerm, currentUser, true)
    }

    def deleteTerm(def idTerm, User currentUser, boolean printMessage) throws CytomineException {
        log.info "Delete term: " + idTerm
        Term term = Term.read(idTerm)
        if (term) {
            //Delete Annotation-Term before deleting Term
            annotationTermService.deleteAnnotationTermFromAllUser(term, currentUser)

            //Delete Suggested-Term before deleting Term
            suggestedTermService.deleteSuggestedTermFromAllUser(term, currentUser)

            //Delete relation-Term before deleting Term
            relationTermService.deleteRelationTermFromTerm(term, currentUser)
        }
        //Delete term
        def json = JSON.parse("{id : $idTerm}")
        return executeCommand(new DeleteCommand(user: currentUser, printMessage: printMessage), json)
    }

    def deleteTermRestricted(def idTerm, User currentUser, boolean printMessage) throws CytomineException {
        log.info "Delete term: " + idTerm
        Term term = Term.read(idTerm)
        if (term) {
            //Delete relation-Term before deleting Term
            relationTermService.deleteRelationTermFromTerm(term, currentUser)
        }
        //Delete term
        def json = JSON.parse("{id : $idTerm}")
        return executeCommand(new DeleteCommand(user: currentUser, printMessage: printMessage), json)
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(Term.createFromDataWithId(json), printMessage)
    }

    def create(Term domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name, domain.ontology?.name], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Term.get(json.id), printMessage)
    }

    def destroy(Term domain, boolean printMessage) {
        //Build response message
        if (!SuggestedTerm.findAllByTerm(domain).isEmpty()) throw new ConstraintException("Term " + domain.id + " has suggested term")
        if (!AnnotationTerm.findAllByTerm(domain).isEmpty()) throw new ConstraintException("Term " + domain.id + " has annotation term")
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name, domain.ontology?.name], printMessage, "Delete", domain.getCallBack())
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
        edit(fillDomainWithData(new Term(), json), printMessage)
    }

    def edit(Term domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name, domain.ontology?.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Term createFromJSON(def json) {
        return Term.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        Term term = Term.get(json.id)
        if (!term) throw new ObjectNotFoundException("Term " + json.id + " not found")
        return term
    }
}
