package jsondoc.pojo

import org.jsondoc.core.pojo.ApiObjectFieldDoc

/**
 * ApiMethodDocLight must be used instead of ApiMethodDoc to use a light rest api doc
 * @author Loïc Rollus
 *
 */
public class ApiObjectFieldDocLight extends ApiObjectFieldDoc {

    //field is use for domain creation
    Boolean useForCreation

    //only user if userForCreation = true
    Boolean mandatory

    //only if diff from java standart (bool true, string non empty, number !=0,...)
    String defaultValue

    //field is prent in response
    Boolean presentInResponse


}