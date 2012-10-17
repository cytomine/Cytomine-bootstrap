package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 * This exception means that the server failed
 */
public class ServerException extends CytomineException {

    /**
     * Message map with this exception
     * @param message Message
     */
    public ServerException(String message) {
             super(message,500);
    }

}
