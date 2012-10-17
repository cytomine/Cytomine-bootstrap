package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 * This exception means that the content of the request in not valid
 * E.g. The project we want to add has no ontology
 */
public class InvalidRequestException extends CytomineException {

    /**
     * Message map with this exception
     * @param message Message
     */
    public InvalidRequestException(String message) {
             super(message,400);
    }

}
