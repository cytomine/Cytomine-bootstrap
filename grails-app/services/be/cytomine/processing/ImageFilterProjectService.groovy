package be.cytomine.processing

import be.cytomine.SecurityACL
import be.cytomine.command.Command
import be.cytomine.utils.Task
import grails.plugins.springsecurity.Secured

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.command.Transaction
import grails.converters.JSON
import static org.springframework.security.acls.domain.BasePermission.*

class ImageFilterProjectService extends ModelService {

    static transactional = true
    def springSecurityService

    def currentDomain() {
        return ImageFilterProject
    }

    def list() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        return ImageFilterProject.list()
    }

    def get(Project project, ImageFilter image) {
        SecurityACL.check(project,READ)
        return ImageFilterProject.findByImageFilterAndProject(image, project)
    }

    def list(Project project) {
        SecurityACL.check(project,READ)
        return ImageFilterProject.findAllByProject(project)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        SecurityACL.check(json.project, Project,READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(ImageFilterProject domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecurityACL.check(domain.projectDomain(),READ)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.imageFilter?.name, domain.project?.name]
    }
}