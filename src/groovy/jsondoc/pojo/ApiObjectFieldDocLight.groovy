package jsondoc.pojo

import grails.util.Holders
import org.jsondoc.core.pojo.ApiMethodDoc
import org.jsondoc.core.pojo.*
import org.jsondoc.core.pojo.ApiVerb
import org.springframework.http.MediaType
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.jsondoc.core.annotation.ApiObjectField;
import org.jsondoc.core.util.JSONDocUtils;
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