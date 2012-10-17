package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 * This exception means that the content of the request is too long
 */
public class TooLongRequestException extends CytomineException {

    /**
     * Message map with this exception
     * @param message Message
     */
    public TooLongRequestException(String message) {
             super(message,413);
    }

}
