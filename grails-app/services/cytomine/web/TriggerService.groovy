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
            statement.execute(getImageAnnotationCountTriggerIncr())
            statement.execute(getImageAnnotationCountTriggerDecr())
            statement.execute(getAnnotationCommentTriggerIncr())

        } catch (org.postgresql.util.PSQLException e) {
            log.info e
        }

    }

    String getAnnotationCommentTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementAnnotationComment() RETURNS trigger as '
        BEGIN UPDATE annotation SET count_comments = count_comments + 1 WHERE id = NEW.annotation_id; RETURN NEW; END ;'
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
        DECLARE
            current_class sec_user.class%TYPE;
            user_class sec_user.class%TYPE := 'be.cytomine.security.User';
            job_class sec_user.class%TYPE := 'be.cytomine.security.UserJob';
        BEGIN
		    SELECT class INTO current_class from sec_user where id = NEW.user_id;
            IF current_class = user_class THEN
                UPDATE project
                SET count_annotations = count_annotations + 1
                FROM image_instance
                WHERE project.id = image_instance.project_id
                AND image_instance.id = NEW.image_id;
            ELSEIF current_class = job_class THEN
                UPDATE project
                SET count_job_annotations = count_job_annotations + 1
                FROM image_instance
                WHERE project.id = image_instance.project_id
                AND image_instance.id = NEW.image_id;
            END IF;
            RETURN NEW;
        END ;
        \$incProjAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAnnotationProject on annotation;"

        String createTrigger = "CREATE TRIGGER countAnnotationProject AFTER INSERT ON annotation FOR EACH ROW EXECUTE PROCEDURE incrementProjectAnnotation(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectAnnotationCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementProjectAnnotation() RETURNS TRIGGER AS \$decProjAnn\$
        DECLARE
            current_class sec_user.class%TYPE;
            user_class sec_user.class%TYPE := 'be.cytomine.security.User';
            job_class sec_user.class%TYPE := 'be.cytomine.security.UserJob';
        BEGIN
		SELECT class INTO current_class from sec_user where id = OLD.user_id;
		IF current_class = user_class THEN
            UPDATE project
            SET count_annotations = count_annotations - 1
            FROM image_instance
            WHERE project.id = image_instance.project_id
            AND image_instance.id = OLD.image_id;
		ELSEIF current_class = job_class THEN
            UPDATE project
            SET count_job_annotations = count_job_annotations - 1
            FROM image_instance
            WHERE project.id = image_instance.project_id
            AND image_instance.id = OLD.image_id;
		END IF;
		RETURN OLD;
        END ;
         \$decProjAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationProject on annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationProject AFTER DELETE ON annotation FOR EACH ROW EXECUTE PROCEDURE decrementProjectAnnotation(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getImageAnnotationCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementImageAnnotation() RETURNS trigger as \$incImageAnn\$
        DECLARE
            current_class sec_user.class%TYPE;
            user_class sec_user.class%TYPE := 'be.cytomine.security.User';
            job_class sec_user.class%TYPE := 'be.cytomine.security.UserJob';
        BEGIN
        SELECT class INTO current_class from sec_user where id = NEW.user_id;
        IF current_class = user_class THEN
            UPDATE image_instance
            SET count_image_annotations = count_image_annotations + 1
            WHERE image_instance.id = NEW.image_id;
        ELSEIF current_class = job_class THEN
            UPDATE image_instance
            SET count_image_job_annotations = count_image_job_annotations + 1
            WHERE image_instance.id = NEW.image_id;
        END IF;
        RETURN NEW;
        END ;
        \$incImageAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAnnotationImage on annotation;"

        String createTrigger = "CREATE TRIGGER countAnnotationImage AFTER INSERT ON annotation FOR EACH ROW EXECUTE PROCEDURE incrementImageAnnotation(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getImageAnnotationCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementImageAnnotation() RETURNS trigger as \$decImageAnn\$
        DECLARE
            current_class sec_user.class%TYPE;
            user_class sec_user.class%TYPE := 'be.cytomine.security.User';
            job_class sec_user.class%TYPE := 'be.cytomine.security.UserJob';
        BEGIN
        SELECT class INTO current_class from sec_user where id = OLD.user_id;
        IF current_class = user_class THEN
            UPDATE image_instance
            SET count_image_annotations = count_image_annotations - 1
            WHERE image_instance.id = OLD.image_id;
        ELSEIF current_class = job_class THEN
            UPDATE image_instance
            SET count_image_job_annotations = count_image_job_annotations - 1
            WHERE image_instance.id = OLD.image_id;
        END IF;
        RETURN OLD;

        END ;
        \$decImageAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationImage on annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationImage AFTER DELETE ON annotation FOR EACH ROW EXECUTE PROCEDURE decrementImageAnnotation(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }
}
