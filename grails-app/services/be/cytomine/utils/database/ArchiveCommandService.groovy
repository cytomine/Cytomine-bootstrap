package be.cytomine.utils.database

import be.cytomine.SecurityACL
import grails.util.Environment
import groovy.sql.Sql

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/07/11
 * Time: 15:16
 * To change this template use File | Settings | File Templates.
 */
class ArchiveCommandService {
    def cytomineService
    def sessionFactory
    def grailsApplication
    def commandService
    static transactional = false
    def dataSource

    public def archiveOldCommand() {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        Date before = getMonthBefore(new Date(), 1)
        archive(before)
    }

    public def archive(Date before) {
        SecurityACL.checkAdmin(cytomineService.currentUser)
        Date today = new Date()
        File directory = new File("oldcommand/${Environment.getCurrent()}")
        def subdirectory = new File(directory.absolutePath)
        if (!subdirectory.exists()) {
            subdirectory.mkdirs()
        }
        int i = 0
        def total
        def request = "select count(id) from command_history where extract(epoch from created)*1000 < ${before.getTime()}"
        println request
        new Sql(dataSource).eachRow(request) {
            total = it[0]
        }
        println "TOTAL=$total"
        request = "SELECT command.id || ';' || extract(epoch from command.created) || ';' || command_history.prefix_action || ';'  || command.action_message || ';' ||  command.user_id || ';' || command_history.project_id \n" +
                "FROM command, command_history\n" +
                "WHERE command_history.command_id = command.id\n" +
                "AND extract(epoch from command.created)*1000 < ${before.getTime()} order by command.id asc"
        println request
        new Sql(dataSource).eachRow(request) {

            if (i % 10000 == 0) {
                println "$i/$total"
            }
            new File(subdirectory.absolutePath + "/${today.year}-${today.month+1}-${today.date}.log").append(it[0]+"\n")
            i++
        }
        request = "delete from command_history where extract(epoch from created)*1000 < ${before.getTime()}"
        println request
        new Sql(dataSource).execute(request)
        request = "delete from undo_stack_item where extract(epoch from created)*1000 < ${before.getTime()}"
        println request
        new Sql(dataSource).execute(request)
        request = "delete from redo_stack_item"
         println request
         new Sql(dataSource).execute(request)
        request = "delete from command where extract(epoch from created)*1000 < ${before.getTime()-10000}"
         println request
         new Sql(dataSource).execute(request)
    }

    /**
     * Clean GORM cache
     */
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    public void cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

    static Date getMonthBefore(Date date, int month) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, -month);  // number of days to add
        def before = c.getTime();  // dt is now the new date
        return before
    }


}
