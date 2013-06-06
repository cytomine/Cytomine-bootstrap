package be.cytomine.utils.database

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

    /**
     * Create domain index
     */
    def initIndex() {
        sessionFactory.getCurrentSession().clear();
        def connection = sessionFactory.currentSession.connection()

        try {
            def statement = connection.createStatement()

            /**
             * Abastract Image //already created (via unique) on filename
             */
            createIndex(statement, "abstract_image", "sample_id");
            createIndex(statement, "abstract_image", "created");
            /**
             * Abstract_Image_group
             */
            createIndex(statement, "abstract_image_group", "abstractimage_id");
            createIndex(statement, "abstract_image_group", "group_id");
            /**
             * Image_Instance //base image & project already created
             */
            createIndex(statement, "image_instance", "user_id");
            createIndex(statement, "image_instance", "created");
            /**
             * Annotation
             */
            createIndex(statement, "user_annotation", "image_id");
            createIndex(statement, "user_annotation", "user_id");
            createIndex(statement, "user_annotation", "created");
            createIndex(statement, "user_annotation", "project_id");
            createIndex(statement, "user_annotation", "location", "GIST");

            createIndex(statement, "algo_annotation", "image_id");
            createIndex(statement, "algo_annotation", "user_id");
            createIndex(statement, "algo_annotation", "created");
            createIndex(statement, "algo_annotation", "project_id");
            createIndex(statement, "algo_annotation", "location", "GIST");

            /**
             * ReviewedAnnotation
             */
            createIndex(statement, "reviewed_annotation", "project_id");
            createIndex(statement, "reviewed_annotation", "user_id");
            createIndex(statement, "reviewed_annotation", "image_id");  //GIST
            createIndex(statement, "reviewed_annotation", "location", "GIST");

            /**
             * Annotation_term
             */
            createIndex(statement, "annotation_term", "user_annotation_id");
            createIndex(statement, "annotation_term", "term_id");

            /**
             * Algo_annotation_term
             */
            createIndex(statement,"algo_annotation_term","annotation_ident")
            createIndex(statement,"algo_annotation_term","project_id")
            createIndex(statement,"algo_annotation_term","rate")
            createIndex(statement,"algo_annotation_term","term_id")
            createIndex(statement,"algo_annotation_term","user_job_id")


            /**
             * relation_term
             */
            createIndex(statement, "relation_term", "relation_id");
            createIndex(statement, "relation_term", "term1_id");
            createIndex(statement, "relation_term", "term2_id");
            /**
             * ProjectGroup
             */
            createIndex(statement, "project_group", "project_id");
            createIndex(statement, "project_group", "group_id");
            /**
             * Sample
             */
            createIndex(statement, "sample", "name");
            createIndex(statement, "sample", "created");
            /**
             * Use_group
             */
            createIndex(statement, "user_group", "user_id");
            createIndex(statement, "user_group", "group_id");

            /**
             * Command
             */
            createIndex(statement, "command", "user_id");
            createIndex(statement, "command", "project_id");
            createIndex(statement, "command", "created");

            /**
             * CommandHistory
             */
            createIndex(statement, "command_history", "project_id");
            createIndex(statement, "command_history", "user_id");
            createIndex(statement, "command_history", "created");
            createIndex(statement, "command_history", "command_id");
            /**
             * Term
             */
            createIndex(statement, "term", "ontology_id");

            createIndex(statement, "annotation_index", "image_id");
            createIndex(statement, "annotation_index", "user_id");


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
    def createIndex(def statement, String table, String col) {
        createIndex(statement,table,col,"btree");
    }

    /**
     * Create an index (various type: BTREE, HASH, GIST,...)
     * @param statement Database statement
     * @param table Table for index
     * @param col Column for index
     * @param type Index structure type (BTREE, HASH, GIST,...)
     */
    def createIndex(def statement, String table, String col, String type) {
        String name = table + "_" + col + "_index"
        String reqcreate = "CREATE INDEX " + name + " ON " + table + " USING $type (" + col + ");"
        log.info reqcreate
        try {statement.execute(reqcreate); } catch(Exception e) { log.info "Cannot create index $name="+e}
    }

}
