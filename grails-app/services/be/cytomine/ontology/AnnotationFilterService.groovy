package be.cytomine.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.command.Transaction
import grails.converters.JSON
import static org.springframework.security.acls.domain.BasePermission.*

class AnnotationFilterService extends ModelService {

    static transactional = true

    def cytomineService
    def transactionService

    def currentDomain() {
        return AnnotationFilter
    }

    def listByProject(Project project) {
        SecurityACL.check(project,READ)
        return AnnotationFilter.findAllByProject(project)
    }

    AnnotationFilter read(def id) {
        def filter = AnnotationFilter.read(id)
        if (filter) {
            SecurityACL.check(filter.projectDomain(),READ)
        }
        filter
    }

    AnnotationFilter get(def id) {
        def filter = AnnotationFilter.get(id)
        if (filter) {
            SecurityACL.check(filter.projectDomain(),READ)
        }
        filter
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        SecurityACL.check(json.project,Project,READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(be.cytomine.ontology.AnnotationFilter af, def jsonNewData) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.isCreator(af,currentUser)
        return executeCommand(new EditCommand(user: currentUser), af, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(AnnotationFilter domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.isCreator(domain,currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id]
    }

    /**
     * Delete all annotations filters which contains a Term instance
     * @param term
     */
    def deleteFiltersFromTerm(Term term) {
        def annotationsFilters = AnnotationFilter.createCriteria().list {
            terms {
                idEq(term.id)
            }
        }
        annotationsFilters.each {
            destroy(it, true)
        }
    }
//
//
//    def deleteDependentHasManyTerms(AnnotationFilter project, Transaction transaction) {
//        project.terms?.clear()
//    }
//
//    def deleteDependentHasManySecUser(AnnotationFilter project, Transaction transaction) {
//        project
//    }



//        def deleteDependentHasManyTerms(AnnotationFilter project, Transaction transaction) {
//            //remove Retrieval-project where this project is set
//           def criteria = Project.createCriteria()
//            List<Project> projectsThatUseThisProjectForRetrieval = criteria.list {
//              retrievalProjects {
//                  eq('id', project.id)
//              }
//            }
//
//            projectsThatUseThisProjectForRetrieval.each {
//                it.refresh()
//                it.removeFromRetrievalProjects(project)
//                it.save(flush: true)
//            }
//
//
//            project.retrievalProjects?.clear()
//    }

}
