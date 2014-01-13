package jsondoc

import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.annotation.ApiObjectField
import org.jsondoc.core.pojo.ApiMethodDoc
import org.jsondoc.core.pojo.*
import org.jsondoc.core.pojo.ApiVerb
import org.springframework.http.MediaType

import java.lang.reflect.Field

/**
 * ApiMethodDocLight must be used instead of ApiMethodDoc to use a light rest api doc
 * @author Loïc Rollus
 *
 */
public class ApiObjectDocLight {

    @SuppressWarnings("rawtypes")
    public static ApiObjectDoc buildFromAnnotation(ApiObject annotation, Class clazz) {
        List<ApiObjectFieldDoc> fieldDocs = new ArrayList<ApiObjectFieldDoc>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(ApiObjectField.class) != null) {
                fieldDocs.add(ApiObjectFieldDocLight.buildFromAnnotation(field.getAnnotation(ApiObjectField.class), field));
            }
        }

        Class<?> c = clazz.getSuperclass();
        if (c != null) {
            if (c.isAnnotationPresent(ApiObject.class)) {
                ApiObjectDoc objDoc = ApiObjectDoc.buildFromAnnotation(c.getAnnotation(ApiObject.class), c);
                fieldDocs.addAll(objDoc.getFields());
            }
        }

        return new ApiObjectDoc(annotation.name(), annotation.description(), fieldDocs);
    }

}