package be.cytomine.Exception;

/**
 * User: lrollus
 * Date: 17/11/11
 * GIGA-ULg
 */
public abstract class CytomineException extends RuntimeException{

    public int code;
    public String msg;

    public CytomineException(String msg, int code) {
        this.msg=msg;
        this.code = code;
    }
}
