package be.cytomine.utils.database

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/07/11
 * Time: 15:16
 *
 * Cytomine Service that create all trigger in database
 * Most of trigger are used avoid request that count something
 * For example:
 * project has properties countUserAnnotation, countImages,...
 * A trigger automaticaly incr (or decr) these values.
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
            statement.execute(getAnnotationReviewCountTriggerIncr())
            statement.execute(getAnnotationReviewCountTriggerDecr())
            statement.execute(getProjectAnnotationReviewedCountTriggerIncr())
            statement.execute(getProjectAnnotationReviewedCountTriggerDecr())
            statement.execute(getImageAnnotationReviewedCountTriggerIncr())
            statement.execute(getImageAnnotationReviewedCountTriggerDecr())

            statement.execute(getAnnotationIndexTriggerIncr())
            statement.execute(getAnnotationIndexTriggerDecr())

            statement.execute(getAlgoAnnotationIndexTriggerIncr())
            statement.execute(getAlgoAnnotationIndexTriggerDecr())

            statement.execute(getReviewedAnnotationIndexTriggerIncr())
            statement.execute(getReviewedAnnotationIndexTriggerDecr())





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
        \$incProjAnnAlgo\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAlgoAnnotationProject on algo_annotation;"

        String createTrigger = "CREATE TRIGGER countAlgoAnnotationProject AFTER INSERT ON algo_annotation FOR EACH ROW EXECUTE PROCEDURE incrementProjectAnnotationAlgo();"

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
         \$decProjAnnAlgo\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationProjectAlgo on algo_annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationProjectAlgo AFTER DELETE ON algo_annotation FOR EACH ROW EXECUTE PROCEDURE decrementProjectAnnotationAlgo(); "

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
            SET count_image_job_annotations = count_image_job_annotations + 1
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
        CREATE OR REPLACE FUNCTION decrementImageAnnotationAlgo() RETURNS trigger as \$decImageAnnAlgo\$
        BEGIN
            UPDATE image_instance
            SET count_image_job_annotations = count_image_job_annotations - 1
            WHERE image_instance.id = OLD.image_id;
        RETURN OLD;

        END ;
        \$decImageAnnAlgo\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationImageAlgo on algo_annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationImageAlgo AFTER DELETE ON algo_annotation FOR EACH ROW EXECUTE PROCEDURE decrementImageAnnotationAlgo(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getAnnotationReviewCountTriggerIncr() {

        //be.cytomine.ontology.AlgoAnnotation
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementAnnotationReviewedAnnotation() RETURNS trigger as \$incAnnRevAnn\$
        DECLARE
           current_class reviewed_annotation.parent_class_name%TYPE;
           algo_class reviewed_annotation.parent_class_name%TYPE := 'be.cytomine.ontology.AlgoAnnotation';
           user_class reviewed_annotation.parent_class_name%TYPE := 'be.cytomine.ontology.UserAnnotation';
        BEGIN
            SELECT parent_class_name INTO current_class from reviewed_annotation where id = NEW.id;
            IF current_class = user_class THEN
                UPDATE user_annotation
                SET count_reviewed_annotations = count_reviewed_annotations + 1
                WHERE user_annotation.id = NEW.parent_ident;
            ELSEIF current_class = algo_class THEN
                UPDATE algo_annotation
                SET count_reviewed_annotations = count_reviewed_annotations + 1
                WHERE algo_annotation.id = NEW.parent_ident;
            END IF;
            RETURN NEW;
        END ;
        \$incAnnRevAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAnnotationReviewedAnnotation on reviewed_annotation;"

        String createTrigger = "CREATE TRIGGER countAnnotationReviewedAnnotation AFTER INSERT ON reviewed_annotation FOR EACH ROW EXECUTE PROCEDURE incrementAnnotationReviewedAnnotation(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getAnnotationReviewCountTriggerDecr() {

        //be.cytomine.ontology.AlgoAnnotation
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementAnnotationReviewedAnnotation() RETURNS trigger as \$decrAnnRevAnn\$
        DECLARE
           current_class reviewed_annotation.parent_class_name%TYPE;
           algo_class reviewed_annotation.parent_class_name%TYPE := 'be.cytomine.ontology.AlgoAnnotation';
           user_class reviewed_annotation.parent_class_name%TYPE := 'be.cytomine.ontology.UserAnnotation';
        BEGIN
            IF OLD.parent_class_name = user_class THEN
                UPDATE user_annotation
                SET count_reviewed_annotations = count_reviewed_annotations - 1
                WHERE user_annotation.id = OLD.parent_ident;
            ELSEIF OLD.parent_class_name = algo_class THEN
                UPDATE algo_annotation
                SET count_reviewed_annotations = count_reviewed_annotations - 1
                WHERE algo_annotation.id = OLD.parent_ident;
            END IF;
            RETURN OLD;
        END ;
        \$decrAnnRevAnn\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countAnnotationReviewedAnnotationDecr on reviewed_annotation;"

        String createTrigger = "CREATE TRIGGER countAnnotationReviewedAnnotationDecr AFTER DELETE ON reviewed_annotation FOR EACH ROW EXECUTE PROCEDURE decrementAnnotationReviewedAnnotation(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectAnnotationReviewedCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementProjectAnnotationReviewed() RETURNS TRIGGER AS \$incProjAnnRev\$
        BEGIN
            UPDATE project
            SET count_reviewed_annotations = count_reviewed_annotations + 1
            WHERE project.id = NEW.project_id;
            RETURN NEW;
        END ;
        \$incProjAnnRev\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countReviewedAnnotationProject on reviewed_annotation;"

        String createTrigger = "CREATE TRIGGER countReviewedAnnotationProject AFTER INSERT ON reviewed_annotation FOR EACH ROW EXECUTE PROCEDURE incrementProjectAnnotationReviewed();"

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getProjectAnnotationReviewedCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementProjectAnnotationReviewed() RETURNS TRIGGER AS \$decProjAnnRev\$
        BEGIN
            UPDATE project
            SET count_reviewed_annotations = count_reviewed_annotations - 1
            WHERE project.id = OLD.project_id;
		    RETURN OLD;
        END ;
         \$decProjAnnRev\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationProjectReviewed on reviewed_annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationProjectReviewed AFTER DELETE ON reviewed_annotation FOR EACH ROW EXECUTE PROCEDURE decrementProjectAnnotationReviewed(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }


    String getImageAnnotationReviewedCountTriggerIncr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION incrementImageAnnotationReviewed() RETURNS TRIGGER AS \$incImgAnnRev\$
        BEGIN
            UPDATE image_instance
            SET count_image_reviewed_annotations = count_image_reviewed_annotations + 1
            WHERE image_instance.id = NEW.image_id;
            RETURN NEW;
        END ;
        \$incImgAnnRev\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countReviewedAnnotationImage on reviewed_annotation;"

        String createTrigger = "CREATE TRIGGER countReviewedAnnotationImage AFTER INSERT ON reviewed_annotation FOR EACH ROW EXECUTE PROCEDURE incrementImageAnnotationReviewed();"

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getImageAnnotationReviewedCountTriggerDecr() {
        String createFunction = """
        CREATE OR REPLACE FUNCTION decrementImageAnnotationReviewed() RETURNS TRIGGER AS \$decImgAnnRev\$
        BEGIN
            UPDATE image_instance
            SET count_image_reviewed_annotations = count_image_reviewed_annotations - 1
            WHERE image_instance.id = OLD.image_id;
		    RETURN OLD;
        END ;
         \$decImgAnnRev\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS countDecrAnnotationImageReviewed on reviewed_annotation;"

        String createTrigger = "CREATE TRIGGER countDecrAnnotationImageReviewed AFTER DELETE ON reviewed_annotation FOR EACH ROW EXECUTE PROCEDURE decrementImageAnnotationReviewed(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }






    String getAnnotationIndexTriggerIncr() {
        String createFunction = """
                            CREATE OR REPLACE FUNCTION incrementAnnotationIndex() RETURNS trigger as \$incAnnUserIndex\$
            DECLARE
                    alreadyExist INTEGER;
            BEGIN
                    SELECT count(*) INTO alreadyExist FROM annotation_index WHERE user_id = NEW.user_id AND image_id = NEW.image_id;
                    IF (alreadyExist=0) THEN
                        INSERT INTO annotation_index(user_id, image_id, count_annotation, count_reviewed_annotation, version, id) VALUES(NEW.user_id,NEW.image_id,0,0,0,nextval('hibernate_sequence'));
                    END IF;
                    UPDATE annotation_index SET count_annotation = count_annotation+1, version = version+1 WHERE user_id = NEW.user_id AND image_id = NEW.image_id;
                    RETURN NEW;
            END;
            \$incAnnUserIndex\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS incrementUserAnnotationIndexTrigger on user_annotation;"

        String createTrigger = "CREATE TRIGGER incrementUserAnnotationIndexTrigger AFTER INSERT ON user_annotation FOR EACH ROW EXECUTE PROCEDURE incrementAnnotationIndex(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getAnnotationIndexTriggerDecr() {
        String createFunction = """
                            CREATE OR REPLACE FUNCTION decrementAnnotationIndex() RETURNS trigger as \$decrAnnUserIndex\$
            DECLARE
                    alreadyExist INTEGER;
            BEGIN
                    UPDATE annotation_index SET count_annotation = count_annotation-1, version = version+1 WHERE user_id = OLD.user_id AND image_id = OLD.image_id;
                    RETURN OLD;
            END;
            \$decrAnnUserIndex\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS decrementUserAnnotationIndexTrigger on user_annotation;"

        String createTrigger = "CREATE TRIGGER decrementUserAnnotationIndexTrigger AFTER DELETE ON user_annotation FOR EACH ROW EXECUTE PROCEDURE decrementAnnotationIndex(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getAlgoAnnotationIndexTriggerIncr() {

        String dropTrigger = "DROP TRIGGER IF EXISTS incrementAlgoAnnotationIndexTrigger on algo_annotation;"

        String createTrigger = "CREATE TRIGGER incrementAlgoAnnotationIndexTrigger AFTER INSERT ON algo_annotation FOR EACH ROW EXECUTE PROCEDURE incrementAnnotationIndex(); "

        log.info dropTrigger
        log.info createTrigger
        return dropTrigger + createTrigger
    }

    String getAlgoAnnotationIndexTriggerDecr() {

        String dropTrigger = "DROP TRIGGER IF EXISTS decrementAlgoAnnotationIndexTrigger on algo_annotation;"

        String createTrigger = "CREATE TRIGGER decrementAlgoAnnotationIndexTrigger AFTER DELETE ON algo_annotation FOR EACH ROW EXECUTE PROCEDURE decrementAnnotationIndex(); "

        log.info dropTrigger
        log.info createTrigger
        return dropTrigger + createTrigger
    }




    String getReviewedAnnotationIndexTriggerIncr() {
        String createFunction = """
                           CREATE OR REPLACE FUNCTION incrementReviewedAnnotationIndex() RETURNS trigger as \$incAnnRevIndex\$
    DECLARE
            alreadyExist INTEGER;
    BEGIN
            SELECT count(*) INTO alreadyExist FROM annotation_index WHERE user_id = NEW.user_id AND image_id = NEW.image_id;
            IF (alreadyExist=0) THEN
                INSERT INTO annotation_index(user_id, image_id, count_annotation, count_reviewed_annotation, version, id) VALUES(NEW.user_id,NEW.image_id,0,0,0,nextval('hibernate_sequence'));
            END IF;
            UPDATE annotation_index SET count_reviewed_annotation = count_reviewed_annotation+1, version = version+1 WHERE user_id = NEW.user_id AND image_id = NEW.image_id;
            RETURN NEW;
    END;
    \$incAnnRevIndex\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS incrementReviewedAnnotationIndexTrigger on reviewed_annotation;"

        String createTrigger = "CREATE TRIGGER incrementReviewedAnnotationIndexTrigger AFTER INSERT ON reviewed_annotation FOR EACH ROW EXECUTE PROCEDURE incrementReviewedAnnotationIndex(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }

    String getReviewedAnnotationIndexTriggerDecr() {
        String createFunction = """
                            CREATE OR REPLACE FUNCTION decrementReviewedAnnotationIndex() RETURNS trigger as \$decrAnnUserIndex\$
            DECLARE
                    alreadyExist INTEGER;
            BEGIN
                    UPDATE annotation_index SET count_reviewed_annotation = count_reviewed_annotation-1, version = version+1 WHERE user_id = OLD.user_id AND image_id = OLD.image_id;
                    RETURN OLD;
            END;
            \$decrAnnUserIndex\$ LANGUAGE plpgsql; """

        String dropTrigger = "DROP TRIGGER IF EXISTS decrementReviewedAnnotationIndexTrigger on reviewed_annotation;"

        String createTrigger = "CREATE TRIGGER decrementReviewedAnnotationIndexTrigger AFTER DELETE ON reviewed_annotation FOR EACH ROW EXECUTE PROCEDURE decrementReviewedAnnotationIndex(); "

        log.info createFunction
        log.info dropTrigger
        log.info createTrigger
        return createFunction + dropTrigger + createTrigger
    }























































}
