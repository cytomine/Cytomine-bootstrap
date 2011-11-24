package be.cytomine.command.discipline

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 */

import be.cytomine.Exception.ConstraintException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import grails.converters.JSON

class DeleteDisciplineCommand extends DeleteCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Retrieve domain
        Discipline domain = Discipline.findById(json.id)
        if (!domain) throw new ObjectNotFoundException("Discipline $json.id was not found")
        if (domain && Project.findAllByDiscipline(domain).size() > 0) throw new ConstraintException("Discipline is still map with project")
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
        return restore(disciplineService,JSON.parse(data))
    }

    def redo() {
        return destroy(disciplineService,JSON.parse(data))
    }
}