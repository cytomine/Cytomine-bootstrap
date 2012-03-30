package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 */
public class ObjectNotFoundException extends CytomineException {

    public ObjectNotFoundException(String message) {
             super(message,404);
    }
}
