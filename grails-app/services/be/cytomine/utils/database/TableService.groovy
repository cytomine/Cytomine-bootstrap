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

        //drop constraint (for perf)
        dropForeignKeys()

        try {

            if(executeSimpleRequest("select character_maximum_length from information_schema.columns where table_name = 'command' and column_name = 'data'")!=null) {
                log.info "Change type..."
                new Sql(dataSource).executeUpdate("alter table command alter column data type character varying")
            }

            if(executeSimpleRequest("select character_maximum_length from information_schema.columns where table_name = 'shared_annotation' and column_name = 'comment'")!=null) {
                log.info "Change type..."
                new Sql(dataSource).executeUpdate("alter table shared_annotation alter column comment type character varying")
            }

            if(executeSimpleRequest("select character_maximum_length from information_schema.columns where table_name = 'property' and column_name = 'value'")!=null) {
                log.info "Change type property table..."
                new Sql(dataSource).executeUpdate("alter table property alter column value type character varying")
            }

            String reqcreate

            reqcreate = "CREATE VIEW user_project AS\n" +
                                "SELECT distinct project.*, sec_user.id as user_id\n" +
                                "FROM project, acl_object_identity, sec_user, acl_sid, acl_entry \n" +
                                "WHERE project.id = acl_object_identity.object_id_identity\n" +
                                "AND acl_sid.sid = sec_user.username\n" +
                                "AND acl_entry.sid = acl_sid.id\n" +
                                "AND acl_entry.acl_object_identity = acl_object_identity.id\n" +
                                "AND sec_user.user_id is null\n" +
                                "AND mask >= 1 AND project.deleted IS NULL"
            createRequest('user_project',reqcreate)

            reqcreate = "CREATE VIEW admin_project AS\n" +
                                "SELECT distinct project.*, sec_user.id as user_id\n" +
                                "FROM project, acl_object_identity, sec_user, acl_sid, acl_entry \n" +
                                "WHERE project.id = acl_object_identity.object_id_identity\n" +
                                "AND acl_sid.sid = sec_user.username\n" +
                                "AND acl_entry.sid = acl_sid.id\n" +
                                "AND acl_entry.acl_object_identity = acl_object_identity.id\n" +
                                "AND sec_user.user_id is null\n" +
                                "AND mask >= 16 AND project.deleted IS NULL"
            createRequest('admin_project',reqcreate)

            reqcreate = "CREATE VIEW creator_project AS\n" +
                                "SELECT distinct project.*, sec_user.id as user_id\n" +
                                "FROM project, acl_object_identity, sec_user, acl_sid\n" +
                                "WHERE project.id = acl_object_identity.object_id_identity\n" +
                                "AND acl_sid.sid = sec_user.username\n" +
                                "AND acl_object_identity.owner_sid = acl_sid.id\n" +
                                "AND sec_user.user_id is null AND project.deleted IS NULL"
            createRequest('creator_project',reqcreate)

            reqcreate = "CREATE VIEW user_image AS\n" +
                    "SELECT distinct image_instance.*, abstract_image.filename, abstract_image.original_filename, project.name as project_name, sec_user.id as user_image_id\n" +
                    "FROM project,  image_instance, abstract_image, acl_object_identity, sec_user, acl_sid, acl_entry \n" +
                    "WHERE project.id = acl_object_identity.object_id_identity\n" +
                    "AND image_instance.deleted IS NULL \n" +
                    "AND project.deleted IS NULL \n" +
                    "AND image_instance.project_id = project.id \n" +
                    "AND image_instance.parent_id IS NULL \n" + //don't get nested images
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

    def executeSimpleRequest(String request) {
        def response = null
        log.info "request = $request"
        new Sql(dataSource).eachRow(request) {
            log.info it[0]
            response = it[0]
        }
        log.info "response = $response"
        response
    }

    def createRequest(def name,def reqcreate) {
        try {

            boolean alreadyExist = false

            new Sql(dataSource).eachRow("select table_name from INFORMATION_SCHEMA.views where table_name like ?",[name]) {
                alreadyExist = true
            }

            if(alreadyExist) {
                def req =  "DROP VIEW " + name
                new Sql(dataSource).execute(req)

            }
            log.info reqcreate
            new Sql(dataSource).execute(reqcreate)


        } catch(Exception e) {
            log.error e
        }
    }

//    def dropConstraint(String table, String constraint) {
//
//    }



    def dropForeignKeys() {
        def response = null
        String request = """
            SELECT
                tc.constraint_name, tc.table_name, kcu.column_name,
                ccu.table_name AS foreign_table_name,
                ccu.column_name AS foreign_column_name
            FROM
                information_schema.table_constraints AS tc
                JOIN information_schema.key_column_usage AS kcu
                  ON tc.constraint_name = kcu.constraint_name
                JOIN information_schema.constraint_column_usage AS ccu
                  ON ccu.constraint_name = tc.constraint_name
            WHERE constraint_type = 'FOREIGN KEY' AND (tc.table_name='last_connection');

        """
        log.info "request = $request"
        new Sql(dataSource).eachRow(request) {
            def sql = new Sql(dataSource)
            try {
                String table = it[1]
                String constraint = it[0]
                String req = "ALTER TABLE $table DROP CONSTRAINT $constraint"
                log.info "drop constraint $constraint => $req"
                sql.executeUpdate(req)
                sql.close()
            }catch (Exception e) {}
        }
        log.info "response = $response"
        response
    }
}
