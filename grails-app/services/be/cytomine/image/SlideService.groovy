package be.cytomine.image

import be.cytomine.ModelService
import be.cytomine.project.Slide
import be.cytomine.security.User
import grails.orm.PagedResultList
import be.cytomine.command.AddCommand
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.Exception.ObjectNotFoundException
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.command.EditCommand
import be.cytomine.security.SecUser
import be.cytomine.command.DeleteCommand

class SlideService extends ModelService {

    static transactional = true

    def domainService
    def cytomineService

    def list() {
        Slide.list()
    }

    def list(User user) {
        user.slides()
    }

    PagedResultList list(User user, def page, def limit, def sortedRow, def sord) {
        def data = [:]
        log.info "page=" + page + " limit=" + limit + " sortedRow=" + sortedRow + " sord=" + sord
        int pg = Integer.parseInt(page) - 1
        int max = Integer.parseInt(limit)
        int offset = pg * max
        PagedResultList results = user.slides(max, offset, sortedRow, sord)

        data.page = pg + ""
        data.records = results.totalCount
        data.total = Math.ceil(results.totalCount / max) + "" //[100/10 => 10 page]
        data.rows = results.list
        return data
    }

    def read(def id) {
        Slide.read(id)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def update(def domain,def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def delete(def domain,def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }
    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    Slide createFromJSON(def json) {
        return Slide.createFromData(json)
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(Slide.createFromDataWithId(json), printMessage)
    }

    def create(Slide domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.name, domain.index], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(Slide.get(json.id), printMessage)
    }

    def destroy(Slide domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,  [domain.id, domain.name, domain.index], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new Slide(), json), printMessage)
    }

    def edit(Slide domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,  [domain.id, domain.name, domain.index], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }


    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        Slide slide
        slide = Slide.read(json.id)
        if (!slide) throw new ObjectNotFoundException("Slide " + json.id + " not found")
        return slide
    }
}
