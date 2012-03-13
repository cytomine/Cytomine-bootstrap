package be.cytomine.processing

import be.cytomine.project.Project
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.ModelService
import grails.plugins.springsecurity.Secured
import be.cytomine.command.AddCommand
import be.cytomine.security.SecUser
import be.cytomine.Exception.CytomineException
import be.cytomine.command.EditCommand
import be.cytomine.command.DeleteCommand
import org.codehaus.groovy.grails.web.json.JSONObject
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.Exception.ObjectNotFoundException


class ImageFilterProjectService extends ModelService {

    static transactional = true
    def aclPermissionFactory
    def aclService
    def aclUtilService
    def springSecurityService
    def imageFilterService
    def projectService

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def get(Project project, ImageFilter image) {
        return ImageFilterProject.findByImageFilterAndProject(image, project)
    }

    @Secured(['ROLE_ADMIN', 'ROLE_USER'])
    def list() {
        return ImageFilterProject.list()
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        return ImageFilterProject.findAllByProject(project)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def add(Project project, ImageFilter imageFilter) {
        ImageFilterProject.link(imageFilter, project)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def delete(Project project, ImageFilter imageFilter) {
        ImageFilterProject.unlink(imageFilter, project)
    }


    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def update(def domain,def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser), json)
    }

    def delete(def domain,def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new DeleteCommand(user: currentUser), json)
    }


    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(ImageFilterProject.createFromDataWithId(json), printMessage)
    }

    def create(ImageFilterProject domain, boolean printMessage) {
        if(ImageFilterProject.findByImageFilterAndProject(domain.imageFilter,domain.project)) throw new WrongArgumentException("ImageFilter  "+domain.imageFilter?.name + " already map with project "+domain.project?.name)
        //Save new object
        domain = ImageFilterProject.link(domain.id, domain.imageFilter,domain.project)

        //Build response message
        return responseService.createResponseMessage(domain, [domain.imageFilter?.name, domain.project?.name], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        log.info "JSON="+json
        destroy(ImageFilterProject.get(json.id), printMessage)
    }

    def destroy(ImageFilterProject domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,  [domain.imageFilter?.name, domain.project?.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        ImageFilterProject.unlink(domain.imageFilter,domain.project)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    ImageFilterProject createFromJSON(def json) {
        return ImageFilterProject.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        ImageFilterProject parameter = ImageFilterProject.get(json.id)
        if (!parameter) throw new ObjectNotFoundException("ImageFilterProject " + json.id + " not found")
        return parameter
    }

}