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

    def currentDomain() {
        return Term
    }

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
    def delete(def json, SecurityCheck security, Task task = null) throws CytomineException {
        return delete(retrieve(json), transactionService.start())
    }

    def delete(Term term, Transaction transaction = null, boolean printMessage = true) {
        log.info "Delete term "
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${term.id}}")
        return executeCommand(new DeleteCommand(user: currentUser, transaction:transaction), json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def deleteDependentAlgoAnnotationTerm(Term term, Transaction transaction, Task task = null) {
        def nbreAlgoAnnotation = AlgoAnnotationTerm.countByTermOrExpectedTerm(term,term)

        if (nbreAlgoAnnotation>0) {
            throw new ConstraintException("Term is still linked with ${nbreAlgoAnnotation} annotations created by job. Cannot delete term!")
        }
    }

    def deleteDependentAnnotationTerm(Term term, Transaction transaction, Task task = null) {
        def nbreUserAnnotation = AnnotationTerm.countByTerm(term,term)

        if (nbreUserAnnotation>0) {
            throw new ConstraintException("Term is still linked with ${nbreUserAnnotation} annotations created by user. Cannot delete term!")
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
            throw new ConstraintException("Term is linked with ${results.size()} validate annotations. Cannot delete term!")
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
