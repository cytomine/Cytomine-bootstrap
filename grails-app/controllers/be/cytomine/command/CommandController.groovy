package be.cytomine.command

import be.cytomine.security.User
import grails.converters.JSON
import grails.converters.XML

class CommandController {
  def springSecurityService
  def messageSource

  def undo = {
    log.info "Undo"
    User user = User.read(springSecurityService.principal.id)
    log.debug "User="+user.id
    def lastCommands = UndoStack.findAllByUser(user)
    log.debug "Lastcommands="+lastCommands

    if (UndoStack.findAllByUser(user).size() == 0) {
      def message = messageSource.getMessage('be.cytomine.UndoCommand', [] as Object[], Locale.ENGLISH)
      def data = [success : true, message: message, callback : null]
      response.status = 200
      withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
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

    println "result : " + result
    response.status = result.status

    withFormat {
      json { render result.data as JSON }
      xml { render result.data as XML }
    }
  }

  def redo = {
    User user = User.read(springSecurityService.principal.id)

    if (RedoStack.findAllByUser(user).size() == 0) {
      def message = messageSource.getMessage('be.cytomine.RedoCommand', [] as Object[], Locale.ENGLISH)
      def data = [success : true, message: message, callback : null]
      response.status = 200
       withFormat {
        json { render data as JSON }
        xml { render data as XML }
      }
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
