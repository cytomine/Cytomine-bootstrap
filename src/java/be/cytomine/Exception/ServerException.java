package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 */
public class ServerException extends CytomineException {

    public ServerException(String message) {
             super(message,500);
    }

}
