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
 * The UndoStackItem class allow to store command on a undo stack so that a command or a group of command can be undo
 */
class UndoStackItem extends CytomineDomain {

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

    /**
     * Flag that indicate if command comes from redo stack (true) or is a new command (false)
     */
    boolean isFromRedo = false

    static belongsTo = [user: User, command: Command]

    static mapping = {
        sort "id"
        user index: 'undostackitem_user_index'
    }


    static constraints = {
        transaction(nullable: true)
    }

    String toString() {
        return "|user=" + user.id + " command=" + command + " transaction=" + transaction + " isFromRedo=" + isFromRedo
    }
}
