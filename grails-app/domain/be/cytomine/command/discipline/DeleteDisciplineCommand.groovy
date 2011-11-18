package be.cytomine.command.discipline

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 */

import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import grails.converters.JSON
import java.util.prefs.BackingStoreException
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.Exception.ConstraintException

class DeleteDisciplineCommand extends DeleteCommand implements UndoRedoCommand {

    boolean saveOnUndoRedoStack = true;

    def execute() {
        Discipline discipline = Discipline.findById(json.id)
        if (!discipline) throw new ObjectNotFoundException("Discipline $json.id was not found")
        if (discipline && Project.findAllByDiscipline(discipline).size() > 0) throw new ConstraintException("Discipline is still map with project")
        return super.deleteAndCreateDeleteMessage(json.id, discipline, [discipline.id, discipline.name] as Object[])
    }

    def undo() {
        log.info("Undo")
        def disciplineData = JSON.parse(data)
        Discipline discipline = Discipline.createFromData(disciplineData)
        discipline.id = disciplineData.id;
        discipline.save(flush: true)
        return super.createUndoMessage(discipline, [discipline.id, discipline.name] as Object[]);
    }

    def redo() {
        log.info("Redo")
        def postData = JSON.parse(postData)
        Discipline discipline = Discipline.findById(postData.id)
        String id = postData.id
        String name = discipline.name
        discipline.delete(flush: true);
        return super.createRedoMessage(id, discipline, [id, name] as Object[]);
    }
}