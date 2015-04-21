package be.cytomine.Exception;

/**
 * Created by julien
 * Date : 12/03/15
 * Time : 14:18
 */
public class MiddlewareException extends CytomineException {

    /**
     * Message map with this exception
     * @param message Message
     */
    public MiddlewareException(String message) {
        super(message,500);
    }

}