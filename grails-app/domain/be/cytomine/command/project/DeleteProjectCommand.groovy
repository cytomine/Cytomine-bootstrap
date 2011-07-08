package be.cytomine.command.project

import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import grails.converters.JSON
import be.cytomine.command.DeleteCommand
import java.util.prefs.BackingStoreException

import be.cytomine.project.ProjectGroup
import be.cytomine.security.UserGroup
import be.cytomine.security.Group

class DeleteProjectCommand extends DeleteCommand implements UndoRedoCommand {

  def execute() {
    log.info "Execute"
    try {
      def postData = JSON.parse(postData)
      Project project = Project.findById(postData.id)
      log.info "project " + project +" " + project?.name + " will be deleted"
      Group projectGroup = Group.findByName(project.name);
      //TEMP CODE: group and project must have same name
      if(projectGroup) {
        projectGroup.name = "TO REMOVE " + project.id
        log.info "group " + projectGroup + " will be renamed"
        projectGroup.save(flush:true)
      }
      def groups = project.groups()
      groups.each { group ->
          ProjectGroup.unlink(project, group)
          //for each group, delete user link
          def users = group.users()
          users.each { user ->
             UserGroup.unlink(user, group)
          }

          //delete group
          group.delete(flush:true)
      }
      return super.deleteAndCreateDeleteMessage(postData.id,project,[project.id,project.name] as Object[])
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
    def projectData = JSON.parse(data)
    Project project = Project.createFromData(projectData)
    project.id = projectData.id;
    project.save(flush:true)
    log.error "Project errors = " + project.errors
    return super.createUndoMessage(project,[project.id,project.name] as Object[]);
  }

  def redo() {
    log.info("Redo")
    def postData = JSON.parse(postData)
    Project project = Project.findById(postData.id)
    project.delete(flush:true);
    String id = postData.id
    return super.createRedoMessage(id,project,[postData.id,postData.name] as Object[]);
  }

}
