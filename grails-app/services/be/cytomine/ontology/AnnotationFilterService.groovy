package be.cytomine.ontology

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
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

class AnnotationFilterService extends ModelService {

    static transactional = true

    def cytomineService
    def transactionService

    def currentDomain() {
        return AnnotationFilter
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listByProject(Project project) {
        return AnnotationFilter.findAllByProject(project)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    AnnotationFilter read(def id) {
        def filter = AnnotationFilter.read(id)
        if (filter) {
            SecurityCheck.checkReadAuthorization(filter.project)
        }
        filter
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    AnnotationFilter get(def id) {
        def filter = AnnotationFilter.get(id)
        if (filter) {
            SecurityCheck.checkReadAuthorization(filter.project)
        }
        filter
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess(#json['project']) or hasRole('ROLE_ADMIN')")
    def add(def json, SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkCurrentUserCreator(principal.id) or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkCurrentUserCreator(principal.id) or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) throws CytomineException {
        return delete(retrieve(json),transactionService.start())
    }


    def delete(AnnotationFilter af, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${af.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
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
