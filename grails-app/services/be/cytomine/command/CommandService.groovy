package be.cytomine.command

import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.TooLongRequestException

class CommandService {

    def springSecurityService
    def grailsApplication

    static final int SUCCESS_ADD_CODE = 200
    static final int SUCCESS_EDIT_CODE = 200
    static final int SUCCESS_DELETE_CODE = 200

    static final int NOT_FOUND_CODE = 404
    static final int TOO_LONG_REQUEST = 413

    /**
     * Execute an 'addcommand' c with json data
     * Store command in undo stack if necessary and in command history
     */
    def processCommand(AddCommand c) throws CytomineException {
        processCommand(c, SUCCESS_ADD_CODE)
    }

    /**
     * Execute an 'editcommand' c with json data
     * Store command in undo stack if necessary and in command history
     */
    def processCommand(EditCommand c) throws CytomineException {
        processCommand(c, SUCCESS_EDIT_CODE)
    }

    /**
     * Execute a 'deletecommand' c with json data
     * Store command in undo stack if necessary and in command history
     */
    def processCommand(DeleteCommand c) throws CytomineException {
        processCommand(c, SUCCESS_DELETE_CODE)
    }

    /**
     * Execute a 'command' c with json data
     * Store command in undo stack if necessary and in command history
     * if success, put http response code as successCode
     */
    def processCommand(Command c, int successCode) throws CytomineException {
        log.debug "processCommand: ${c.class}"
        println "3.image=${c.domain}"
        String postData = c.json?.toString()
        def maxRequestSize = grailsApplication.config.cytomine.maxRequestSize

        //check if request data are not too big
        if (postData && postData.size() >= maxRequestSize) {
            log.error "c.postData.size() is too big=" + postData.size() + " Command.MAXSIZEREQUEST=" + maxRequestSize
            throw new TooLongRequestException("Request is too long")
        }

        //execute command
        def result = c.execute()
        if (result.status == successCode) {
            if (!c.validate()) {
                log.error c.errors.toString()
            }
            c.save()
            CommandHistory ch = new CommandHistory(command: c, prefixAction: "", project: c.project,user: c.user, message: c.actionMessage)
            ch.save();
            log.info "Save on undo stack: ${c.saveOnUndoRedoStack}"  + " transaction " + c.transaction?.id

            if (c.saveOnUndoRedoStack) {
                log.debug "Save..."
                def item = new UndoStackItem(command: c, user: c.user, transaction: c.transaction)
                log.info item.validate()
                item.save(flush: true,failOnError: true)
                log.info "Item = ${item}"
            }
        }
        log.debug "result.status=" + result.status
        return result
    }
}
