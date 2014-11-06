package be.cytomine.utils

import groovy.sql.Sql

/**
 * Comment for a task
 * Example: comment 1: "Start task...", comment 2: "Delete project data", ...
 * THIS CLASS CANNOT BE A DOMAIN! Because it cannot works with hibernate transaction.
 */
class TaskComment {


    Long taskIdent

    String comment

    Long timestamp

}
