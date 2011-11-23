package be.cytomine.command.discipline

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 */

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Discipline
import grails.converters.JSON

class AddDisciplineCommand extends AddCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Init new domain object
        Discipline domain = Discipline.createFromData(json)
        //Validate and save domain
        domainService.saveDomain(domain)
        //Build response message
        String message = createMessage(domain, [domain.id, domain.name])
        //Init command info
        fillCommandInfo(domain,message)
        //Create and return response
        return responseService.createResponseMessage(domain,message,printMessage)
    }

    def undo() {
        def disciplineData = JSON.parse(data)
        Discipline discipline = Discipline.get(disciplineData.id)
        discipline.delete(flush: true)
        String id = disciplineData.id
        return super.createUndoMessage(id, discipline, [disciplineData.id, disciplineData.name] as Object[]);
    }

    def redo() {
        def disciplineData = JSON.parse(data)
        def discipline = Discipline.createFromData(disciplineData)
        discipline.id = disciplineData.id
        discipline.save(flush: true)
        return super.createRedoMessage(discipline, [disciplineData.id, disciplineData.name] as Object[]);
    }

}
