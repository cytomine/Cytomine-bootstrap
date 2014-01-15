package jsondoc.pojo

import jsondoc.annotation.ApiMethodLight
import org.jsondoc.core.pojo.ApiMethodDoc
import org.jsondoc.core.pojo.ApiVerb
import org.springframework.http.MediaType

/**
 * ApiMethodDocLight must be used instead of ApiMethodDoc to use a light rest api doc
 * @author Loïc Rollus
 *
 */
public class ApiMethodDocLight {

    public final static String UNDEFINED = "Undefined"


    public static ApiMethodDoc buildFromAnnotation(ApiMethodLight annotation, String path, String verb) {
        ApiMethodDoc apiMethodDoc = new ApiMethodDoc();

        def objVerb = retrieveVerb(verb)
        String newPath = path.trim()

        if(!annotation.path().equals(UNDEFINED)) {
            //path has been overrided
            newPath = annotation.path()
            objVerb = annotation.verb()
        }

        def prod = Arrays.asList(annotation.produces())
        def cons = Arrays.asList(annotation.consumes())

        if(cons.isEmpty() && (objVerb==ApiVerb.POST || objVerb==ApiVerb.PUT)) {
            //if no cons definition and POST/PUT method => auto put json
            cons = [MediaType.APPLICATION_JSON_VALUE]
        }
        if(!cons.isEmpty() && (cons.first()==null && cons.first().equals(""))) {
            //if force set cons to null/empty string, no cons definition
            cons = []
        }

        if(prod.isEmpty()) {
            //if no cons definition => auto put json for all verb
            prod = [MediaType.APPLICATION_JSON_VALUE]
        }
        if(!prod.isEmpty() && (prod.first()==null && prod.first().equals(""))) {
            //if force set cons to null/empty string, no cons definition
            prod = []
        }

        apiMethodDoc.setPath(newPath);
        apiMethodDoc.setDescription(annotation.description());
        apiMethodDoc.setVerb(objVerb);
        apiMethodDoc.setProduces(prod);
        apiMethodDoc.setConsumes(cons);


        return apiMethodDoc;
    }

    public static ApiVerb retrieveVerb(String verb) {
        //return ApiVerb.valueOf(verb.toUpperCase())
        return ApiVerb.valueOf(verb.toUpperCase())
    }


}