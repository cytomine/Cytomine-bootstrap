package be.cytomine.image

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.ontology.Annotation
import be.cytomine.project.Project
import be.cytomine.security.User
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class ImageInstanceService extends ModelService {

    static transactional = true

    def cytomineService
    def commandService
    def transactionService
    def annotationService
    def responseService
    def domainService

    boolean saveOnUndoRedoStack = true

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
            executeCommand(new AddCommand(user: currentUser), json)
        }
    }

    def update(def json) {
        User currentUser = cytomineService.getCurrentUser()
        executeCommand(new EditCommand(user: currentUser), json)
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
        def result = executeCommand(new DeleteCommand(user: currentUser), jsonImage)

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
    def restore(JSONObject json, String commandType, boolean printMessage) {
        restore(ImageInstance.createFromDataWithId(json),commandType,printMessage)
    }
    def restore(ImageInstance domain, String commandType, boolean printMessage) {
        if (ImageInstance.findByBaseImageAndProject(domain.baseImage, domain.project))
            throw new WrongArgumentException("Image " + domain?.baseImage?.filename + " already map with project " + domain.project.name)
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain,[domain.id, domain.baseImage?.filename, domain.project.name],printMessage,commandType,domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info
     * @param commandType command name (add/delete/...) which execute this method
     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, String commandType, boolean printMessage) {
        //Get object to delete
         destroy(ImageInstance.get(json.id),commandType,printMessage)
    }
    def destroy(ImageInstance domain, String commandType, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.baseImage?.filename, domain.project.name],printMessage,commandType,domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param commandType  command name (add/delete/...) which execute this method
     * @param printMessage  print message or not
     * @return response
     */
    def edit(JSONObject json, String commandType, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new ImageInstance(),json),commandType,printMessage)
    }
    def edit(ImageInstance domain, String commandType, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain,[domain.id, domain.baseImage?.filename, domain.project.name],printMessage,commandType,domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    ImageInstance createFromJSON(def json) {
       return ImageInstance.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        ImageInstance image = ImageInstance.get(json.id)
        if(!image) throw new ObjectNotFoundException("ImageInstance " + json.id + " not found")
        return image
    }
}
