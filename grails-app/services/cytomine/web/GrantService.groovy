package cytomine.web

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/07/11
 * Time: 15:16
 * To change this template use File | Settings | File Templates.
 */
class GrantService {

    def sessionFactory
    def grailsApplication
    static transactional = true

    def initGrant() {
        sessionFactory.getCurrentSession().clear();
        def connection = sessionFactory.currentSession.connection()

        try {
            def statement = connection.createStatement()


            statement.execute(getGrantInfo())

        } catch (org.postgresql.util.PSQLException e) {
            log.info e
        }

    }

    String getGrantInfo() {
        String createroot = "create user root with password 'root';"
        String createsudo = "create user sudo with password 'sudo';"
        String grantroot = "GRANT postgres TO root;"
        String grantsudo = "GRANT postgres TO sudo;"
        return createroot + createsudo + grantroot + grantsudo
    }
}
