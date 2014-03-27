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

    public final static String DEFAULT_TYPE = MediaType.APPLICATION_JSON_VALUE

    public final static String UNDEFINED = "Undefined"

    /**
     * Build a method doc from annotation
     * @param annotation Method annotation
     * @param path HTTP REST path to the method
     * @param verb HTTP verb for the method path
     * @return A method doc object
     */
    public static ApiMethodDoc buildFromAnnotation(ApiMethodLight annotation, String path, String verb) {
        ApiMethodDoc apiMethodDoc = new ApiMethodDoc();

        def objVerb = retrieveVerb(verb)
        String newPath = path.trim()

        if(!annotation.path().equals(UNDEFINED)) {
            //path has been overrided in urlmapping
            newPath = annotation.path()
            objVerb = annotation.verb()
        }

        def prod = Arrays.asList(annotation.produces())
        def cons = Arrays.asList(annotation.consumes())

        if(cons.isEmpty() && (objVerb==ApiVerb.POST || objVerb==ApiVerb.PUT)) {
            //if no cons definition and POST/PUT method => auto put json
            cons = [DEFAULT_TYPE]
        }
        if(!cons.isEmpty() && (cons.first()==null && cons.first().equals(""))) {
            //if force set cons to null/empty string, no cons definition
            cons = []
        }

        if(prod.isEmpty()) {
            //if no cons definition => auto put json for all verb
            prod = [DEFAULT_TYPE]
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
        return ApiVerb.valueOf(verb.toUpperCase())
    }

}