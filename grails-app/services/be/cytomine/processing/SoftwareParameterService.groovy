package be.cytomine.processing

import be.cytomine.Exception.CytomineException
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

import static org.springframework.security.acls.domain.BasePermission.*

class SoftwareParameterService extends ModelService{

   static transactional = true

    def cytomineService
    def transactionService
    def modelService
    def jobParameterService

    def currentDomain() {
        return SoftwareParameter
    }

    def list() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        SoftwareParameter.list()
    }

    def read(def id) {
        def softParam = SoftwareParameter.read(id)
        //TODO: security?
        softParam
    }

    def list(Software software, Boolean includeSetByServer = false) {
        SecurityACL.check(software,READ)
        SoftwareParameter.findAllBySoftwareAndSetByServer(software, includeSetByServer)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
   def add(def json) throws CytomineException {
        SecurityACL.check(json.software,Software, READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(SoftwareParameter softwareParam, def jsonNewData) {
        SecurityACL.check(softwareParam.container(),WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser),softwareParam, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(SoftwareParameter domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain.container(),DELETE)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.name, domain.type, domain.software?.name]
    }


    def deleteDependentJobParameter(SoftwareParameter sp, Transaction transaction, Task task = null) {
        JobParameter.findAllBySoftwareParameter(sp).each {
            jobParameterService.delete(it,transaction,null,false)
        }
    }
}
