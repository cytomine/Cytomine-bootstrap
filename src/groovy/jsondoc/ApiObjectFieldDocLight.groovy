package jsondoc

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

    @Override
    public static ApiObjectFieldDoc buildFromAnnotation(ApiObjectField annotation, Field field) {

        println "*************" + field.getName() + "*************"
        println annotation.apiFieldName()
        println annotation.description()
        println annotation.allowedType()
        println isGrailsDomain(field.type.name)
        println "*************************************************"

        ApiObjectFieldDoc apiPojoFieldDoc = new ApiObjectFieldDoc();
        if (annotation.apiFieldName().equals("")) { apiPojoFieldDoc.setName(field.getName());}
        else { apiPojoFieldDoc.setName(annotation.apiFieldName());}
        apiPojoFieldDoc.setDescription(annotation.description());
        String[] typeChecks = getFieldObject(field);
        if (annotation.allowedType().equals("")) {
            if(isGrailsDomain(field.type.name)) {
                apiPojoFieldDoc.setType("long");
            } else {
                apiPojoFieldDoc.setType(typeChecks[0]);
            }

        }
        else { apiPojoFieldDoc.setType(annotation.allowedType());}
        apiPojoFieldDoc.setMultiple(String.valueOf(JSONDocUtils.isMultiple(field.getType())));
        apiPojoFieldDoc.setFormat(annotation.format());
        apiPojoFieldDoc.setAllowedvalues(annotation.allowedvalues());
        apiPojoFieldDoc.setMapKeyObject(typeChecks[1]);
        apiPojoFieldDoc.setMapValueObject(typeChecks[2]);
        apiPojoFieldDoc.setMap(typeChecks[3]);
        return apiPojoFieldDoc;
    }


    public static boolean isGrailsDomain(String fullName) {

        def domain = Holders.getGrailsApplication().getDomainClasses().find {
            it.fullName.equals(fullName)
        }
        return domain != null
    }

}