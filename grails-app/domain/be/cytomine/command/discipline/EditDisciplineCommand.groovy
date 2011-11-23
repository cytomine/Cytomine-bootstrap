package be.cytomine.command.discipline

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Discipline
import grails.converters.JSON

class EditDisciplineCommand extends EditCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = true;

    def execute() {
        //Retrieve
        Discipline updatedDomain = Discipline.get(json.id)
        if (!updatedDomain) throw new ObjectNotFoundException("Discipline ${json.id} not found")
        def oldDomain = updatedDomain.encodeAsJSON()
        updatedDomain.getFromData(updatedDomain, json)
        //Validate and save domain
        domainService.editDomain(updatedDomain,json)
        //Build response message
        String message = createMessage(updatedDomain, [updatedDomain.id, updatedDomain.name])
        //Init command info
        fillCommandInfo(updatedDomain,oldDomain,message)
        //Create and return response
        return responseService.createResponseMessage(updatedDomain,message,printMessage)
    }

    def undo() {
        log.info "Undo"
        def disciplineData = JSON.parse(data)
        Discipline discipline = Discipline.findById(disciplineData.previousDiscipline.id)
        discipline = Discipline.getFromData(discipline, disciplineData.previousDiscipline)
        discipline.save(flush: true)
        super.createUndoMessage(disciplineData, discipline, [discipline.id, discipline.name] as Object[])
    }

    def redo() {
        log.info "Redo"
        def disciplineData = JSON.parse(data)
        Discipline discipline = Discipline.findById(disciplineData.newDiscipline.id)
        discipline = Discipline.getFromData(discipline, disciplineData.newDiscipline)
        discipline.save(flush: true)
        super.createRedoMessage(disciplineData, discipline, [discipline.id, discipline.name] as Object[])
    }

}