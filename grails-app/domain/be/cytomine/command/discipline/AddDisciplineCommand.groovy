package be.cytomine.command.discipline

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */

import be.cytomine.command.AddCommand
import be.cytomine.command.UndoRedoCommand
import grails.converters.JSON
import org.codehaus.groovy.grails.validation.exceptions.ConstraintException
import be.cytomine.project.Discipline
import be.cytomine.Exception.CytomineException

class AddDisciplineCommand extends AddCommand implements UndoRedoCommand {
  boolean saveOnUndoRedoStack = true;
  def execute() {
    log.info("Execute")
    Discipline newDiscipline=null
    try{
      def json = JSON.parse(postData)
      newDiscipline = Discipline.createFromData(json)
      return super.validateAndSave(newDiscipline,["#ID#",json.name] as Object[])
    }catch(ConstraintException  ex){
      return [data : [discipline:newDiscipline,errors:newDiscipline.retrieveErrors()], status : 400]
    }catch(IllegalArgumentException ex){
      return [data : [discipline:null,errors:["Cannot save discipline:"+ex.toString()]], status : 400]
    } catch(CytomineException ex){
      return [data : [image:null,errors:["Cannot save image:"+ex.toString()]], status : 400]
    }
  }

  def undo() {
    log.info("Undo")
    def disciplineData = JSON.parse(data)
    Discipline discipline = Discipline.get(disciplineData.id)
    discipline.delete(flush:true)
    String id = disciplineData.id
    return super.createUndoMessage(id,discipline,[disciplineData.id,disciplineData.name] as Object[]);
  }

  def redo() {
    log.info("Undo")
    def disciplineData = JSON.parse(data)
    def discipline = Discipline.createFromData(disciplineData)
    discipline.id = disciplineData.id
    discipline.save(flush:true)
    return super.createRedoMessage(discipline,[disciplineData.id,disciplineData.name] as Object[]);
  }

}
