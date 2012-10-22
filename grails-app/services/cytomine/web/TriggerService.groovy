package cytomine.web

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/07/11
 * Time: 15:16
 * To change this template use File | Settings | File Templates.
 */
class TriggerService {

    def sessionFactory
    def grailsApplication
    public final static String SEQ_NAME = "CYTOMINE_SEQ"
    static transactional = true

    def initTrigger() {
        sessionFactory.getCurrentSession().clear();
        def connection = sessionFactory.currentSession.connection()

        try {
            def statement = connection.createStatement()

            statement.execute(getProjectImageCountTriggerIncr())
            statement.execute(getProjectImageCountTriggerDecr())
            statement.execute(getProjectAnnotationCountTriggerIncr())
            statement.execute(getProjectAnnotationCountTriggerDecr())
            statement.execute(getProjectAnnotationAlgoCountTriggerIncr())
            statement.execute(getProjectAnnotationAlgoCountTriggerDecr())
            statement.execute(getImageAnnotationCountTriggerIncr())
            statement.execute(getImageAnnotationCountTriggerDecr())
            statement.execute(getImageAnnotationAlgoCountTriggerIncr())
            statement.execute(getImageAnnotationAlgoCountTriggerDecr())
            statement.execute(getAnnotationCommentTriggerIncr())

        } catch (org.postgresql.util.PSQLException e) {
            log.info e
        }

    }

    String getAnnotationCommentTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementAnnotationComment() RETURNS trigger as '
        BEGIN UPDATE user_annotation SET count_comments = count_comments + 1 WHERE id = NEW.user_annotation_id; RETURN NEW; END ;'
        LANGUAGE plpgsql;"""

        String dropTrigger = "DROP TRIGGER IF EXISTS countIncrementAnnotationComment on shared_annotation;"

        String createTrigger = "CREATE TRIGGER countIncrementAnnotationComment AFTER INSERT ON shared_annotation FOR EACH ROW EXECUTE PROCEDURE incrementAnnotationComment();"


        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectImageCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementProjectImage() RETURNS trigger as '
        BEGIN UPDATE project SET count_images = count_images + 1 WHERE id = NEW.project_id; RETURN NEW; END ;'
        LANGUAGE plpgsql;"""

        String dropTrigger = "DROP TRIGGER IF EXISTS countImageProject on image_instance;"

        String createTrigger = "CREATE TRIGGER countImageProject AFTER INSERT ON image_instance FOR EACH ROW EXECUTE PROCEDURE incrementProjectImage();"


        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectImageCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementProjectImage() RETURNS trigger as '
        BEGIN UPDATE project SET count_images = count_images - 1 WHERE id = OLD.project_id; RETURN OLD; END ;'
        LANGUAGE plpgsql;"""

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrImageProject on image_instance;"

        String createTrigger = "CREATE TRIGGER countDecrImageProject AFTER DELETE ON image_instance FOR EACH ROW EXECUTE PROCEDURE decrementProjectImage();"

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectAnnotationCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementProjectAnnotation() RETURNS TRIGGER AS \$incProjAnn\$
        BEGIN
            UPDATE project
            SET count_annotations = count_annotations + 1
            FROM image_instance
            WHERE project.id = image_instance.project_id
            AND image_instance.id = NEW.image_id;
            RETURN NEW;
        END ;
        \$incProjAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAnnotationProject on user_annotation;"

        String createTrigger = "CREATE TRIGGER countAnnotationProject AFTER INSERT ON user_annotation FOR EACH ROW EXECUTE PROCEDURE incrementProjectAnnotation(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectAnnotationAlgoCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementProjectAnnotationAlgo() RETURNS TRIGGER AS \$incProjAnnAlgo\$
        BEGIN
            UPDATE project
            SET count_job_annotations = count_job_annotations + 1
            FROM image_instance
            WHERE project.id = image_instance.project_id
            AND image_instance.id = NEW.image_id;
            RETURN NEW;
        END ;
        \$incProjAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAlgoAnnotationProject on algo_annotation;"

        String createTrigger = "CREATE TRIGGER countAlgoAnnotationProject AFTER INSERT ON algo_annotation FOR EACH ROW EXECUTE PROCEDURE incrementProjectAnnotationAlgo(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }



    String getProjectAnnotationCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementProjectAnnotation() RETURNS TRIGGER AS \$decProjAnn\$
        BEGIN
            UPDATE project
            SET count_annotations = count_annotations - 1
            FROM image_instance
            WHERE project.id = image_instance.project_id
            AND image_instance.id = OLD.image_id;
		    RETURN OLD;
        END ;
         \$decProjAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationProject on user_annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationProject AFTER DELETE ON user_annotation FOR EACH ROW EXECUTE PROCEDURE decrementProjectAnnotation(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectAnnotationAlgoCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementProjectAnnotationAlgo() RETURNS TRIGGER AS \$decProjAnnAlgo\$
        BEGIN
            UPDATE project
            SET count_job_annotations = count_job_annotations - 1
            FROM image_instance
            WHERE project.id = image_instance.project_id
            AND image_instance.id = OLD.image_id;
		    RETURN OLD;
        END ;
         \$decProjAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationProjectAlgo on user_annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationProjectAlgo AFTER DELETE ON user_annotation FOR EACH ROW EXECUTE PROCEDURE decrementProjectAnnotationAlgo(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }



    String getImageAnnotationCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementImageAnnotation() RETURNS trigger as \$incImageAnn\$
        BEGIN
            UPDATE image_instance
            SET count_image_annotations = count_image_annotations + 1
            WHERE image_instance.id = NEW.image_id;
            RETURN NEW;
        END ;
        \$incImageAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAnnotationImage on user_annotation;"

        String createTrigger = "CREATE TRIGGER countAnnotationImage AFTER INSERT ON user_annotation FOR EACH ROW EXECUTE PROCEDURE incrementImageAnnotation(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getImageAnnotationAlgoCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementImageAnnotationAlgo() RETURNS trigger as \$incImageAnn\$
        BEGIN
            UPDATE image_instance
            SET count_image_annotations = count_image_annotations + 1
            WHERE image_instance.id = NEW.image_id;
            RETURN NEW;
        END ;
        \$incImageAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAnnotationImageAlgo on algo_annotation;"

        String createTrigger = "CREATE TRIGGER countAnnotationImageAlgo AFTER INSERT ON algo_annotation FOR EACH ROW EXECUTE PROCEDURE incrementImageAnnotationAlgo(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }


    String getImageAnnotationCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementImageAnnotation() RETURNS trigger as \$decImageAnn\$
        BEGIN
            UPDATE image_instance
            SET count_image_annotations = count_image_annotations - 1
            WHERE image_instance.id = OLD.image_id;
        RETURN OLD;

        END ;
        \$decImageAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationImage on user_annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationImage AFTER DELETE ON user_annotation FOR EACH ROW EXECUTE PROCEDURE decrementImageAnnotation(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getImageAnnotationAlgoCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementImageAnnotationAlgo() RETURNS trigger as \$decImageAnn\$
        BEGIN
            UPDATE image_instance
            SET count_image_annotations = count_image_annotations - 1
            WHERE image_instance.id = OLD.image_id;
        RETURN OLD;

        END ;
        \$decImageAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationImageAlgo on algo_annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationImageAlgo AFTER DELETE ON algo_annotation FOR EACH ROW EXECUTE PROCEDURE decrementImageAnnotationAlgo(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

}
