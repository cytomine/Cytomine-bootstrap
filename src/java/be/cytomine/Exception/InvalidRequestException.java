package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 */
public class InvalidRequestException extends CytomineException {

    public InvalidRequestException(String message) {
             super(message,400);
    }

}
