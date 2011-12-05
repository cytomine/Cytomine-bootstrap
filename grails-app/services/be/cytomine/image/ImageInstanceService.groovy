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
            createAlias("baseImage", "i")
            eq("project", project)
            order("i.created", "desc")
        }
        return images
    }
//    def list(Project project) {
//        def images = ImageInstance.createCriteria().list {
//            createAlias("slide", "s")
//            eq("project", project)
//            order("slide")
//            order("s.index", "asc")
//        }
//        return images
//    }

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

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(ImageInstance.createFromDataWithId(json), printMessage)
    }

    def create(ImageInstance domain, boolean printMessage) {
        if (ImageInstance.findByBaseImageAndProject(domain.baseImage, domain.project))
            throw new WrongArgumentException("Image " + domain?.baseImage?.filename + " already map with project " + domain.project.name)
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.baseImage?.filename, domain.project.name], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(ImageInstance.get(json.id), printMessage)
    }

    def destroy(ImageInstance domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.baseImage?.filename, domain.project.name], printMessage, "Delete", domain.getCallBack())
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
        edit(fillDomainWithData(new ImageInstance(), json), printMessage)
    }

    def edit(ImageInstance domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.baseImage?.filename, domain.project.name], printMessage, "Edit", domain.getCallBack())
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
        if (!image) throw new ObjectNotFoundException("ImageInstance " + json.id + " not found")
        return image
    }
}
