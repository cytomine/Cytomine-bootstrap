package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 * This exception means that a domain is not valid
 * For exemple: we try to add a project without name
 * It correspond to the HTTP code 400 (Bad request)
 */
public class ConstraintException extends CytomineException {

    static int CODE = 400;

    /**
     * Message map with this exception
     * @param message Message
     */
    public ConstraintException(String message) {
        super(message,CODE);

    }
}
