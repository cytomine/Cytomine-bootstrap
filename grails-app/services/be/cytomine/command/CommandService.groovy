package be.cytomine.command

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.Exception.CytomineException

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
        //def maxRequestSize = grailsApplication.config.cytomine.maxRequestSize
        //check if request data are not too big
//        if (postData && postData.size() >= maxRequestSize) {
//            log.error "c.postData.size() is too big=" + postData.size() + " Command.MAXSIZEREQUEST=" + maxRequestSize
//            throw new TooLongRequestException("Request is too long")
//        }
        //execute command
        def result = c.execute()
        if (result.status == successCode) {
            if (!c.validate()) {
                log.error c.errors.toString()
            }
            c.save(failOnError: true)
            CommandHistory ch = new CommandHistory(command: c, prefixAction: "", project: c.project,user: c.user, message: c.actionMessage)
            ch.save(failOnError: true);
            if (c.saveOnUndoRedoStack) {
                def item = new UndoStackItem(command: c, user: c.user, transaction: c.transaction)
                item.save(flush: true,failOnError: true)
            }
        }
        return result
    }
}
