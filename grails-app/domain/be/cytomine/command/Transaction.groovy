package be.cytomine.command

import be.cytomine.CytomineDomain

/**
 * @author lrollus
 * A transaction allow to group command. It allow to undo/redo multiple command (e.g. add annotation x + add term y to x = 2 commands)
 * Its a long number generated with sequence (thread-safe)
 */
class Transaction extends CytomineDomain {

}
