package be.cytomine.command

import be.cytomine.security.User
import grails.converters.JSON
import grails.converters.XML
import be.cytomine.api.RestController

class CommandController extends RestController{
  def springSecurityService
  def messageSource

  def undo = {
    log.info "Undo"
    User user = User.read(springSecurityService.principal.id)
    log.debug "User="+user.id
    def lastCommands = UndoStackItem.findAllByUser(user,[sort:"created", order:"desc", max:1, offset:0])
    log.debug "Lastcommands="+lastCommands

    if (lastCommands.size() == 0) {
      log.debug "Nothing to undo"
      buildNothingToUndoResponse()
      return
    }

    def results = []
    def result

    //first command
    def firstUndoStack = lastCommands.last()
    log.debug "FirstUndoStack=" + firstUndoStack

    def transactionInProgress = firstUndoStack.transactionInProgress //backup
    boolean noError = true;
    int firstTransaction=-1

    if (!transactionInProgress) {
      log.debug "Transaction not in progress"
      result = firstUndoStack.getCommand().undo()
      moveToRedoStack(firstUndoStack)
      results << result.data
      response.status = result.status
    }else  {
      log.debug "Transaction in progress"
      def undoStacks = UndoStackItem.findAllByUser(user,[sort:"created", order:"desc"])
      if(undoStacks.size()>0)
        firstTransaction =  undoStacks.get(0).transaction
      for (undoStack in undoStacks) {
        log.debug "Undo stack transaction:" +  firstTransaction + " VS " + undoStack.transaction
        if (!undoStack.transactionInProgress || firstTransaction!=undoStack.transaction) break;
        result = undoStack.getCommand().undo()
        results << result.data
        noError = noError && (result.status==200 || result.status==201)
        moveToRedoStack(undoStack)
      }
      response.status = noError?200:400
    }
    log.debug results
    responseSuccess(results)
  }

  private def buildNothingToUndoResponse() {
    def results = []
    def message = messageSource.getMessage('be.cytomine.UndoCommand', [] as Object[], Locale.ENGLISH)
    def data = [success: true, message: message, callback: null, printMessage: true]
    results << data
    responseSuccess(results, 200)
  }

  private def moveToRedoStack(UndoStackItem firstUndoStack) {

    new RedoStackItem(
            command : firstUndoStack.getCommand(),
            user : firstUndoStack.getUser(),
            transactionInProgress:  firstUndoStack.transactionInProgress,
            transaction : firstUndoStack.transaction
    ).save(flush : true)
    new CommandHistory(command:firstUndoStack.getCommand(),prefixAction:"UNDO").save();
    firstUndoStack.delete(flush : true)
  }

  def redo = {
    log.info "Redo"
    User user = User.read(springSecurityService.principal.id)
    log.debug "User="+user.id

    def lastCommands = RedoStackItem.findAllByUser(user,[sort:"created", order:"desc",max:1,offset:0])

    def results = []
    if (lastCommands.size() == 0) {
      buildNothingToRedoResponse()
      return
    }
    def lastRedoStack = lastCommands.last()
    def result
    def transactionInProgress = lastRedoStack.transactionInProgress //backup
    boolean noError = true;
    int firstTransaction

    if (!transactionInProgress) {
      log.debug "Transaction not in progress"
      result = lastRedoStack.getCommand().redo()
      moveToUndoStack(lastRedoStack)
      results << result.data
      response.status = result.status
    }else  {
      log.debug "Transaction in progress"
      def redoStacks = RedoStackItem.findAllByUser(user,[sort:"created", order:"desc"])

      if(redoStacks.size()>0)
        firstTransaction =  redoStacks.get(0).transaction
      for (redoStack in redoStacks) {
        log.debug redoStack.getCommand()
        log.debug redoStack.transactionInProgress
        if (!redoStack.transactionInProgress || firstTransaction!=redoStack.transaction) break;
        result = redoStack.getCommand().redo()
        results << result.data
        noError = noError && (result.status==200 || result.status==201)

        moveToUndoStack(redoStack)
      }
      response.status = noError?200:400
    }
    log.debug results
    responseSuccess(results)
  }

  private def buildNothingToRedoResponse() {
    def results = []
    def message = messageSource.getMessage('be.cytomine.RedoCommand', [] as Object[], Locale.ENGLISH)
    def data = [success: true, message: message, callback: null, printMessage: true]
    results << data
    responseSuccess(results,200)
  }

  private def moveToUndoStack(RedoStackItem lastRedoStack) {

    new UndoStackItem(
            command : lastRedoStack.getCommand(),
            user : lastRedoStack.getUser(),
            transactionInProgress:  lastRedoStack.transactionInProgress,
            transaction : lastRedoStack.transaction,
    ).save(flush : true)
    new CommandHistory(command:lastRedoStack.getCommand(),prefixAction:"REDO").save();

    lastRedoStack.delete(flush : true)
  }



}