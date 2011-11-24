package be.cytomine.command.ontology

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.ontology.Ontology
import be.cytomine.project.Project
import grails.converters.JSON

class DeleteOntologyCommand extends DeleteCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() throws CytomineException {

        //Retrieve domain
        Ontology domain = Ontology.get(json.id)
        if (!domain) throw new ObjectNotFoundException("Ontology " + json.id + " was not found")
        if (domain && Project.findAllByOntology(domain).size() > 0) throw new ConstraintException("Ontology is still map with project")
        //Build response message
        String message = createMessage(domain, [domain.id, domain.name])
        //Init command info
        fillCommandInfo(domain,message)
        //Delete domain
        domainService.deleteDomain(domain)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        return restore(ontologyService,JSON.parse(data))
    }

    def redo() {
        return destroy(ontologyService,JSON.parse(data))
    }
}