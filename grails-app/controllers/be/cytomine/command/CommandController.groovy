package be.cytomine.command

import be.cytomine.security.User
import grails.converters.JSON
import grails.converters.XML

class CommandController {
  def springSecurityService
  def messageSource

  //static allowedMethods = [undo:'POST', redo:'POST']

  def undo = {
    log.info "Undo"
    User user = User.read(springSecurityService.principal.id)
    log.debug "User="+user.id
    def lastCommands = UndoStackItem.findAllByUser(user)
    log.debug "Lastcommands="+lastCommands

    if (UndoStackItem.findAllByUser(user).size() == 0) {
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
    def firstUndoStack = UndoStackItem.findAllByUser(user).last()
    def transactionInProgress = firstUndoStack.transactionInProgress //backup

    if (!transactionInProgress) {
      result = firstUndoStack.getCommand().undo()
      new RedoStackItem(command : firstUndoStack.getCommand(), user : firstUndoStack.getUser(), transactionInProgress:  firstUndoStack.transactionInProgress).save(flush : true)
      firstUndoStack.delete(flush : true)
    }

    if (transactionInProgress) {
      def undoStacks = UndoStackItem.findAllByUser(user).reverse()
      for (undoStack in undoStacks) {
        if (!undoStack.transactionInProgress) break;
        result = undoStack.getCommand().undo()
        new RedoStackItem(command : undoStack.getCommand(), user : undoStack.getUser(), transactionInProgress:  undoStack.transactionInProgress).save(flush : true)
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

    if (RedoStackItem.findAllByUser(user).size() == 0) {
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
    def lastRedoStack = RedoStackItem.findAllByUser(user).last()
    def transactionInProgress = lastRedoStack.transactionInProgress //backup

    if (!transactionInProgress) {
      result = lastRedoStack.getCommand().redo()
      new UndoStackItem(command : lastRedoStack.getCommand(), user : lastRedoStack.getUser(), transactionInProgress:  lastRedoStack.transactionInProgress).save(flush : true)
      lastRedoStack.delete(flush : true)
    }

    if (transactionInProgress) {
      def redoStacks = RedoStackItem.findAllByUser(user).reverse()
      for (redoStack in redoStacks) {
        if (!redoStack.transactionInProgress) break;
        result = redoStack.getCommand().redo()
        new UndoStackItem(command : redoStack.getCommand(), user : redoStack.getUser(), transactionInProgress:  redoStack.transactionInProgress).save(flush : true)
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
