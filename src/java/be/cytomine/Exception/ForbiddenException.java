package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 */
public class ForbiddenException extends CytomineException {

    public ForbiddenException(String message) {
             super(message,403);
    }

}
