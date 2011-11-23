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
        Discipline newDiscipline = Discipline.createFromData(json)
        return super.validateAndSave(newDiscipline, ["#ID#", json.name] as Object[])
    }

    def undo() {
        log.info("Undo")
        def disciplineData = JSON.parse(data)
        Discipline discipline = Discipline.get(disciplineData.id)
        discipline.delete(flush: true)
        String id = disciplineData.id
        return super.createUndoMessage(id, discipline, [disciplineData.id, disciplineData.name] as Object[]);
    }

    def redo() {
        log.info("Undo")
        def disciplineData = JSON.parse(data)
        def discipline = Discipline.createFromData(disciplineData)
        discipline.id = disciplineData.id
        discipline.save(flush: true)
        return super.createRedoMessage(discipline, [disciplineData.id, disciplineData.name] as Object[]);
    }

}
