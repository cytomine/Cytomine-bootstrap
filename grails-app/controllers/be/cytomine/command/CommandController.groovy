package be.cytomine.command

import be.cytomine.command.stack.RedoStack
import be.cytomine.command.stack.UndoStack
import be.cytomine.security.User

class CommandController {

  def undo = {
    User user = User.findByUsername("stevben")
    def lastCommands = UndoStack.findAllByUser(user)

    if (lastCommands.size() == 0) {
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
    render ""
  }

  def redo = {
    User user = User.findByUsername("stevben")
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
    render ""
  }
}
