package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.security.User

/**
 * @author ULG-GIGA Cytomine Team
 * The RedoStackItem class allow to store command on a redo stack so that a command or a group of command can be redo
 */
class RedoStackItem extends CytomineDomain implements Comparable {

    /**
     * User who launch command
     */
    User user

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

    int compareTo(obj) {
        created.compareTo(obj.created)
    }
}
