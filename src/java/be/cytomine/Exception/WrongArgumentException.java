package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 */
public class WrongArgumentException extends CytomineException {

    public WrongArgumentException(String message) {
             super(message,400);
    }
}
