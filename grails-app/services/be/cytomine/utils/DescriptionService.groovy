package be.cytomine.utils

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.*
import be.cytomine.security.SecUser

import static org.springframework.security.acls.domain.BasePermission.READ

class DescriptionService extends ModelService {

    static transactional = true
    def springSecurityService
    def transactionService
    def commandService
    def cytomineService
    def securityACLService

    def currentDomain() {
        return Description
    }

    /**
     * List all description, Only for admin
     */
    def list() {
        securityACLService.checkAdmin(cytomineService.currentUser)
        return Description.list()
    }

    def get(def domain) {
        securityACLService.check(domain.container(),READ)
        Description.findByDomainIdentAndDomainClassName(domain.id,domain.class.name)
    }

    /**
     * Get a description thanks to its domain info (id and class)
     */
    def get(def domainIdent, def domainClassName) {
        securityACLService.check(domainIdent,domainClassName,READ)
        Description.findByDomainIdentAndDomainClassName(domainIdent,domainClassName)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        securityACLService.check(json.domainIdent,json.domainClassName,READ)
        securityACLService.checkReadOnly(json.domainIdent,json.domainClassName)
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
        securityACLService.check(description.container(),READ)
        securityACLService.checkReadOnly(description.container())
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
        securityACLService.check(domain.container(),READ)
        securityACLService.checkReadOnly(domain.container())
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
