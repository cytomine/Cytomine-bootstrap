package be.cytomine.ontology

import be.cytomine.Exception.ConstraintException
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import groovy.sql.Sql

import static org.springframework.security.acls.domain.BasePermission.*

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

    def dataSource

    protected def secUserService

    def initialize() { this.secUserService = grailsApplication.mainContext.secUserService }

    def currentDomain() {
        return Term
    }

    /**
     * List all term, Only for admin
     */
    def list() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        return Term.list()
    }

    Term read(def id) {
        def term = Term.read(id)
        if (term) {
            SecurityACL.check(term.container(),READ)
        }
        term
    }

    Term get(def id) {
        def term = Term.get(id)
        if (term) {
            SecurityACL.check(term.container(),READ)
        }
        term
    }

    def list(Ontology ontology) {
        SecurityACL.check(ontology.container(),READ)
        return ontology?.leafTerms()
    }

    def list(Project project) {
        SecurityACL.check(project,READ)
        return project?.ontology?.terms()
    }

    def list(UserAnnotation annotation, User user) {
        SecurityACL.check(annotation.container(),READ)
        return AnnotationTerm.findAllByUserAndUserAnnotation(user, annotation).collect {it.term.id}
    }

    /**
     * Get all term id for a project
     */
    public List<Long> getAllTermId(Project project) {
        SecurityACL.check(project.container(),READ)
        //better perf with sql request
        String request = "SELECT t.id FROM term t WHERE t.ontology_id="+project.ontology.id
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0]
        }
        return data
    }

    def statProject(Term term) {
        SecurityACL.check(term.container(),READ)
        def projects = Project.findAllByOntology(term.ontology)
        def count = [:]
        def percentage = [:]

        //init list
        projects.each { project ->
            count[project.name] = 0
            percentage[project.name] = 0
        }

        projects.each { project ->
            def layers = secUserService.listLayers(project)
            if(!layers.isEmpty()) {
                def annotations = UserAnnotation.createCriteria().list {
                    eq("project", project)
                    inList("user", layers)
                }
                annotations.each { annotation ->
                    if (annotation.terms().contains(term)) {
                        count[project.name] = count[project.name] + 1;
                    }
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
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecurityACL.check(json.ontology, Ontology,WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Term term, def jsonNewData) {
        SecurityACL.check(term.container(),WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), term,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Term domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain.container(),DELETE)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
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
        def nbreUserAnnotation = AnnotationTerm.countByTerm(term)

        if (nbreUserAnnotation>0) {
            throw new ConstraintException("Term is still linked with ${nbreUserAnnotation} annotations created by user. Cannot delete term!")
        }
    }

    def deleteDependentRelationTerm(Term term, Transaction transaction, Task task = null) {
        RelationTerm.findAllByTerm1(term).each {
            relationTermService.delete(it,transaction,null,false)
        }
        RelationTerm.findAllByTerm2(term).each {
            relationTermService.delete(it,transaction,null,false)
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
