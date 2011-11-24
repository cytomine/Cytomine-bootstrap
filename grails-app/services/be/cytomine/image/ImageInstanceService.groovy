package be.cytomine.image

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.imageinstance.AddImageInstanceCommand
import be.cytomine.command.imageinstance.DeleteImageInstanceCommand
import be.cytomine.command.imageinstance.EditImageInstanceCommand
import be.cytomine.ontology.Annotation
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON

class ImageInstanceService extends ModelService {

    static transactional = true

    def cytomineService
    def commandService
    def transactionService
    def annotationService
    def responseService

    def read(def id) {
        ImageInstance.read(id)
    }

    def read(Project project, AbstractImage image) {
        ImageInstance.findByBaseImageAndProject(image, project)
    }

    def get(def id) {
        ImageInstance.get(id)
    }

    def get(Project project, AbstractImage image) {
        ImageInstance.findByBaseImageAndProject(image, project)
    }

    def list() {
        ImageInstance.list()
    }

    def list(User user) {
        ImageInstance.findAllByUser(user)
    }

    def list(AbstractImage image) {
        ImageInstance.findAllByBaseImage(image)
    }

    def list(Project project) {
        def images = ImageInstance.createCriteria().list {
            createAlias("slide", "s")
            eq("project", project)
            order("slide")
            order("s.index", "asc")
        }
        return images
    }

    def add(def json) {
        User currentUser = cytomineService.getCurrentUser()
        synchronized (this.getClass()) {
            commandService.processCommand(new AddImageInstanceCommand(user: currentUser), json)
        }
    }

    def update(def json) {
        User currentUser = cytomineService.getCurrentUser()
        commandService.processCommand(new EditImageInstanceCommand(user: currentUser), json)
    }

    def delete(def json) {
        //Start transaction
        transactionService.start()
        User currentUser = cytomineService.getCurrentUser()
        //Read image
        Project project = Project.read(json.idproject)
        AbstractImage image = AbstractImage.read(json.idimage)
        ImageInstance imageInstance = ImageInstance.findByBaseImageAndProject(image, project)

        //Delete each annotation from image (if possible)
        if (imageInstance) {
            log.info "Delete annotation from image"
            def annotations = Annotation.findAllByImage(imageInstance)
            annotations.each { annotation ->
                annotationService.deleteAnnotation(annotation, currentUser, false)
            }
        }
        //Delete image
        log.info "Delete image"
        Long id = imageInstance?.id
        if (!imageInstance) throw new ObjectNotFoundException("Image Instance $json.idproject - $json.idimage not found")
        def jsonImage = JSON.parse("{id : $id}")
        def result = commandService.processCommand(new DeleteImageInstanceCommand(user: currentUser, printMessage: true), jsonImage)

        //Stop transaction
        transactionService.stop()

        return result
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def restore(def json, String commandType, boolean printMessage) {
        //Rebuilt object that was previoulsy deleted
        def domain = ImageInstance.createFromDataWithId(json)
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.baseImage?.filename, domain.project.name],printMessage,commandType)
        //Save new object
        domain.save(flush: true)
        return response
    }

    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def destroy(def json, String commandType, boolean printMessage) {
         //Get object to delete
        def domain = ImageInstance.get(json.id)
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.baseImage?.filename, domain.project.name],printMessage,commandType)
        //Delete object
        domain.delete(flush: true)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param commandType  command name (add/delete/...) which execute this method
     * @param printMessage  print message or not
     * @return response
     */
    def edit(def json, String commandType, boolean printMessage) {
         //Rebuilt previous state of object that was previoulsy edited
        def domain = fillDomainWithData(new ImageInstance(),json)
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.baseImage?.filename, domain.project.name],printMessage,commandType)
        //Save update
        domain.save(flush: true)
        return response
    }
}
