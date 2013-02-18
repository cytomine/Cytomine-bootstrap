package be.cytomine.processing

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.command.Transaction
import grails.converters.JSON

class SoftwareProjectService extends ModelService{

   static transactional = true

    def cytomineService
    def transactionService
    def modelService

    def currentDomain() {
        return SoftwareProject
    }

    def read(def id) {
        def sp = SoftwareProject.get(id)
        if(sp) {
            SecurityCheck.checkReadAuthorization(sp.project)
        }
        sp
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def list() {
        SoftwareProject.list()
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        SoftwareProject.findAllByProject(project)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess(#json['project']) or hasRole('ROLE_ADMIN')")
   def add(def json,SecurityCheck security) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) throws CytomineException {
        return delete(retrieve(json),transactionService.start())
    }


    def delete(SoftwareProject sp, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${sp.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }


    def getStringParamsI18n(def domain) {
        return [domain.software?.name, domain.project?.name]
    }
}
