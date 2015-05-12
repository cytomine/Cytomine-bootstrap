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
 * The DeleteCommand class is a command that delete a domain
 * It provide an execute method that delete domain from command, an undo method that re-build domain and an redo method that delete domain
 * @author ULG-GIGA Cytomine Team
 */
class DeleteCommand extends Command {

    def backup
    /**
     * Add project link in command
     */
    boolean linkProject = true


    /**
     * Process an Add operation for this command
     * @return Message
     */
    def execute() {
        initService()
        //Retrieve domain to delete it
        def oldDomain = domain
        //Init command info
        CytomineDomain container = oldDomain?.container()
        if(container && container instanceof Project && linkProject) {
            super.setProject(container)
        }
        def response = service.destroy(oldDomain, printMessage)
        fillCommandInfoJSON(backup, response.data.message)
        return response

    }

    /**
     * Process an undo op
     * @return Message
     */
    def undo() {
        initService()
        return service.create(JSON.parse(data), printMessage)
    }

    /**
     * Process a redo op
     * @return Message
     */
    def redo() {
        initService()
        return service.destroy(JSON.parse(data), printMessage)
    }
}
