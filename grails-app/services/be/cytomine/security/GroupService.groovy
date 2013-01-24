package be.cytomine.security

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.utils.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.image.AbstractImage
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.SecurityCheck
import org.springframework.security.access.prepost.PreAuthorize
import grails.converters.JSON

class GroupService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def modelService

    @PreAuthorize("hasRole('ROLE_USER')")
    def list() {
        return Group.list(sort: "name", order: "asc")
    }

    //TODO:: security!
    def list(AbstractImage abstractimage) {
        return abstractimage.abstractimagegroup.collect {
            it.group
        }
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
    def delete(def json, SecurityCheck security) {
        println "delete=$json"
        println cytomineService.getCurrentUser().authorities.collect {it.authority}
        SecUser currentUser = cytomineService.getCurrentUser()
        return deleteGroup(Group.read(json.id), currentUser)
    }

    def deleteGroup(Group group, SecUser currentUser) {

        UserGroup.findAllByGroup(group).each {
            it.delete()
        }

        def json = JSON.parse("{id: $group.id}")
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }


    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(Group.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(Group domain, boolean printMessage) {
        //Save new object
        saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Group.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(Group domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        deleteDomain(domain)
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
        edit(fillDomainWithData(new Group(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(Group domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.name], printMessage, "Edit", domain.getCallBack())
        //Save update
        saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Group createFromJSON(def json) {
        return Group.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        Group group = Group.get(json.id)
        if (!group) {
            throw new ObjectNotFoundException("Group " + json.id + " not found")
        }
        return group
    }

}
