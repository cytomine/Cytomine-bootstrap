package be.cytomine.search

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.command.AddCommand
import be.cytomine.command.Command
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import grails.transaction.Transactional

import static be.cytomine.search.SearchEngineFilter.*

@Transactional
class SearchEngineFilterService extends ModelService {

    static transactional = true
    boolean saveOnUndoRedoStack = true

    def springSecurityService
    def transactionService
    def securityACLService

    def currentDomain() {
        return SearchEngineFilter
    }

    SearchEngineFilter read(def id) {
        def filter = SearchEngineFilter.read(id)
        /*if (filter) {
            securityACLService.check(filter,READ)
        }*/
        filter
    }

    /**
     * Get all filters of the current user
     * @return SearchEngineFilter list
     */
    def list() {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkUser(currentUser)
        return SearchEngineFilter.findAllByUser(currentUser)
    }


    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkUser(currentUser)
        json.user = currentUser.id
        checkJsonConsistency(json)
        return executeCommand(new AddCommand(user: currentUser), null,json)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(SearchEngineFilter domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        //securityACLService.check(domain,DELETE)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null, task)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    /**
     * Check if the params json are ok
     * I.e. if selected projects exists and if some words are filled for the search
     * @param json SearchEngineFilter json
     */
    private void checkJsonConsistency(def json) {

        if (!json.projects.equals("null") && !json.projects.equals("[]")) {
            for (def projectId in json.projects) {
                def project = Project.findById(projectId);
                if (project == null) {
                    throw new WrongArgumentException("SearchEngineFilter cannot have non-existing projects")
                }
            }
        }

        if (json.words.equals("null") || json.words.equals("[]")) {
            throw new WrongArgumentException("SearchEngineFilter cannot search with no words")
        }
    }

}
