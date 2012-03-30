package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 */
public class TooLongRequestException extends CytomineException {

    public TooLongRequestException(String message) {
             super(message,413);
    }

}
