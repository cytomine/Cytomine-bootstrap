package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 */
public class ConstraintException extends CytomineException {

    public ConstraintException(String message) {
        super(message,400);

    }
}
