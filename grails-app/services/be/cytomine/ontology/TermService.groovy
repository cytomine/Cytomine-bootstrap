package be.cytomine.ontology

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import grails.converters.JSON
import groovy.sql.Sql
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.utils.Task

class TermService extends ModelService {

    static transactional = true
    def springSecurityService
    def transactionService
    def commandService
    def cytomineService
    def annotationTermService
    def algoAnnotationTermService
    def relationTermService
    def modelService

    def annotationFilterService
    def dataSource

    protected def secUserService

    def initialize() { this.secUserService = grailsApplication.mainContext.secUserService }


    /**
     * List all term, Only for admin
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list() {
        return Term.list()
    }

    Term read(def id) {
        def term = Term.read(id)
        if (term) {
            SecurityCheck.checkReadAuthorization(term.ontology)
        }
        term
    }

    Term get(def id) {
        def term = Term.get(id)
        if (term) {
            SecurityCheck.checkReadAuthorization(term.ontology)
        }
        term
    }

    @PreAuthorize("#ontology.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Ontology ontology) {
        return ontology?.leafTerms()
    }

    @PreAuthorize("#project.ontology.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        return project?.ontology?.terms()
    }

    @PreAuthorize("#annotation.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(UserAnnotation annotation, User user) {
        return AnnotationTerm.findAllByUserAndUserAnnotation(user, annotation).collect {it.term.id}
    }

    /**
     * Get all term id for a project
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    public List<Long> getAllTermId(Project project) {
        //better perf with sql request
        String request = "SELECT t.id FROM term t WHERE t.ontology_id="+project.ontology.id
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0]
        }
        return data
    }

    @PreAuthorize("#term.ontology.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def statProject(Term term) {
        def projects = Project.findAllByOntology(term.ontology)
        def count = [:]
        def percentage = [:]

        //init list
        projects.each { project ->
            count[project.name] = 0
            percentage[project.name] = 0
        }

        projects.each { project ->
            def annotations = UserAnnotation.createCriteria().list {
                eq("project", project)
                inList("user", secUserService.listLayers(project))
            }
            annotations.each { annotation ->
                if (annotation.terms().contains(term)) {
                    count[project.name] = count[project.name] + 1;
                }
            }
        }

        //convert data map to list and merge term name and color
        return convertHashToList(count)
    }

    private List convertHashToList(HashMap<String, Integer> map) {
        def list = []
        map.each {
            list << ["key": it.key, "value": it.value]
        }
        list
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkOntologyAccess(#json['ontology']) or hasRole('ROLE_ADMIN')")
    def add(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkOntologyWrite()  or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkOntologyDelete()  or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security) throws CytomineException {
        return delete(retrieve(json), transactionService.start())
    }

    def delete(Term term, Transaction transaction = null, boolean printMessage = true) {
        log.info "Delete term "
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${term.id}}")
        return executeCommand(new DeleteCommand(user: currentUser, transaction:transaction), json)
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(Term.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(Term domain, boolean printMessage) {
        //Save new object
        saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name, domain.ontology?.name], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Term.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(Term domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name, domain.ontology?.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        removeDomain(domain)
        return response
    }

    /**
     * Edit domain from database
     * @param json domain data in json
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Term(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(Term domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name, domain.ontology?.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        saveDomain(domain)
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

    def deleteDependentAlgoAnnotationTerm(Term term, Transaction transaction, Task task = null) {
        AlgoAnnotationTerm.findAllByTerm(term).each {
            println "${it.annotationIdent}-${it.term?.id}-${it.userJob?.id}"
            algoAnnotationTermService.delete(it,transaction,false)
        }
        AlgoAnnotationTerm.findAllByExpectedTerm(term).each {
            println "${it.annotationIdent}-${it.term?.id}-${it.userJob?.id}"
            algoAnnotationTermService.delete(it,transaction,false)
        }
    }

    def deleteDependentAnnotationTerm(Term term, Transaction transaction, Task task = null) {
        AnnotationTerm.findAllByTerm(term).each {
            annotationTermService.delete(it,transaction,false)
        }
    }

    def deleteDependentRelationTerm(Term term, Transaction transaction, Task task = null) {
        RelationTerm.findAllByTerm1(term).each {
            relationTermService.delete(it,transaction,false)
        }
        RelationTerm.findAllByTerm2(term).each {
            relationTermService.delete(it,transaction,false)
        }
    }

    def deleteDependentHasManyReviewedAnnotation(Term term, Transaction transaction, Task task = null) {
        def criteria = ReviewedAnnotation.createCriteria()
        def results = criteria.list {
            terms {
             inList("id", term.id)
            }
        }

        if(!results.isEmpty()) {
            throw new ConstraintException("Term is linked with validate annotation. Cannot delete term!")
        }
     }

    def deleteDependentHasManyAnnotationFilter(Term term, Transaction transaction, Task task = null) {
        def criteria = AnnotationFilter.createCriteria()
        def results = criteria.list {
          users {
             inList("id", term.id)
          }
        }
        results.each {
            it.removeFromTerms(term)
            it.save()
        }
     }

}
