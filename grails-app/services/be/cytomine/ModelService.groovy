package be.cytomine

import be.cytomine.command.Command
import grails.util.GrailsNameUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.project.Project

abstract class ModelService {

    static transactional = true

    def responseService
    def commandService
    def cytomineService

    boolean saveOnUndoRedoStack

    abstract def add(def json)

    abstract def update(def domain,def json)

    abstract def delete(def domain,def json)

//
//    abstract def create(JSONObject json, boolean printMessage)
//    abstract def create(Annotation domain, boolean printMessage)
//    abstract def destroy(JSONObject json, boolean printMessage)
//    abstract def destroy(Annotation domain, boolean printMessage)
//    abstract def edit(JSONObject json, boolean printMessage)
//    abstract def edit(Annotation domain, boolean printMessage)
//    abstract def retrieve(JSONObject json)


    protected def fillDomainWithData(def object, def json) {
        def domain = object.get(json.id)
        domain = object.getFromData(domain, json)
        domain.id = json.id
        return domain
    }

    public String getServiceName() {
        return GrailsNameUtils.getPropertyName(GrailsNameUtils.getShortName(this.getClass()))
    }

    void initCommandService() {
        if (!commandService) {
            log.info "initService:" + serviceName
            commandService = ApplicationHolder.application.getMainContext().getBean("commandService")
        }

    }

    protected executeCommand(Command c, def json) {
        initCommandService()
        c.saveOnUndoRedoStack = this.isSaveOnUndoRedoStack() //need to use getter method, to get child value
        c.service = this
        c.serviceName = getServiceName()
        log.info "commandService=" + commandService + " c=" + c + " json=" + json
        return commandService.processCommand(c, json)
    }

    @PreAuthorize("hasPermission(#id ,'be.cytomine.project.Project',read) or hasPermission(#id ,'be.cytomine.project.Project',admin) or hasRole('ROLE_ADMIN')")
    void checkAuthorization(def id) {
    }
}
