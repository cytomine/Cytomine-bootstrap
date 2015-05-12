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

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import grails.converters.JSON

/**
 * @author ULG-GIGA Cytomine Team
 * The AddCommand class is a command that create a new domain
 * It provide an execute method that create domain from command, an undo method that drop domain and an redo method that recreate domain
 */
class AddCommand extends Command {

    /**
     * Process an Add operation for this command
     * @return Message
     */
    def execute() {
        initService()
        //Create new domain from json data
        json.id = null
        def newDomain = service.createFromJSON(json)
        //Save new domain in database
        def response = service.create(newDomain, printMessage)
        //Init command domain
        newDomain = response.object
        fillCommandInfo(newDomain, response.data.message)
        CytomineDomain container = newDomain?.container()
        if(container && container instanceof Project) {
            super.setProject(container)
        }
        return response
    }

    /**
     * Process an undo op
     * @return Message
     */
    def undo() {
        initService()
        return service.destroy(JSON.parse(data), printMessage)
    }

    /**
     * Process a redo op
     * @return Message
     */
    def redo() {
        initService()
        return service.create(JSON.parse(data), printMessage)
    }
}




