package be.cytomine.command

import be.cytomine.security.User
import grails.converters.*

class CommandController {
  def springSecurityService

  def undo = {
    log.info "Undo"
    User user = User.get(springSecurityService.principal.id)
    log.debug "User="+user.id
    def lastCommands = UndoStack.findAllByUser(user)
    log.debug "Lastcommands="+lastCommands

    if (lastCommands.size() == 0) {
      log.error "Command stack is empty"
      response.status = 404
      render ""
      return
    }

    def lastTransaction = lastCommands.last().getCommand().getTransaction()
    def result = null
    lastCommands.reverseEach { undoCommand ->
      if (undoCommand.getCommand().getTransaction().id != lastTransaction.id) return
      result = undoCommand.getCommand().undo()
      new RedoStack(command : undoCommand.getCommand(), user : undoCommand.getUser()).save()
      undoCommand.delete()
    }
    response.status = result.status

    withFormat {
      json { render result.data ?: "" }
      xml { render result.data ?: ""}
    }
  }

  def redo = {
    User user = User.get(springSecurityService.principal.id)
    def redoCommands = RedoStack.findAllByUser(user)

    if (redoCommands.size() == 0) {
      response.status = 404
      render ""
      return
    }

    def lastTransaction = redoCommands.last().getCommand().getTransaction()
    def result = null
    redoCommands.reverseEach { redoCommand ->
      if (redoCommand.getCommand().getTransaction().id != lastTransaction.id) return
      result = redoCommand.getCommand().redo()
      new UndoStack(command : redoCommand.getCommand(), user : redoCommand.getUser()).save()
      redoCommand.delete()
    }
    response.status = result.status

    /*withFormat {
      json { render result.data ?: "" }
      xml { render result.data ?: ""}
    }*/
    //TODO: must work with withformat!
    render result.data as JSON
  }
}
