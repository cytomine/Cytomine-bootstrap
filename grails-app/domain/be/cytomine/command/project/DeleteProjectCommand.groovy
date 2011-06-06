package be.cytomine.command.project

import be.cytomine.command.Command
import be.cytomine.command.UndoRedoCommand
import be.cytomine.project.Project
import grails.converters.JSON
import be.cytomine.command.DeleteCommand
import java.util.prefs.BackingStoreException
import be.cytomine.security.Group
import be.cytomine.project.ProjectGroup
import be.cytomine.security.UserGroup

class DeleteProjectCommand extends DeleteCommand implements UndoRedoCommand {

  def execute() {
    log.info "Execute"
    try {

      def postData = JSON.parse(postData)
      Project project = Project.findById(postData.id)
      def groups = project.groups()
      groups.each { group ->
          ProjectGroup.unlink(project, group)
          def users = group.users()
          users.each { user ->
             UserGroup.unlink(user, group)
          }
          group.delete(flush:true)
      }

      return super.deleteAndCreateDeleteMessage(postData.id,project,"Project",[project.id,project.name] as Object[])
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
    Project project = Project.createProjectFromData(projectData)
    project.id = projectData.id;
    project.save(flush:true)
    log.error "Project errors = " + project.errors

    return super.createUndoMessage(
            project,
            'Project',
            [project.id,project.name] as Object[]
    );
  }



  def redo() {
    log.info("Redo")
    def postData = JSON.parse(postData)
    Project project = Project.findById(postData.id)
    project.delete(flush:true);
    String id = postData.id

    return super.createRedoMessage(
            id,
            'Project',
            [postData.id,postData.name] as Object[]
    );
  }

}
