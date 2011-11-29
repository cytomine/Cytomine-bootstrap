package be.cytomine

import grails.util.GrailsNameUtils
import be.cytomine.command.Command
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.ontology.Annotation

abstract class ModelService {

    static transactional = true

    def responseService
    def commandService

    boolean saveOnUndoRedoStack

    abstract def add(def json)

    abstract def update(def json)

    abstract def delete(def json)

//
//    abstract def restore(JSONObject json, String commandType, boolean printMessage)
//    abstract def restore(Annotation domain, String commandType, boolean printMessage)
//    abstract def destroy(JSONObject json, String commandType, boolean printMessage)
//    abstract def destroy(Annotation domain, String commandType, boolean printMessage)
//    abstract def edit(JSONObject json, String commandType, boolean printMessage)
//    abstract def edit(Annotation domain, String commandType, boolean printMessage)
//    abstract def retrieve(JSONObject json)


    protected def fillDomainWithData(def object, def json)
    {
        def domain = object.get(json.id)
        domain = object.getFromData(domain,json)
        domain.id = json.id
        return domain
    }

    public String getServiceName() {
        return GrailsNameUtils.getPropertyName(GrailsNameUtils.getShortName(this.getClass()))
    }

    void initCommandService() {
        if(!commandService) {
            log.info "initService:"+serviceName
            commandService = ApplicationHolder.application.getMainContext().getBean("commandService")
        }

    }

    protected executeCommand(Command c,def json) {
        initCommandService()
        c.saveOnUndoRedoStack = this.isSaveOnUndoRedoStack() //need to use getter method, to get child value
        c.service = this
        c.serviceName = getServiceName()
        log.info "commandService="+commandService + " c="+c + " json="+json
        return commandService.processCommand(c,json)
    }
}
