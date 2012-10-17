package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 * This exception means that some argument from request are not valid
 */
public class WrongArgumentException extends CytomineException {

    /**
     * Message map with this exception
     * @param message Message
     */
    public WrongArgumentException(String message) {
             super(message,400);
    }
}
