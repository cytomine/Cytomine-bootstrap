package be.cytomine.laboratory

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.image.AbstractImage
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.utils.ModelService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize

import be.cytomine.command.Transaction

import grails.converters.JSON
import be.cytomine.Exception.ConstraintException
import be.cytomine.utils.Task

class SampleService extends ModelService {

    static transactional = true

    def cytomineService
    def abstractImageService
    def transactionService

    def currentDomain() {
        return Sample
    }

    @Secured(['ROLE_ADMIN'])
    def list() {
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
    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json,SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    //TODO:: secure ACL (who can add/update/delete a sample?)
    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    def update(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    //TODO:: secure ACL (who can add/update/delete a sample?)
    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    def delete(def json, SecurityCheck security, Task task = null) {
        return delete(retrieve(json),transactionService.start())
    }

    def delete(Sample sample, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${sample.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def deleteDependentAbstractImage(Sample sample, Transaction transaction, Task task = null) {
        AbstractImage.findAllBySample(sample).each {
            abstractImageService.delete(it,transaction,false)
        }
    }

    def deleteDependentSource(Sample sample, Transaction transaction, Task task = null) {
        //TODO: implement source cascade delete (first impl source command delete)
        if(Source.findAllBySample(sample)) {
            throw new ConstraintException("Sample has source. Cannot delete sample!")
        }
    }

}
