package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.image.AbstractImage
import be.cytomine.utils.ModelService
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.command.Transaction
import be.cytomine.image.AbstractImageGroup
import be.cytomine.utils.Task

class GroupService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def abstractImageGroupService
    def userGroupService
    def transactionService

    def currentDomain() {
        Group
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def list() {
        return Group.list(sort: "name", order: "asc")
    }

    //TODO:: security!
    def list(AbstractImage abstractImage) {
        return AbstractImageGroup.findAllByAbstractImage(abstractImage).collect{it.group}
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def list(User user) {
        UserGroup.findByUser(user).collect{it.group}
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def read(def id) {
        return Group.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def get(def id) {
        return Group.get(id)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json,SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkIfUserIsMemberGroup(principal.id) or hasRole('ROLE_ADMIN')")
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) {
        return delete(retrieve(json), transactionService.start())
    }

    def delete(Group group, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${group.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }


    def deleteDependentAbstractImageGroup(Group group, Transaction transaction, Task task = null) {
        AbstractImageGroup.findAllByGroup(group).each {
            abstractImageGroupService.delete(it,transaction,false)
        }
    }

    def deleteDependentUserGroup(Group group, Transaction transaction, Task task = null) {
        UserGroup.findAllByGroup(group).each {
            userGroupService.delete(it, transaction, false)
        }
    }

}
