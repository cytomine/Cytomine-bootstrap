package be.cytomine.command.discipline

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 15:58
 * To change this template use File | Settings | File Templates.
 */

import be.cytomine.command.EditCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Discipline
import grails.converters.JSON
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.ObjectNotFoundException

class EditDisciplineCommand extends EditCommand implements UndoRedoCommand {
    boolean saveOnUndoRedoStack = true;

    def execute() {
        Discipline updatedDiscipline = Discipline.get(json.id)
        if (!updatedDiscipline) throw new ObjectNotFoundException("Discipline $json.id was not found")
        return super.validateAndSave(json, updatedDiscipline, [updatedDiscipline.id, updatedDiscipline.name] as Object[])
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