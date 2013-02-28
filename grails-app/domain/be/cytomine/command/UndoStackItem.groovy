package be.cytomine.command

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
