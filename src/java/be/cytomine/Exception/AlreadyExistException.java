package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 * This exception means that a domain already exist in database
 * For exemple: we try to add a project with same name
 * It correspond to the HTTP code 409 (Conflict)
 */
public class AlreadyExistException extends CytomineException {

    /**
     * Message map with this exception
     * @param message Message
     */
    public AlreadyExistException(String message) {
             super(message,409);
    }
}
