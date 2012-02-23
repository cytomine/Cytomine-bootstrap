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
            println e
        }

    }

    String getAnnotationCommentTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementAnnotationComment() RETURNS trigger as '
        BEGIN UPDATE annotation SET count_comments = count_comments + 1 WHERE id = NEW.id; RETURN NEW; END ;'
        LANGUAGE plpgsql;"""

        String dropTrigger = "DROP TRIGGER IF EXISTS incrementAnnotationComment on shared_annotation;"

        String createTrigger = "CREATE TRIGGER incrementAnnotationComment AFTER INSERT ON shared_annotation FOR EACH ROW EXECUTE PROCEDURE incrementAnnotationComment();"


        println createFunction
        println dropTrigger
        println createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectImageCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementProjectImage() RETURNS trigger as '
        BEGIN UPDATE project SET count_images = count_images + 1 WHERE id = NEW.project_id; RETURN NEW; END ;'
        LANGUAGE plpgsql;"""

        String dropTrigger = "DROP TRIGGER IF EXISTS countImageProject on image_instance;"

        String createTrigger = "CREATE TRIGGER countImageProject AFTER INSERT ON image_instance FOR EACH ROW EXECUTE PROCEDURE incrementProjectImage();"


        println createFunction
        println dropTrigger
        println createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectImageCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementProjectImage() RETURNS trigger as '
        BEGIN UPDATE project SET count_images = count_images - 1 WHERE id = OLD.project_id; RETURN OLD; END ;'
        LANGUAGE plpgsql;"""

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrImageProject on image_instance;"

        String createTrigger = "CREATE TRIGGER countDecrImageProject AFTER DELETE ON image_instance FOR EACH ROW EXECUTE PROCEDURE decrementProjectImage();"

        println createFunction
        println dropTrigger
        println createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectAnnotationCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementProjectAnnotation() RETURNS trigger as '
        BEGIN
        UPDATE project
        SET count_annotations = count_annotations + 1
        FROM image_instance
        WHERE project.id = image_instance.project_id
        AND image_instance.id = NEW.image_id; RETURN NEW;
        END ;
        ' LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAnnotationProject on annotation;"

        String createTrigger = "CREATE TRIGGER countAnnotationProject AFTER INSERT ON annotation FOR EACH ROW EXECUTE PROCEDURE incrementProjectAnnotation(); "

        println createFunction
        println dropTrigger
        println createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectAnnotationCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementProjectAnnotation() RETURNS trigger as '
        BEGIN
        UPDATE project
        SET count_annotations = count_annotations - 1
        FROM image_instance
        WHERE project.id = image_instance.project_id
        AND image_instance.id = OLD.image_id; RETURN OLD;
        END ;
        ' LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationProject on annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationProject AFTER DELETE ON annotation FOR EACH ROW EXECUTE PROCEDURE decrementProjectAnnotation(); "

        println createFunction
        println dropTrigger
        println createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getImageAnnotationCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementImageAnnotation() RETURNS trigger as '
        BEGIN
        UPDATE image_instance
        SET count_image_annotations = count_image_annotations + 1
        WHERE image_instance.id = NEW.image_id; RETURN NEW;
        END ;
        ' LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAnnotationImage on annotation;"

        String createTrigger = "CREATE TRIGGER countAnnotationImage AFTER INSERT ON annotation FOR EACH ROW EXECUTE PROCEDURE incrementImageAnnotation(); "

        println createFunction
        println dropTrigger
        println createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getImageAnnotationCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementImageAnnotation() RETURNS trigger as '
        BEGIN
        UPDATE image_instance
        SET count_image_annotations = count_image_annotations - 1
        WHERE image_instance.id = OLD.image_id; RETURN OLD;
        END ;
        ' LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationImage on annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationImage AFTER DELETE ON annotation FOR EACH ROW EXECUTE PROCEDURE decrementImageAnnotation(); "

        println createFunction
        println dropTrigger
        println createTrigger
        return createFunction + dropTrigger + createTrigger
    }
}
