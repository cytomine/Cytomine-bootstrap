package be.cytomine.command

import be.cytomine.CytomineDomain
import be.cytomine.security.User

class RedoStackItem extends CytomineDomain implements Comparable {
    User user
    Command command
    Boolean transactionInProgress
    int transaction

    static belongsTo = [user: User, command: Command]

    int compareTo(obj) {
        created.compareTo(obj.created)
    }
}
