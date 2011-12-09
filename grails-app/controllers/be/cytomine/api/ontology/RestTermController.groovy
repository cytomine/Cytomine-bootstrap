package be.cytomine.api.ontology

import be.cytomine.api.RestController
import be.cytomine.image.ImageInstance
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import grails.converters.JSON

class RestTermController extends RestController {

    def termService

    def list = {
        responseSuccess(termService.list())
    }

    def show = {
        Term term = termService.read(params.long('id'))
        if (term) responseSuccess(term)
        else responseNotFound("Term", params.id)
    }

    def listByOntology = {
        Ontology ontology = Ontology.read(params.idontology)
        if (ontology) responseSuccess(termService.list(ontology))
        else responseNotFound("Term", "Ontology", params.idontology)
    }

    def listAllByProject = {
        Project project = Project.read(params.idProject)
        if (project && project.ontology) responseSuccess(termService.list(project))
        else responseNotFound("Term", "Project", params.idProject)
    }

    def listByImageInstance = {
        ImageInstance image = ImageInstance.read(params.id)
        if (image) responseSuccess(termService.list(image))
        else responseNotFound("Term", "Image", params.id)
    }

    def statProject = {
        Term term = Term.read(params.id)
        if (term) termService.statProject(term)
        else responseNotFound("Project", params.id)
    }

    def add = {
        add(termService, request.JSON)
    }

    def update = {
        update(termService, request.JSON)
    }

    def delete = {
        delete(termService, JSON.parse("{id : $params.id}"))
    }

}
