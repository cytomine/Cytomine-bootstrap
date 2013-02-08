package be.cytomine.utils.database

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/07/11
 * Time: 15:16
 * Service used to create index at the application begining
 */
class TableService {

    def sessionFactory
    def grailsApplication
    public final static String SEQ_NAME = "CYTOMINE_SEQ"
    static transactional = true

    /**
     * Create domain index
     */
    def initTable() {
        sessionFactory.getCurrentSession().clear();
        def connection = sessionFactory.currentSession.connection()

        try {
            def statement = connection.createStatement()
            createTaskTable(statement)
            createTaskCommentTable(statement)
        } catch (org.postgresql.util.PSQLException e) {
            log.info e
        }
    }

    def createTaskTable(def statement) {
        String reqcreate =  "CREATE TABLE task (id bigint,progress int, projectIdent bigint,userIdent bigint);"
        log.info reqcreate
        try {statement.execute(reqcreate); } catch(Exception e) { log.info "Cannot create TABLE="+e}
    }

    def createTaskCommentTable(def statement) {
        String reqcreate =  "CREATE TABLE task_comment (taskIdent bigint,comment varchar(255), timestamp bigint);"
        log.info reqcreate
        try {statement.execute(reqcreate); } catch(Exception e) { log.info "Cannot create TABLE="+e}

    }



}
