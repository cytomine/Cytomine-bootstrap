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
        String postData = c.json?.toString()
        def maxRequestSize = grailsApplication.config.cytomine.maxRequestSize
        log.info "1"
        //check if request data are not too big
        if (postData && postData.size() >= maxRequestSize) {
            log.error "c.postData.size() is too big=" + postData.size() + " Command.MAXSIZEREQUEST=" + maxRequestSize
            throw new TooLongRequestException("Request is too long")
        }
        log.info "2"
        //execute command
        def result = c.execute()
        if (result.status == successCode) {
            log.info "2a"
            if (!c.validate()) {
                log.error c.errors.toString()
            }
            c.save()
            log.info "2b"
            CommandHistory ch = new CommandHistory(command: c, prefixAction: "", project: c.project,user: c.user, message: c.actionMessage)
            log.info "2c"
            ch.save();
            log.info "2d"
            if (c.saveOnUndoRedoStack) {
                def item = new UndoStackItem(command: c, user: c.user, transaction: c.transaction)
                item.save(flush: true,failOnError: true)
            }
            log.info "2e"
        }
        log.info "3"
        log.debug "result.status=" + result.status
        log.info "processCommand result"
        return result
    }
}
