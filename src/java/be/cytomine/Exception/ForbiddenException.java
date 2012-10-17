package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 * This exception means that a user cannot access to a specific service
 * E.g. a user cannot access an image if its not a user from this project
 * It correspond to the HTTP code 403 (Forbidden)
 */
public class ForbiddenException extends CytomineException {

    /**
     * Message map with this exception
     * @param message Message
     */
    public ForbiddenException(String message) {
             super(message,403);
    }

}
