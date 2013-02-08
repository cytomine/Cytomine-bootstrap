package be.cytomine.utils

import groovy.sql.Sql
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import grails.util.Holders

/**
 * A task provide info about a command.
 * The main info is the progress status
 * THIS CLASS CANNOT BE A DOMAIN! Because it cannot works with hibernate transaction.
 */
class Task {

    Long id
    /**
     * Request progress between 0 and 100
     */
    int progress = 0

    /**
     * Project updated by the command task
     */
    Long projectIdent = -1

    /**
     * User that ask the task
     */
    Long userIdent


    def sequenceService


    def getMap() {
        def map = [:]
        map.id = id
        map.progress = progress
        map.project = projectIdent
        map.user = userIdent
        map.comments = getLastComments(5)
        return map
    }

    def getLastComments(int max) {
        //sql request retrieve n last comments for task
        def data = []
        new Sql(Holders.grailsApplication.mainContext.dataSource).eachRow("SELECT comment FROM task_comment where taskIdent = ${id} order by timestamp desc limit $max") {
            data << it[0]
        }
        data
    }

    Task saveOnDatabase() {
        println AH.application.mainContext.dataSource
        boolean isAlreadyInDatabase = false
        new Sql(Holders.grailsApplication.mainContext.dataSource).eachRow("SELECT id FROM task where id = ${id}") {
            isAlreadyInDatabase = true
        }

        if(!isAlreadyInDatabase) {
            id = AH.application.mainContext.sequenceService.generateID()
            new Sql(Holders.grailsApplication.mainContext.dataSource).executeInsert("INSERT INTO task (id,progress,projectIdent,userIdent) VALUES ($id,$progress,$projectIdent,$userIdent)")
        } else {
            new Sql(Holders.grailsApplication.mainContext.dataSource).executeInsert("UPDATE task set progress=${progress} WHERE id=$id")
        }
        getFromDatabase(id)

    }

    def getFromDatabase(def id) {
        Task task = null
        new Sql(Holders.grailsApplication.mainContext.dataSource).eachRow("SELECT id,progress,projectIdent,userIdent FROM task where id = ${id}") {
            task = new Task()
            task.id = it[0]
            task.progress = it[1]
            task.projectIdent = it[2]
            task.userIdent = it[3]
        }
        return task
    }

    def addComment(String comment) {
        if(comment!=null && !comment.equals("")) {
            TaskComment taskComment = new TaskComment(taskIdent: id,comment: comment,timestamp: new Date().getTime())
            taskComment.saveOnDatabase()
        }
    }

}
