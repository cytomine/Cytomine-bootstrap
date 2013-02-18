package be.cytomine.image

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.EditCommand
import be.cytomine.command.Transaction
import be.cytomine.ontology.AlgoAnnotation
import be.cytomine.ontology.UserAnnotation
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.social.UserPosition
import be.cytomine.utils.ModelService
import grails.converters.JSON
import groovy.sql.Sql
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.FetchMode
import org.springframework.security.access.prepost.PreAuthorize
import be.cytomine.ontology.ReviewedAnnotation
import be.cytomine.utils.Task

/**
 * TODO:: refactor + doc!!!!!!!
 */
class ImageInstanceService extends ModelService {

    static transactional = true

    def cytomineService
    def transactionService
    def userAnnotationService
    def algoAnnotationService
    def dataSource
    def reviewedAnnotationService

    def currentDomain() {
        return ImageInstance
    }

    def read(def id) {
        def image = ImageInstance.read(id)
        if(image) {
            SecurityCheck.checkReadAuthorization(image.project)
        }
        image
    }

    def get(def id) {
        def image = ImageInstance.get(id)
        if(image) {
            SecurityCheck.checkReadAuthorization(image.project)
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
            order("created", "desc")
            fetchMode 'baseImage', FetchMode.JOIN
        }
        return images
    }

    /**
     * Get all image id from project
     */
    @PreAuthorize("#project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    public List<Long> getAllImageId(Project project) {
        //better perf with sql request
        String request = "SELECT a.id FROM image_instance a WHERE project_id="+project.id
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0]
        }
        return data
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess(#json['project']) or hasRole('ROLE_ADMIN')")
    def add(def json,SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        log.info "current user = " + currentUser.username
        json.user = currentUser.id
        synchronized (this.getClass()) {
            executeCommand(new AddCommand(user: currentUser), json)
        }
    }

    /**
     * Update this domain with new data from json
     * @param json JSON with new data
     * @param security Security service object (user for right check)
     * @return  Response structure (new domain data, old domain data..)
     */
    @PreAuthorize("#security.checkProjectAccess() or hasRole('ROLE_ADMIN')")
    def update(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        executeCommand(new EditCommand(user: currentUser), json)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess() or hasRole('ROLE_ADMIN')")
    def delete(def json, SecurityCheck security, Task task = null) {
        return delete(retrieve(json),transactionService.start())
    }

    def delete(ImageInstance image, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${image.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.baseImage?.filename, domain.project.name]
    }

    def deleteDependentAlgoAnnotation(ImageInstance image,Transaction transaction, Task task = null) {
        AlgoAnnotation.findAllByImage(image).each {
             algoAnnotationService.delete(it,transaction)
        }
    }

    def deleteDependentReviewedAnnotation(ImageInstance image,Transaction transaction, Task task = null) {
        ReviewedAnnotation.findAllByImage(image).each {
            reviewedAnnotationService.delete(it,transaction,false)
        }
    }

    def deleteDependentUserAnnotation(ImageInstance image,Transaction transaction, Task task = null) {
        UserAnnotation.findAllByImage(image).each {
            userAnnotationService.delete(it,transaction,false)
        }
    }

    def deleteDependentUserPosition(ImageInstance image,Transaction transaction, Task task = null) {
        UserPosition.findAllByImage(image).each {
            it.delete()
        }
    }
}
