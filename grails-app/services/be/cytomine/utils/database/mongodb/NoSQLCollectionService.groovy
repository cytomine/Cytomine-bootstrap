package be.cytomine.utils.database.mongodb
/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/07/11
 * Time: 15:16
 * Service used to create index at the application begining
 */
class NoSQLCollectionService {

    def sessionFactory
    def grailsApplication
    static transactional = true
    def mongo

    static String databaseName = "cytomine"

    public String getDatabaseName() {
        return grailsApplication.config.grails.mongo.databaseName
    }

    public String getDatabaseFullDetails() {
        return grailsApplication.config.grails.mongo
    }

    public def cleanActivityDB() {
        log.info "Clean data from "+ getDatabaseName()
        def db = mongo.getDB(getDatabaseName())
        db.persistentConnection.drop()
        db.lastConnection.drop()
        db.persistentUserPosition.drop()
        db.lastUserPosition.drop()

    }


}
