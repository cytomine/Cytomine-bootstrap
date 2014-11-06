package be.cytomine.utils

import grails.util.Holders
import groovy.sql.Sql

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

    boolean printInActivity = false


    def dataSource


    def sequenceService


    def getMap(taskService) {
        def map = [:]
        map.id = id
        map.progress = progress
        map.project = projectIdent
        map.user = userIdent
        map.printInActivity = printInActivity
        map.comments = taskService.getLastComments(this,5)
        return map
    }

}
