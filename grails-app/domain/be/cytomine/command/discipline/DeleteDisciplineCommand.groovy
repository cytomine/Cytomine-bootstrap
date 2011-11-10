package be.cytomine.command.discipline

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */

import be.cytomine.command.DeleteCommand
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Discipline
import be.cytomine.project.Project
import grails.converters.JSON
import java.util.prefs.BackingStoreException

class DeleteDisciplineCommand extends DeleteCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info "Execute"
    try {
      def postData = JSON.parse(postData)
      Discipline discipline = Discipline.findById(postData.id)
      log.info "discipline="+discipline
      if(discipline && Project.findAllByDiscipline(discipline).size()>0) throw new BackingStoreException("Discipline is still map with project")
      return super.deleteAndCreateDeleteMessage(postData.id,discipline,[discipline.id,discipline.name] as Object[])
    } catch(NullPointerException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 404]
    } catch(BackingStoreException e) {
      log.error(e)
      return [data : [success : false, errors : e.getMessage()], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def disciplineData = JSON.parse(data)
    Discipline discipline = Discipline.createFromData(disciplineData)
    discipline.id = disciplineData.id;
    discipline.save(flush:true)
    log.error "Discipline errors = " + discipline.errors
    return super.createUndoMessage(discipline,[discipline.id,discipline.name] as Object[]);
  }

  def redo() {
    log.info("Redo")
    def postData = JSON.parse(postData)
    Discipline discipline = Discipline.findById(postData.id)
    String id = postData.id
    String name = discipline.name
    discipline.delete(flush:true);
    return super.createRedoMessage(id, discipline,[id,name] as Object[]);
  }
}