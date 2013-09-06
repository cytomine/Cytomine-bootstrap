package be.cytomine.utils.database

import groovy.sql.Sql

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/07/11
 * Time: 15:16
 * Service used to create index at the application begining
 */
class TableService {

    def sessionFactory
    def dataSource
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


            String reqcreate

            reqcreate = "CREATE VIEW user_project AS\n" +
                                "SELECT distinct project.*, sec_user.id as user_id\n" +
                                "FROM project, acl_object_identity, sec_user, acl_sid, acl_entry \n" +
                                "WHERE project.id = acl_object_identity.object_id_identity\n" +
                                "AND acl_sid.sid = sec_user.username\n" +
                                "AND acl_entry.sid = acl_sid.id\n" +
                                "AND acl_entry.acl_object_identity = acl_object_identity.id\n" +
                                "AND sec_user.user_id is null\n" +
                                "AND mask >= 1"
            createRequest('user_project',reqcreate)

            reqcreate = "CREATE VIEW admin_project AS\n" +
                                "SELECT distinct project.*, sec_user.id as user_id\n" +
                                "FROM project, acl_object_identity, sec_user, acl_sid, acl_entry \n" +
                                "WHERE project.id = acl_object_identity.object_id_identity\n" +
                                "AND acl_sid.sid = sec_user.username\n" +
                                "AND acl_entry.sid = acl_sid.id\n" +
                                "AND acl_entry.acl_object_identity = acl_object_identity.id\n" +
                                "AND sec_user.user_id is null\n" +
                                "AND mask >= 16"
            createRequest('admin_project',reqcreate)

            reqcreate = "CREATE VIEW creator_project AS\n" +
                                "SELECT distinct project.*, sec_user.id as user_id\n" +
                                "FROM project, acl_object_identity, sec_user, acl_sid\n" +
                                "WHERE project.id = acl_object_identity.object_id_identity\n" +
                                "AND acl_sid.sid = sec_user.username\n" +
                                "AND acl_object_identity.owner_sid = acl_sid.id\n" +
                                "AND sec_user.user_id is null"
            createRequest('creator_project',reqcreate)

            reqcreate = "CREATE VIEW user_image AS\n" +
                    "SELECT distinct image_instance.*, abstract_image.filename, abstract_image.original_filename, project.name as project_name, sec_user.id as user_image_id\n" +
                    "FROM project,  image_instance, abstract_image, acl_object_identity, sec_user, acl_sid, acl_entry \n" +
                    "WHERE project.id = acl_object_identity.object_id_identity\n" +
                    "AND image_instance.project_id = project.id \n" +
                    "AND abstract_image.id = image_instance.base_image_id\n" +
                    "AND acl_sid.sid = sec_user.username\n" +
                    "AND acl_entry.sid = acl_sid.id\n" +
                    "AND acl_entry.acl_object_identity = acl_object_identity.id\n" +
                    "AND sec_user.user_id is null\n" +
                    "AND mask >= 1"
            createRequest('user_image',reqcreate)

        } catch (org.postgresql.util.PSQLException e) {
            log.info e
        }
    }

    def createRequest(def name,def reqcreate) {
        try {

            boolean alreadyExist = false

            new Sql(dataSource).eachRow("select table_name from INFORMATION_SCHEMA.views where table_name like ?",[name]) {
                alreadyExist = true
            }

            if(alreadyExist) {
                println "DROP VIEW $name"
                new Sql(dataSource).execute("DROP VIEW $name")

            }
            log.info reqcreate
            new Sql(dataSource).execute(reqcreate)


        } catch(Exception e) {
            println e
        }
    }
}
