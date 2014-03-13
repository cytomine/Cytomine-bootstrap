package jsondoc.annotation

import org.jsondoc.core.pojo.ApiVerb

import java.lang.annotation.*

/**
 * Override ApiMethod to hbe allowed to have have no value for some fields
 * @author Loïc Rollus
 *
 */
@Documented
@Target(value=ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiMethodLight {

    /**
     * The relative path for this method (ex. /country/get/{name})
     * @return
     */
    public String path() default "Undefined"; //should be ApiMethodDocLight.UNDEFINED!

    /**
     * A description of what the method does
     * @return
     */
    public String description();

    /**
     * The request verb (or method), to be filled with an ApiVerb value (GET, POST, PUT, DELETE)
     * @see ApiVerb
     * @return
     */
    public ApiVerb verb() default ApiVerb.GET;

    /**
     * An array of strings representing media types produced by the method, like application/json, application/xml, ...
     * @return
     */
    public String[] produces() default []; // [MediaType.APPLICATION_JSON_VALUE]

    /**
     * An array of strings representing media types consumed by the method, like application/json, application/xml, ...
     * @return
     */
    public String[] consumes() default [];

    //is it a listing action? => put max/offset
    public boolean listing() default false;
}
