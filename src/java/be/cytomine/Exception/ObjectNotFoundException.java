package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 * This exception means that the object was not found on DB
 * It correspond to the HTTP code 404
 */
public class ObjectNotFoundException extends CytomineException {

    /**
     * Message map with this exception
     * @param message Message
     */
    public ObjectNotFoundException(String message) {

        super(message,404);
    }
}
