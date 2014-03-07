package jsondoc.pojo

import org.jsondoc.core.pojo.*
/**
 * ApiMethodDocLight must be used instead of ApiMethodDoc to use a light rest api doc
 * @author Loïc Rollus
 *
 */
public class ApiObjectFieldDocLight extends ApiObjectFieldDoc {

    Boolean useForCreation

    //only user if userForCreation = true
    Boolean mandatory

    //only if diff from java standart (bool true, string non empty, number !=0,...)
    String defaultValue

    Boolean presentInResponse


}