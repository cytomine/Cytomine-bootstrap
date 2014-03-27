package jsondoc.utils

import groovy.util.logging.Log
import jsondoc.annotation.ApiBodyObjectLight
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiParamsLight
import jsondoc.annotation.ApiResponseObjectLight
import jsondoc.pojo.ApiMethodDocLight
import jsondoc.pojo.ApiObjectDocLight
import jsondoc.pojo.ApiParamDocLight
import jsondoc.pojo.ApiResponseObjectDocLight
import org.jsondoc.core.annotation.Api
import org.jsondoc.core.annotation.ApiErrors
import org.jsondoc.core.annotation.ApiHeaders
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.pojo.*
import org.jsondoc.core.util.JSONDocUtils

import java.lang.reflect.Method

@Log
public class JSONDocUtilsLight extends JSONDocUtils {

    static def DEFAULT_ERROR_ALL = [
            "400": "Bad Request: missing parameters or bad message format",
            "401": "Unauthorized: must be auth",
            "403": "Forbidden: role error",
            "404": "Object not found"
    ]

    static def DEFAULT_ERROR_GET = [:]

    static def DEFAULT_ERROR_POST = [
            "409": "Object already exist"
    ]

    static def DEFAULT_ERROR_PUT = [
            "409": "Object already exist"
    ]

    static def DEFAULT_ERROR_DELETE = [:]

    /**
     * Build Doc for controller method
     * @param classes Controllers classes
     * @return Controller method doc
     */
    @Override
    public static Set<ApiDoc> getApiDocs(Set<Class<?>> classes) {
        Set<ApiDoc> apiDocs = new TreeSet<ApiDoc>();

        //build map with ["controller.action" => path and verb]
        log.info "Build path map..."
        BuildPathMap buildPathMap = new BuildPathMap()
        MappingRules rules = buildPathMap.build()

        //For each controller, build doc from annotation and build method doc
        for (Class<?> controller : classes) {
            ApiDoc apiDoc = ApiDoc.buildFromAnnotation(controller.getAnnotation(Api.class));
            apiDoc.setMethods(getApiMethodDocs(controller,rules));
            apiDocs.add(apiDoc);
        }
        return apiDocs;
    }

    /**
     * Build doc for domain
     * @param classes Domain classes
     * @return Domain object doc
     */
    @Override
    public static Set<ApiObjectDoc> getApiObjectDocs(Set<Class<?>> classes,def customResponseDoc=null) {

        Set<ApiObjectDoc> pojoDocs = new TreeSet<ApiObjectDoc>();
        for (Class<?> pojo : classes) {
            ApiObject annotation = pojo.getAnnotation(ApiObject.class);
            ApiObjectDoc pojoDoc = ApiObjectDocLight.buildFromAnnotation(annotation, pojo);
            if(annotation.show()) {
                pojoDocs.add(pojoDoc);
            }
        }

        //if response is not "standard" (with json) simply use CustomResponseDoc class
        if(customResponseDoc) {
            customResponseDoc.class.declaredFields.each { field ->
                if(field.isAnnotationPresent(ApiObjectFieldLight.class)) {
                    def annotation = field.getAnnotation(ApiObjectFieldLight.class)
                    ApiObjectDoc pojoDoc = ApiObjectDocLight.buildFromAnnotation(field.name, annotation.description(),customResponseDoc.class, true);
                    pojoDocs.add(pojoDoc);
                }
            }
        }




        return pojoDocs;
    }

    /**
     * Build method doc object for all controller methods
     */
    @Override
    private static List<ApiMethodDoc> getApiMethodDocs(Class<?> controller, MappingRules rules) {
        log.info "\tProcess controller ${controller} ..."
        List<ApiMethodDoc> apiMethodDocs = new ArrayList<ApiMethodDoc>();
        Method[] methods = controller.getMethods();

        for (Method method : methods) {
            log.info "\t\tProcess method ${method} ..."
            if(method.isAnnotationPresent(ApiMethodLight.class)) {

                //Retrieve the path/verb to go to this method
                MappingRulesEntry rule = rules.getRule(controller.simpleName,method.name)
                String verb = "GET"
                String path = "Undefined"
                if(rule) {
                    verb = rule.verb
                    path = rule.path
                }

                ApiMethodDoc apiMethodDoc = ApiMethodDocLight.buildFromAnnotation(method.getAnnotation(ApiMethodLight.class),path,verb);

                if(method.isAnnotationPresent(ApiHeaders.class)) {
                    apiMethodDoc.setHeaders(ApiHeaderDoc.buildFromAnnotation(method.getAnnotation(ApiHeaders.class)));
                }

                def queryParameters = []
                if(method.isAnnotationPresent(ApiParamsLight.class)) {

                    def urlParams = ApiParamDocLight.buildFromAnnotation(method.getAnnotation(ApiParamsLight.class), ApiParamType.PATH)
                    apiMethodDoc.setPathparameters(urlParams.minus(null));

                    queryParameters = ApiParamDocLight.buildFromAnnotation(method.getAnnotation(ApiParamsLight.class), ApiParamType.QUERY)
                }

                if(method.getAnnotation(ApiMethodLight.class).listing()) {
                    queryParameters.add(new ApiParamDocLight("max", "Pagination: Number of record per page (default 0 = no pagination)", "int", "false", new String[0], ""))
                    queryParameters.add(new ApiParamDocLight("offset", "Pagination: Offset of first record (default 0 = first record)", "int", "false", new String[0], ""))
                }

                apiMethodDoc.setQueryparameters(queryParameters.minus(null));

                if(method.isAnnotationPresent(ApiBodyObjectLight.class)) {
                    apiMethodDoc.setBodyobject(ApiBodyObjectDoc.buildFromAnnotation(method.getAnnotation(ApiBodyObjectLight.class)));
                } else if(verb.equals("POST") || verb.equals("PUT")) {
                    String currentDomain = controller.newInstance().currentDomainName()
                    apiMethodDoc.setBodyobject(new ApiBodyObjectDoc(currentDomain, "", "", "Unknow", ""));
                }

                if(method.isAnnotationPresent(ApiResponseObjectLight.class)) {
                    apiMethodDoc.setResponse(ApiResponseObjectDocLight.buildFromAnnotation(method.getAnnotation(ApiResponseObjectLight.class), method));
                } else {
                    String currentDomain = controller.newInstance().currentDomainName()
                    apiMethodDoc.setResponse(new ApiResponseObjectDocLight(currentDomain, "", "", "Unknow", ""))
                }

                List<ApiErrorDoc> errors = []
                if(method.isAnnotationPresent(ApiErrors.class)) {
                    errors = ApiErrorDoc.buildFromAnnotation(method.getAnnotation(ApiErrors.class))
                }

                //add default errors
                DEFAULT_ERROR_ALL.each { code ->
                    if(!errors.find{it.code.equals(code.key)}) {
                        errors.add(new ApiErrorDoc(code.key, code.value));
                    }
                }

                if(verb.equals("GET")) {
                    DEFAULT_ERROR_GET.each { code ->
                        if(!errors.find{it.code.equals(code.key)}) {
                            errors.add(new ApiErrorDoc(code.key, code.value));
                        }
                    }
                } else if(verb.equals("POST")) {
                    DEFAULT_ERROR_POST.each { code ->
                        if(!errors.find{it.code.equals(code.key)}) {
                            errors.add(new ApiErrorDoc(code.key, code.value));
                        }
                    }
                } else if(verb.equals("PUT")) {
                    DEFAULT_ERROR_PUT.each { code ->
                        if(!errors.find{it.code.equals(code.key)}) {
                            errors.add(new ApiErrorDoc(code.key, code.value));
                        }
                    }
                } else if(verb.equals("DELETE")) {
                    DEFAULT_ERROR_DELETE.each { code ->
                        if(!errors.find{it.code.equals(code.key)}) {
                            errors.add(new ApiErrorDoc(code.key, code.value));
                        }
                    }
                }
                apiMethodDoc.setApierrors(errors)
                apiMethodDocs.add(apiMethodDoc);
            }
        }
        return apiMethodDocs;
    }

    public static boolean isMultiple(String className) {
        if(className.toLowerCase().equals("list") || className.toLowerCase().equals("map")) {
            return true;
        }
        return false;
    }
}