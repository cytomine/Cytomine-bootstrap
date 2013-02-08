package be.cytomine.utils


import groovy.sql.Sql
import grails.util.Holders

/**
 * Comment for a task
 * Example: comment 1: "Start task...", comment 2: "Delete project data", ...
 * THIS CLASS CANNOT BE A DOMAIN! Because it cannot works with hibernate transaction.
 */
class TaskComment {


    Long taskIdent

    String comment

    Long timestamp

    def saveOnDatabase() {
        new Sql(Holders.grailsApplication.mainContext.dataSource).executeInsert("INSERT INTO task_comment (taskIdent,comment,timestamp) VALUES ($taskIdent,$comment,$timestamp)")
    }
}
