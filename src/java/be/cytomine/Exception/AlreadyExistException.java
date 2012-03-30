package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 */
public class AlreadyExistException extends CytomineException {

    public AlreadyExistException(String message) {
             super(message,409);
    }
}
