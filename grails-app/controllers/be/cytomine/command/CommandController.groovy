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

    def results = []
    if (UndoStackItem.findAllByUser(user).size() == 0) {
      def message = messageSource.getMessage('be.cytomine.UndoCommand', [] as Object[], Locale.ENGLISH)

      def data = [success : true, message: message, callback : null,printMessage:true]
      results << data

      response.status = 200
      withFormat {
        json { render results as JSON }
        xml { render results as XML }
      }
      return
    }

    def result = null

    //first command
    def firstUndoStack = UndoStackItem.findAllByUser(user).last()
    log.debug "******* firstUndoStack ******* =" + firstUndoStack
    def transactionInProgress = firstUndoStack.transactionInProgress //backup


    if (!transactionInProgress) {
      log.debug "******* TRANSACTION NOT IN PROGRESS *******"
      result = firstUndoStack.getCommand().undo()
      new RedoStackItem(
              command : firstUndoStack.getCommand(),
              user : firstUndoStack.getUser(),
              transactionInProgress:  firstUndoStack.transactionInProgress
      ).save(flush : true)
      new CommandHistory(command:firstUndoStack.getCommand(),prefixAction:"UNDO").save();

      firstUndoStack.delete(flush : true)
      results << result.data
      response.status = result.status
    }
    boolean noError = true;
    int firstTransaction
    if (transactionInProgress) {
      log.debug "******* TRANSACTION IN PROGRESS *******"
      def undoStacks = UndoStackItem.findAllByUser(user).reverse()
      if(undoStacks.size()>0)
        firstTransaction =  undoStacks.get(0).transaction
      for (undoStack in undoStacks) {
        log.debug "******* UNDO STACK ITEM *******" +  firstTransaction + "VS" + undoStack.transaction
        if (!undoStack.transactionInProgress || firstTransaction!=undoStack.transaction) break;
        result = undoStack.getCommand().undo()
        results << result.data
        noError = noError && (result.status==200 || result.status==201)

        new RedoStackItem(
                command : undoStack.getCommand(),
                user : undoStack.getUser(),
                transactionInProgress:  undoStack.transactionInProgress,
                transaction : undoStack.transaction
        ).save(flush : true)
        new CommandHistory(command:undoStack.getCommand(),prefixAction:"UNDO").save();

        undoStack.delete(flush:true)
      }
      response.status = noError?200:400
    }
    log.debug results
    withFormat {
      json { render results as JSON }
      xml { render results as XML }
    }
  }

  def redo = {
    User user = User.read(springSecurityService.principal.id)

    def results = []
    if (RedoStackItem.findAllByUser(user).size() == 0) {
      def message = messageSource.getMessage('be.cytomine.RedoCommand', [] as Object[], Locale.ENGLISH)

      def data = [success : true, message: message, callback : null,printMessage:true]
      results << data

      response.status = 200
       withFormat {
        json { render results as JSON }
        xml { render results as XML }
      }
      return
    }

    def result = null

    //first command
    def lastRedoStack = RedoStackItem.findAllByUser(user).last()
    def transactionInProgress = lastRedoStack.transactionInProgress //backup


    if (!transactionInProgress) {
      result = lastRedoStack.getCommand().redo()
      new UndoStackItem(
              command : lastRedoStack.getCommand(),
              user : lastRedoStack.getUser(),
              transactionInProgress:  lastRedoStack.transactionInProgress,

      ).save(flush : true)
      new CommandHistory(command:lastRedoStack.getCommand(),prefixAction:"REDO").save();

      lastRedoStack.delete(flush : true)
      results << result.data
      response.status = result.status
    }
    boolean noError = true;
     int firstTransaction
    if (transactionInProgress) {
      def redoStacks = RedoStackItem.findAllByUser(user).reverse()
      if(redoStacks.size()>0)
        firstTransaction =  redoStacks.get(0).transaction
      for (redoStack in redoStacks) {
        log.debug "******* REDO STACK ITEM *******"
        log.debug redoStack.getCommand()
        log.debug redoStack.transactionInProgress
        if (!redoStack.transactionInProgress || firstTransaction!=redoStack.transaction) break;
        result = redoStack.getCommand().redo()
        results << result.data
        noError = noError && (result.status==200 || result.status==201)

        new UndoStackItem(
                command : redoStack.getCommand(),
                user : redoStack.getUser(), transactionInProgress:
                redoStack.transactionInProgress,transaction : redoStack.transaction,

        ).save(flush : true)
        new CommandHistory(command:redoStack.getCommand(),prefixAction:"REDO").save();

        redoStack.delete(flush:true)
      }
      response.status = noError?200:400
    }



    withFormat {
      json { render results as JSON }
      xml { render results as XML }
    }
  }
}
