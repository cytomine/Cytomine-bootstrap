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
import be.cytomine.security.SecUser
import be.cytomine.security.User

/**
 * @author ULG-GIGA Cytomine Team
 * The RedoStackItem class allow to store command on a redo stack so that a command or a group of command can be redo
 */
class RedoStackItem extends CytomineDomain {

    /**
     * User who launch command
     */
    SecUser user

    /**
     * Command save on redo stack
     */
    Command command


    /**
     * Transaction id
     */
    Transaction transaction

    static belongsTo = [user: User, command: Command]

    static constraints = {
        transaction(nullable: true)
    }
}
