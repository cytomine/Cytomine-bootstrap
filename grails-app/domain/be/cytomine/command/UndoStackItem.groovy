package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.security.User

class UndoStackItem extends CytomineDomain implements Comparable {
    User user
    Command command
    Boolean transactionInProgress
    int transaction
    boolean isFromRedo = false //the undo item come from redo stack

    static belongsTo = [user: User, command: Command]

    static mapping = {
        user index: 'undostackitem_user_index'
    }

    int compareTo(obj) {
        created.compareTo(obj.created)
    }

    static constraints = {
        isFromRedo(nullable: true)
    }

    String toString() { return "|user=" + user.id + " command=" + command + " transaction=" + transactionInProgress + " isFromRedo=" + isFromRedo}
}
