package be.cytomine.utils.database

/**
 * Sequence service provide new id for domain
 */
class SequenceService {

    def sessionFactory
    public final static String SEQ_NAME = "hibernate_sequence"
    static transactional = true

    /**
     * Create database sequence
     */
    def initSequences() {
        sessionFactory.getCurrentSession().clear();
        def connection = sessionFactory.currentSession.connection()

        try {
            def statement = connection.createStatement()
            def dropSequenceQuery = ""//"DROP SEQUENCE IF EXISTS "+SEQ_NAME+";"
            def createSequenceQuery = "CREATE SEQUENCE " + SEQ_NAME + " START 1;"
            statement.execute(dropSequenceQuery + createSequenceQuery)
        } catch (org.postgresql.util.PSQLException e) {
            log.debug e
        }

    }

    /**
     * Get a new id number
     */
    def generateID() {
        def statement = sessionFactory.currentSession.connection().createStatement()
        def res = statement.executeQuery("select nextval('" + SEQ_NAME + "');")
        res.next()
        Long nextVal = res.getLong("nextval")
        return nextVal
    }
}
