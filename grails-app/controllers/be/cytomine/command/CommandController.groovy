package be.cytomine.command

import be.cytomine.api.RestController
import be.cytomine.security.SecUser
import be.cytomine.Exception.ObjectNotFoundException

/**
 * Controller for command.
 * A command is an op done by a user (or job).
 * Some command my be undo/redo (e.g. Undo Add annotation x => delete annotation x)
 */
class CommandController extends RestController {
    def springSecurityService
    def messageSource
    def commandService

    /**
     * Do undo op on the last command/transaction done by this user
     */
    def undo = {
        SecUser user = SecUser.read(springSecurityService.principal.id)

        //Get the last command list with max 1 command
        def lastCommands = UndoStackItem.findAllByUser(user, [sort: "created", order: "desc", max: 1, offset: 0])

        //There is no command, so nothing to undo
        if (lastCommands.isEmpty()) {
            def data = [success: true, message: messageSource.getMessage('be.cytomine.UndoCommand', [] as Object[], Locale.ENGLISH), callback: null, printMessage: true]
            responseSuccess([data], 200)
            return
        }

        def results = []
        def result

        //Last command done
        def firstUndoStack = lastCommands.last()
        def transaction =  firstUndoStack.transaction
        log.debug "FirstUndoStack=" + firstUndoStack

        if (!transaction) {
            log.debug "Transaction not in progress"
            //Not Transaction, no other command must be deleted
            result = firstUndoStack.getCommand().undo()
            //An undo command must be move to redo stack
            moveToRedoStack(firstUndoStack)
            results << result.data
            response.status = result.status
        } else {
            log.debug "Transaction in progress"
            //Its a transaction, many other command will be deleted
            int firstTransaction = -1
            boolean noError = true;
            def undoStacks = UndoStackItem.findAllByUser(user, [sort: "created", order: "desc"])
            if (undoStacks.size() > 0) {
                def subtransaction = undoStacks.get(0).transaction
                if(subtransaction) {
                    firstTransaction = subtransaction.id
                }
            }
            for (undoStack in undoStacks) {
                //browse all command and undo it while its the same transaction
                log.debug "Undo stack transaction:" + firstTransaction + " VS " + undoStack?.transaction?.id
                if (!undoStack.transaction || firstTransaction != undoStack.transaction.id) break;
                if(undoStack.getCommand().refuseUndo) {
                    responseError(new ObjectNotFoundException("You cannot delete your last operation!")) //undo delete project is not possible
                    return
                }
                result = undoStack.getCommand().undo()
                log.info "Undo stack transaction: UNDO " + undoStack?.command?.actionMessage
                results << result.data
                noError = noError && (result.status == 200 || result.status == 201)
                moveToRedoStack(undoStack)
            }
            response.status = noError ? 200 : 400
        }
        responseSuccess(results)
    }

    /**
     * Move an undo stack item to redo stack
     * @param firstUndoStack Undo stack item to move
     */
    private def moveToRedoStack(UndoStackItem firstUndoStack) {
        //create new redo stack item
        new RedoStackItem(
                command: firstUndoStack.getCommand(),
                user: firstUndoStack.getUser(),
                transaction: firstUndoStack.transaction
        ).save(flush: true)
        //save to history stack
        new CommandHistory(command: firstUndoStack.getCommand(), prefixAction: "UNDO", project: firstUndoStack.getCommand().project).save()
        //delete from undo stack
        firstUndoStack.delete(flush: true)
    }

    /**
     * Do redo op on the last undo done by this user
     */
    def redo = {
        SecUser user = SecUser.read(springSecurityService.principal.id)
        //Get the last undo command
        def lastCommands = RedoStackItem.findAllByUser(user, [sort: "created", order: "desc", max: 1, offset: 0])

        def results = []
        if (lastCommands.size() == 0) {
            def data = [success: true, message: messageSource.getMessage('be.cytomine.RedoCommand', [] as Object[], Locale.ENGLISH), callback: null, printMessage: true]
            responseSuccess([data], 200)
            return
        }

        def lastRedoStack = lastCommands.last()
        def result
        def transaction = lastRedoStack.transaction //backup
        boolean noError = true;
        int firstTransaction = -1

        if (!transaction) {
            log.debug "Transaction not in progress"
            //last command not in transaction, just redo it
            result = lastRedoStack.getCommand().redo()
            moveToUndoStack(lastRedoStack)
            results << result.data
            response.status = result.status
        } else {
            log.debug "Transaction in progress"
            //last command in a transaction, redo other command from transaction

            def redoStacks = RedoStackItem.findAllByUser(user, [sort: "created", order: "desc"])

            if (redoStacks.size() > 0) {
                def subtransaction = redoStacks.get(0).transaction
                if(subtransaction) {
                    firstTransaction = subtransaction.id
                }
            }
            for (redoStack in redoStacks) {
                //Redo each command from the same transaction
                if (!redoStack.transaction || firstTransaction != redoStack.transaction.id) break;
                result = redoStack.getCommand().redo()
                results << result.data
                noError = noError && (result.status == 200 || result.status == 201)
                moveToUndoStack(redoStack)
            }
            response.status = noError ? 200 : 400
        }
        responseSuccess(results)
    }

    /**
     * Move redo item to the undo stack
     * @param lastRedoStack redo item to move
     */
    private def moveToUndoStack(RedoStackItem lastRedoStack) {
        //create the new undo item
        new UndoStackItem(
                command: lastRedoStack.getCommand(),
                user: lastRedoStack.getUser(),
                transaction: lastRedoStack.transaction,
        ).save(flush: true)
        //add to history stack
        new CommandHistory(command: lastRedoStack.getCommand(), prefixAction: "REDO", project: lastRedoStack.getCommand().project).save();
        //delete the redo item
        lastRedoStack.delete(flush: true)
    }


}