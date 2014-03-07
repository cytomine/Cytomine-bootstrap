package be.cytomine.laboratory

import be.cytomine.Exception.ConstraintException
import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.image.AbstractImage
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

class SampleService extends ModelService {

    static transactional = true

    def cytomineService
    def abstractImageService
    def transactionService

    def currentDomain() {
        return Sample
    }

    //@Secured(['ROLE_ADMIN'])
    def list() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        Sample.list()
    }

    //TODO:: secure ACL (from abstract image)
    def list(User user) {
        def abstractImageAvailable = abstractImageService.list(user)
        if(abstractImageAvailable.isEmpty()) {
            return []
        } else {
            AbstractImage.createCriteria().list {
                inList("id", abstractImageAvailable.collect{it.id})
                projections {
                    groupProperty('sample')
                }
            }
        }
    }

    //TODO:: secure ACL (if abstract image from sample is avaialbale for user)
    def read(def id) {
        Sample.read(id)
    }

    //TODO:: secure ACL (who can add/update/delete a sample?)
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkUser(currentUser)
        Command c = new AddCommand(user: currentUser)
        return executeCommand(c,null,json)
    }

    //TODO:: secure ACL (who can add/update/delete a sample?)
    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Sample sample, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkUser(currentUser)
        return executeCommand(new EditCommand(user: currentUser),sample,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Sample domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.checkUser(currentUser)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def deleteDependentAbstractImage(Sample sample, Transaction transaction, Task task = null) {
        AbstractImage.findAllBySample(sample).each {
            abstractImageService.delete(it,transaction,null,false)
        }
    }

    def deleteDependentSource(Sample sample, Transaction transaction, Task task = null) {
        //TODO: implement source cascade delete (first impl source command delete)
        if(Source.findAllBySample(sample)) {
            throw new ConstraintException("Sample has source. Cannot delete sample!")
        }
    }

}
