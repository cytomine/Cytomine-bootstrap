package be.cytomine.image.multidim

import be.cytomine.SecurityACL
import be.cytomine.command.*
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.utils.ModelService
import be.cytomine.utils.Task

import static org.springframework.security.acls.domain.BasePermission.READ

class ImageGroupService extends ModelService {

    static transactional = true

    def cytomineService
    def transactionService
    def userAnnotationService
    def algoAnnotationService
    def dataSource
    def reviewedAnnotationService
    def imageSequenceService

    def currentDomain() {
        return ImageGroup
    }

    def read(def id) {
        def image = ImageGroup.read(id)
        if(image) {
            SecurityACL.check(image.container(),READ)
        }
        image
    }

    def list(Project project) {
        SecurityACL.check(project,READ)
        return ImageGroup.findAllByProject(project)
    }


    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecurityACL.check(json.project,Project,READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        json.user = currentUser.id
        synchronized (this.getClass()) {
            Command c = new AddCommand(user: currentUser)
            executeCommand(c,null,json)
        }
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(ImageGroup domain, def jsonNewData) {
        SecurityACL.check(domain.container(),READ)
        SecurityACL.check(jsonNewData.project,Project,READ)

        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new EditCommand(user: currentUser)
        executeCommand(c,domain,jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(ImageGroup domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecurityACL.check(domain.container(),READ)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id,  domain.name, domain.project.name]
    }

    def deleteDependentImageSequence(ImageGroup group, Transaction transaction, Task task = null) {
        ImageSequence.findAllByImageGroup(group).each {
            imageSequenceService.delete(it,transaction,null,false)
        }
    }
}
