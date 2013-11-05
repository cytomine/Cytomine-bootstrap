package be.cytomine.utils

import be.cytomine.CytomineDomain
import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.ontology.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import groovy.sql.Sql
import org.springframework.security.acls.model.Permission

import static org.springframework.security.acls.domain.BasePermission.*

class DescriptionService extends ModelService {

    static transactional = true
    def springSecurityService
    def transactionService
    def commandService
    def cytomineService

    def currentDomain() {
        return Description
    }

    /**
     * List all description, Only for admin
     */
    def list() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        return Description.list()
    }

    def get(def domain) {
        SecurityACL.check(domain.container(),READ)
        Description.findByDomainIdentAndDomainClassName(domain.id,domain.class.name)
    }

    /**
     * Get a description thanks to its domain info (id and class)
     */
    def get(def domainIdent, def domainClassName) {
        println "get=$domainIdent $domainClassName"
        SecurityACL.check(domainIdent,domainClassName,READ)
        Description.findByDomainIdentAndDomainClassName(domainIdent,domainClassName)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecurityACL.check(json.domainIdent,json.domainClassName,READ)
        SecurityACL.checkReadOnly(json.domainIdent,json.domainClassName)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Description description, def jsonNewData) {
        SecurityACL.check(description.container(),READ)
        SecurityACL.checkReadOnly(description.container())
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), description,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Description domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain.container(),READ)
        SecurityACL.checkReadOnly(domain.container())
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.domainIdent, domain.domainClassName]
    }


    def retrieve(Map json) {
        try {
            def domain = Class.forName(json.domainClassName, false, Thread.currentThread().contextClassLoader).read(json.domainIdent)
            def description
            if (domain) {
                description = get(domain)
            }
            if(!description) {
                throw new ObjectNotFoundException("Description not found for domain ${json.domainClassName} ${json.domainIdent}")
            } else {
                return description
            }
        }catch(Exception e) {
            throw new ObjectNotFoundException("Description not found for domain ${json.domainClassName} ${json.domainIdent}")
        }
    }
}
