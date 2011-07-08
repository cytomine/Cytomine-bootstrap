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
      def statement  = connection.createStatement()


      statement.execute(getImageCountTriggerIncr())
      statement.execute(getImageCountTriggerDecr())
      statement.execute(getAnnotationCountTriggerIncr())
      statement.execute(getAnnotationCountTriggerDecr())


    } catch (org.postgresql.util.PSQLException e) {
      println e
    }

  }

  String getImageCountTriggerIncr() {
    String createFunction ="""
        CREATE OR REPLACE FUNCTION incrementImage() RETURNS trigger as '
        BEGIN UPDATE project SET count_images = count_images + 1 WHERE id = NEW.project_id; RETURN NEW; END ;'
        LANGUAGE plpgsql;"""

    String dropTrigger = "DROP TRIGGER IF EXISTS countImageProject on image_instance;"

    String createTrigger = "CREATE TRIGGER countImageProject AFTER INSERT ON image_instance FOR EACH ROW EXECUTE PROCEDURE incrementImage();"

    println createFunction
    println dropTrigger
    println createTrigger
    return createFunction + dropTrigger+createTrigger
  }

  String getImageCountTriggerDecr() {
    String createFunction ="""
        CREATE OR REPLACE FUNCTION decrementImage() RETURNS trigger as '
        BEGIN UPDATE project SET count_images = count_images - 1 WHERE id = OLD.project_id; RETURN OLD; END ;'
        LANGUAGE plpgsql;"""

    String dropTrigger = "DROP TRIGGER IF EXISTS countDecrImageProject on image_instance;"

    String createTrigger = "CREATE TRIGGER countDecrImageProject AFTER DELETE ON image_instance FOR EACH ROW EXECUTE PROCEDURE decrementImage();"

    println createFunction
    println dropTrigger
    println createTrigger
    return createFunction + dropTrigger+createTrigger
  }

  String getAnnotationCountTriggerIncr() {
    String createFunction ="""
        CREATE OR REPLACE FUNCTION incrementAnnotation() RETURNS trigger as '
        BEGIN
        UPDATE project
        SET count_annotations = count_annotations + 1
        FROM image_instance, annotation
        WHERE project.id = image_instance.project_id
        AND image_instance.id = NEW.image_id; RETURN NEW;
        END ;
        ' LANGUAGE plpgsql; """

    String dropTrigger = "DROP TRIGGER IF EXISTS countAnnotationProject on annotation;"

    String createTrigger = "CREATE TRIGGER countAnnotationProject AFTER INSERT ON annotation FOR EACH ROW EXECUTE PROCEDURE incrementAnnotation(); "

    println createFunction
    println dropTrigger
    println createTrigger
    return createFunction + dropTrigger+createTrigger
  }

  String getAnnotationCountTriggerDecr() {
    String createFunction ="""
        CREATE OR REPLACE FUNCTION decrementAnnotation() RETURNS trigger as '
        BEGIN
        UPDATE project
        SET count_annotations = count_annotations - 1
        FROM image_instance, annotation
        WHERE project.id = image_instance.project_id
        AND image_instance.id = OLD.image_id; RETURN OLD;
        END ;
        ' LANGUAGE plpgsql; """

    String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationProject on annotation;"

    String createTrigger = "CREATE TRIGGER countDecrAnnotationProject AFTER DELETE ON annotation FOR EACH ROW EXECUTE PROCEDURE decrementAnnotation(); "

    println createFunction
    println dropTrigger
    println createTrigger
    return createFunction + dropTrigger+createTrigger
  }

}
