package be.cytomine.utils.database

import groovy.sql.Sql

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/07/11
 * Time: 15:16
 * Service used to create index at the application begining
 */
class IndexService {

    def sessionFactory
    def grailsApplication
    public final static String SEQ_NAME = "CYTOMINE_SEQ"
    static transactional = true
    def dataSource

    /**
     * Create domain index
     */
    def initIndex() {

        try {

            /**
             * Abastract Image //already created (via unique) on filename
             */
            createIndex("abstract_image", "sample_id");
            createIndex("abstract_image", "created");
            /**
             * Image_Instance //base image & project already created
             */
            createIndex("image_instance", "user_id");
            createIndex("image_instance", "created");
            /**
             * Annotation
             */
            createIndex("user_annotation", "image_id");
            createIndex("user_annotation", "user_id");
            createIndex("user_annotation", "created");
            createIndex("user_annotation", "project_id");
            createIndex("user_annotation", "location", "GIST");

            createIndex("algo_annotation", "image_id");
            createIndex("algo_annotation", "user_id");
            createIndex("algo_annotation", "created");
            createIndex("algo_annotation", "project_id");
            createIndex("algo_annotation", "location", "GIST");

            /**
             * ReviewedAnnotation
             */
            createIndex("reviewed_annotation", "project_id");
            createIndex("reviewed_annotation", "user_id");
            createIndex("reviewed_annotation", "image_id");  //GIST
            createIndex("reviewed_annotation", "location", "GIST");

            /**
             * Annotation_term
             */
            createIndex("annotation_term", "user_annotation_id");
            createIndex("annotation_term", "term_id");

            /**
             * Algo_annotation_term
             */
            createIndex("algo_annotation_term","annotation_ident")
            createIndex("algo_annotation_term","project_id")
            createIndex("algo_annotation_term","rate")
            createIndex("algo_annotation_term","term_id")
            createIndex("algo_annotation_term","user_job_id")


            /**
             * relation_term
             */
            createIndex("relation_term", "relation_id");
            createIndex("relation_term", "term1_id");
            createIndex("relation_term", "term2_id");

            /**
             * Sample
             */
            createIndex("sample", "name");
            createIndex("sample", "created");
            /**
             * Use_group
             */
            createIndex("user_group", "user_id");
            createIndex("user_group", "group_id");

            /**
             * Command
             */
            createIndex("command", "user_id");
            createIndex("command", "project_id");
            createIndex("command", "created");

            /**
             * CommandHistory
             */
            createIndex("command_history", "project_id");
            createIndex("command_history", "user_id");
            createIndex("command_history", "created");
            createIndex("command_history", "command_id");

            createIndex("undo_stack_item", "command_id");
            createIndex("redo_stack_item", "command_id");

            /**
             * Term
             */
            createIndex("term", "ontology_id");

            createIndex("storage", "user_id");

            createIndex("acl_object_identity", "object_id_identity");
            createIndex("acl_entry", "acl_object_identity");
            createIndex("acl_sid", "sid");

            createIndex("annotation_index", "image_id");
            createIndex("annotation_index", "user_id");

            createIndex("user_position", "user_id");
            createIndex("user_position", "updated");
            createIndex("user_position", "image_id");
            createIndex("user_position", "project_id");
            createIndex("user_position", "latitude");
            createIndex("user_position", "longitude");
            createIndex("user_position", "date_part('epoch'::text, created)","btree", "user_position_epochcreated");
            createIndex("user_position", "date_part('epoch'::text, updated)","btree", "user_position_epochupdated");
            createIndex("user_position", "created","btree", "user_position_created");
            createIndex("user_position", "updated","btree", "user_position_updated");

        } catch (org.postgresql.util.PSQLException e) {
            log.info e
        }
    }

    /**
     * Create Btree index
     * @param statement Database statement
     * @param table Table for index
     * @param col Column for index
     */
    def createIndex(String table, String col) {
        createIndex(table,col,"btree",null);
    }

    /**
     * Create an index (various type: BTREE, HASH, GIST,...)
     * @param statement Database statement
     * @param table Table for index
     * @param col Column for index
     * @param type Index structure type (BTREE, HASH, GIST,...)
     */
    def createIndex(String table, String col, String type, String overidedname = null) {
        String name = overidedname? overidedname : table + "_" + col + "_index"

        boolean alreadyExist = false

        new Sql(dataSource).eachRow("select indexname from pg_indexes where indexname like ?",[name]) {
            alreadyExist = true
        }

        //try {statement.execute(reqcreate); } catch(Exception e) { log.info "Cannot create index $name="+e}
        try {
            if(alreadyExist) {
                log.info "$name already exist, don't create it"
            } else {
                String reqcreate = "CREATE INDEX " + name + " ON " + table + " USING $type (" + col + ");"
                log.info reqcreate
                new Sql(dataSource).execute(reqcreate)
            }

        } catch(Exception e) {
            log.error e
        }
    }

}
