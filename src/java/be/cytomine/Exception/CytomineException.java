package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 */
public abstract class CytomineException extends Exception{

    public int code;
    public String message;

    public CytomineException(String message, int code) {
        this.message=message;
        this.code = code;
    }
}
