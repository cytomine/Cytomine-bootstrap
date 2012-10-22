package be.cytomine.image

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.social.FollowRequest
import be.cytomine.social.UserPosition
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.FetchMode
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.ontology.UserAnnotation
import be.cytomine.ontology.AlgoAnnotation

class ImageInstanceService extends ModelService {

    static transactional = true

    def cytomineService
    def commandService
    def transactionService
    def responseService
    def domainService
    def userAnnotationService
    def algoAnnotationService

    boolean saveOnUndoRedoStack = true

    def read(def id) {
        ImageInstance.read(id)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def read(Project project, AbstractImage image) {
        ImageInstance.findByBaseImageAndProject(image, project)
    }

    def get(def id) {
        ImageInstance.get(id)
        //get(image.project, image.baseImage)
    }

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def get(Project project, AbstractImage image) {
        ImageInstance.findByBaseImageAndProject(image, project)
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
    def list(Project project, List ids) {
        ImageInstance.findAllByIdInList(ids)
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

    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def listDatatables(Project project) {
        //TO DO with plugin searchable ?
        /*def availablesCols = ["baseImage.thumbURL", "filename", "mime.extension", "width", "height", "magnification", "resolution", "countImageAnnotations", "created", "action"]
        def search = params.sSearch
        def colSort = Integer.parseInt(params.iSortCol_0)
        def col = availablesCols[colSort]
        def order = params.sSortDir_0
        def first = params.iDisplayStart
        def max  = params.iDisplayLength
        log.info "col = " + col
        log.info "order = " + order
        log.info "first = " + first
        log.info "max = " + max

        def images = ImageInstance.createCriteria().list(offset: first, max: max, sort: col, order: order) {
            eq("project", project)
            fetchMode 'baseImage', FetchMode.JOIN
            fetchMode 'baseImage.storageAbstractImages', FetchMode.JOIN
        }
        return images*/
        []
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        log.info "current user = " + currentUser.username
        json.user = currentUser.id
        synchronized (this.getClass()) {
            executeCommand(new AddCommand(user: currentUser), json)
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def update(def domain,def json) {
//        if(domain) checkAuthorization(domain.projectId)
        SecUser currentUser = cytomineService.getCurrentUser()
        executeCommand(new EditCommand(user: currentUser), json)
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    def delete(def domain,def json) {
//        if(domain) checkAuthorization(domain.projectId)
        //Start transaction
        Transaction transaction = transactionService.start()
        SecUser currentUser = cytomineService.getCurrentUser()
        //Read image
        Project project = Project.read(json.project)
        AbstractImage image = AbstractImage.read(json.image)
        ImageInstance imageInstance = ImageInstance.findByBaseImageAndProject(image, project)
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
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(ImageInstance.createFromDataWithId(json), printMessage)
    }

    def create(ImageInstance domain, boolean printMessage) {
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
        ImageInstance imageInstance
        imageInstance = ImageInstance.read(json.id)

        if(!imageInstance) {
            AbstractImage image = AbstractImage.read(json.image)
            Project project = Project.read(json.project)
            imageInstance = ImageInstance.findByProjectAndBaseImage(project,image)
        }
        if (!imageInstance) throw new ObjectNotFoundException("ImageInstance " + json.id + " not found")
        return imageInstance
    }
}
