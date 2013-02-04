package be.cytomine.utils

import be.cytomine.Exception.InvalidRequestException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.command.Command
import grails.util.GrailsNameUtils
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.cfg.NotYetImplementedException

abstract class ModelService {

    static transactional = true

    def responseService
    def commandService
    def cytomineService
    def grailsApplication
    boolean saveOnUndoRedoStack

    /**
     * Save a domain on database, throw error if cannot save
     */
    def saveDomain(def newObject) {
        newObject.checkAlreadyExist()
        if (!newObject.validate()) {
            log.error newObject.errors
            log.error newObject.retrieveErrors().toString()
            throw new WrongArgumentException(newObject.retrieveErrors().toString())
        }
        if (!newObject.save(flush: true)) {
            throw new InvalidRequestException(newObject.retrieveErrors().toString())
        }
    }

    /**
     * Delete a domain from database
     */
    def deleteDomain(def oldObject) {
        try {
            oldObject.refresh()
            oldObject.delete(flush: true, failOnError: true)
        } catch (Exception e) {
            log.error e.toString()
            throw new InvalidRequestException(e.toString())
        }

    }

    /**
     * Add command info for the new domain concerned by the command
     * @param newObject New domain
     * @param message Message build for the command
     */
    protected def fillDomainWithData(def object, def json) {
        def domain = object.get(json.id)
        domain = object.insertDataIntoDomain(domain, json)
        domain.id = json.id
        return domain
    }

    /**
     * Get the name of the service (project,...)
     */
    public String getServiceName() {
        return GrailsNameUtils.getPropertyName(GrailsNameUtils.getShortName(this.getClass()))
    }

    /**
     * Execute command with JSON data
     */
    protected executeCommand(Command c, def json) {
        if(c instanceof DeleteCommand) {
            def domainToDelete = retrieve(json)

            //Create a backup (for 'undo' op)
            //We create before for deleteCommand to keep data from HasMany inside json (data will be deleted later)
            def backup = domainToDelete.encodeAsJSON()
            c.backup = backup

            //remove all dependent domains
            def allServiceMethods = this.metaClass.methods*.name
            allServiceMethods.each {
                if(it.startsWith("deleteDependent")) {
                    log.info("$it => transaction=${c.transaction}")
                    this."$it"(domainToDelete,c.transaction)
                }
            }
        }

        initCommandService()
        c.saveOnUndoRedoStack = this.isSaveOnUndoRedoStack() //need to use getter method, to get child value
        c.service = this
        c.serviceName = getServiceName()
        log.info "commandService=" + commandService + " c=" + c + " json=" + json
        return commandService.processCommand(c, json)
    }


    private void initCommandService() {
        if (!commandService) {
            commandService =grailsApplication.getMainContext().getBean("commandService")
        }

    }

    protected def retrieve(def json) {
        throw new NotYetImplementedException("The retrieve method must be implement in service "+ this.class)
    }


}
