package be.cytomine

import be.cytomine.image.ImageInstance
import be.cytomine.image.AbstractImage
import be.cytomine.project.Project
import be.cytomine.security.User
import be.cytomine.command.imageinstance.AddImageInstanceCommand
import be.cytomine.command.imageinstance.EditImageInstanceCommand
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Annotation
import be.cytomine.command.imageinstance.DeleteImageInstanceCommand
import grails.converters.JSON
import be.cytomine.Exception.ObjectNotFoundException

class ImageInstanceService {

    static transactional = true

    def cytomineService
    def commandService
    def transactionService
    def annotationService

    def read(def id) {
        ImageInstance.read(id)
    }

    def read(Project project, AbstractImage image) {
        ImageInstance.findByBaseImageAndProject(image,project)
    }

    def get(def id) {
        ImageInstance.get(id)
    }

    def get(Project project, AbstractImage image) {
        ImageInstance.findByBaseImageAndProject(image,project)
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
        synchronized(this.getClass()) {
        commandService.processCommand(new AddImageInstanceCommand(user: currentUser), json)
        }
    }

    def update(def json) {
        User currentUser = cytomineService.getCurrentUser()
        commandService.processCommand(new EditImageInstanceCommand(user: currentUser), json)
    }

    def delete(def idProject, def idImage) {
        //Start transaction
        transactionService.start()
        User currentUser = cytomineService.getCurrentUser()
        //Read image
        Project project = Project.read(idProject)
        AbstractImage image = AbstractImage.read(idImage)
        ImageInstance imageInstance = ImageInstance.findByBaseImageAndProject(image,project)

        //Delete each annotation from image (if possible)
        if(imageInstance) {
            log.info "Delete annotation from image"
            def annotations = Annotation.findAllByImage(imageInstance)
            annotations.each { annotation ->
                annotationService.deleteAnnotation(annotation,currentUser,false)
            }
        }
        //Delete image
        log.info "Delete image"
        Long id = imageInstance?.id
        if(!imageInstance) throw new ObjectNotFoundException("Image Instance $idProject - $idImage not found")
        def json = JSON.parse("{id : $id}")
        def result = commandService.processCommand(new DeleteImageInstanceCommand(user:currentUser,printMessage:true), json)

        //Stop transaction
        transactionService.stop()

        return result
    }
}
