package be.cytomine.command

import be.cytomine.security.User
import grails.converters.JSON
import grails.converters.XML

class CommandController {
  def springSecurityService

  def undo = {
    User user = User.read(springSecurityService.principal.id)

    if (UndoStack.findAllByUser(user).size() == 0) {
      log.error "Command stack is empty"
      response.status = 404
      render ""
      return
    }

    def result = null

    //first command
    def firstUndoStack = UndoStack.findAllByUser(user).last()
    def transactionInProgress = firstUndoStack.transactionInProgress //backup

    if (!transactionInProgress) {
      result = firstUndoStack.getCommand().undo()
      new RedoStack(command : firstUndoStack.getCommand(), user : firstUndoStack.getUser(), transactionInProgress:  firstUndoStack.transactionInProgress).save(flush : true)
      firstUndoStack.delete(flush : true)
    }

    if (transactionInProgress) {
      def undoStacks = UndoStack.findAllByUser(user).reverse()
      for (undoStack in undoStacks) {
        if (!undoStack.transactionInProgress) break;
        result = undoStack.getCommand().undo()
        new RedoStack(command : undoStack.getCommand(), user : undoStack.getUser(), transactionInProgress:  undoStack.transactionInProgress).save(flush : true)
        undoStack.delete(flush:true)
      }
    }

    response.status = result.status

    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  def redo = {
    User user = User.read(springSecurityService.principal.id)

    if (RedoStack.findAllByUser(user).size() == 0) {
      log.error "Command stack is empty"
      response.status = 404
      render ""
      return
    }

    def result = null

    //first command
    def lastRedoStack = RedoStack.findAllByUser(user).last()
    def transactionInProgress = lastRedoStack.transactionInProgress //backup

    if (!transactionInProgress) {
      result = lastRedoStack.getCommand().redo()
      new UndoStack(command : lastRedoStack.getCommand(), user : lastRedoStack.getUser(), transactionInProgress:  lastRedoStack.transactionInProgress).save(flush : true)
      lastRedoStack.delete(flush : true)
    }

    if (transactionInProgress) {
      def redoStacks = RedoStack.findAllByUser(user).reverse()
      for (redoStack in redoStacks) {
        if (!redoStack.transactionInProgress) break;
        result = redoStack.getCommand().redo()
        new UndoStack(command : redoStack.getCommand(), user : redoStack.getUser(), transactionInProgress:  redoStack.transactionInProgress).save(flush : true)
        redoStack.delete(flush:true)
      }
    }

    response.status = result.status

    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }
}
