package be.cytomine.image

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.social.FollowRequest
import be.cytomine.social.UserPosition
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.FetchMode
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.Exception.ForbiddenException

class ImageInstanceService extends ModelService {

    boolean saveOnUndoRedoStack = true
    static transactional = true

    def cytomineService
    def commandService
    def transactionService
    def responseService
    def domainService
    def userAnnotationService
    def algoAnnotationService
    def dataSource

    def read(def id) {
        def image = ImageInstance.read(id)
        if(image) {
            image.project.checkReadPermission()
        }
        image
    }

    def get(def id) {
        def image = ImageInstance.get(id)
        if(image) {
            image.project.checkReadPermission()
        }
        image
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project) {
        def images = ImageInstance.createCriteria().list {
            createAlias("baseImage", "i")
            eq("project", project)
            order("i.created", "desc")
            fetchMode 'baseImage', FetchMode.JOIN
        }
        return images
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listTree(Project project) {
        def children = []
        list(project).each { image->
            children << [ id : image.id, key : image.id, title : image.baseImage.originalFilename, isFolder : false, children : []]
        }
        def tree = [:]
        tree.isFolder = true
        tree.hideCheckbox = true
        tree.name = project.getName()
        tree.title = project.getName();
        tree.key = project.getId()
        tree.id = project.getId()
        tree.children = children
        return tree
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(Project project, int inf, int sup) {
        def images = ImageInstance.createCriteria().list(max:sup-inf, offset:inf) {
            createAlias("baseImage", "i")
            eq("project", project)
            order("i.created", "desc")
            fetchMode 'baseImage', FetchMode.JOIN
        }
        return images
    }

    void checkProjectAccess(def id) {
        def project = Project.read(id)
        if(!project) {
            throw new ObjectNotFoundException("Project $id was not found! Unable to process project auth checking")
        } else {
            project.checkReadPermission()
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json) {
        checkProjectAccess(json.project)
        SecUser currentUser = cytomineService.getCurrentUser()
        log.info "current user = " + currentUser.username
        json.user = currentUser.id
        synchronized (this.getClass()) {
            executeCommand(new AddCommand(user: currentUser), json)
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def update(def domain,def json) {
        checkProjectAccess(domain?.project?.id)
        SecUser currentUser = cytomineService.getCurrentUser()
        executeCommand(new EditCommand(user: currentUser), json)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def delete(def domain,def json) {
        checkProjectAccess(domain?.project?.id)

        Transaction transaction = transactionService.start()
        SecUser currentUser = cytomineService.getCurrentUser()
        //Read image
        ImageInstance imageInstance = retrieve(json)

        if(imageInstance && imageInstance.reviewStart!=null)
            throw new ConstraintException("You cannot remove an image instance that is review or has been reviewed...")

        /* Delete social stuff */
        UserPosition.findAllByImage(imageInstance).each { userPosition ->
            userPosition.delete()
        }
        FollowRequest.findAllByImage(imageInstance).each { followRequest ->
            followRequest.delete()
        }
        //Delete each annotation from image (if possible)
        if (imageInstance) {
            log.info "Delete userAnnotation from image"
            def userAnnotations = UserAnnotation.findAllByImage(imageInstance)
            userAnnotations.each { annotation ->
                userAnnotationService.deleteAnnotation(annotation, currentUser, false,transaction)
            }
            log.info "Delete algoAnnotations from image"
            def algoAnnotations = AlgoAnnotation.findAllByImage(imageInstance)
            algoAnnotations.each { annotation ->
                algoAnnotationService.deleteAnnotation(annotation, currentUser, false,transaction)
            }
        }
        //Delete image
        log.info "Delete image"
        Long id = imageInstance?.id
        if (!imageInstance) throw new ObjectNotFoundException("Image Instance $json.idproject - $json.idimage not found")
        def jsonImage = JSON.parse("{id : $id}")
        def result = executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), jsonImage)

        //Stop transaction
        transactionService.stop()

        return result
    }

    /**
     * Create new domain in database
     * @param json JSON data for the new domain
     * @param printMessage Flag to specify if confirmation message must be show in client
     * Usefull when we create a lot of data, just print the root command message
     * @return Response structure (status, object data,...)
     */
    def create(JSONObject json, boolean printMessage) {
        create(ImageInstance.createFromDataWithId(json), printMessage)
    }

    /**
     * Create new domain in database
     * @param domain Domain to store
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def create(ImageInstance domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.id, domain.baseImage?.filename, domain.project.name], printMessage, "Add", domain.getCallBack())
    }

    /**
     * Destroy domain from database
     * @param json JSON with domain data (to retrieve it)
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(ImageInstance.get(json.id), printMessage)
    }

    /**
     * Destroy domain from database
     * @param domain Domain to remove
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def destroy(ImageInstance domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.id, domain.baseImage?.filename, domain.project.name], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain from database
     * @param json domain data in json
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new ImageInstance(), json), printMessage)
    }

    /**
     * Edit domain from database
     * @param domain Domain to update
     * @param printMessage Flag to specify if confirmation message must be show in client
     * @return Response structure (status, object data,...)
     */
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
        ImageInstance imageInstance
        imageInstance = ImageInstance.read(json.id)
        if (!imageInstance) {
            throw new ObjectNotFoundException("ImageInstance " + json.id + " not found")
        }
        return imageInstance
    }
}
