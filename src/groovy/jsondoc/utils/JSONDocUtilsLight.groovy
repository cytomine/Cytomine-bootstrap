package jsondoc.utils

import be.cytomine.api.doc.CustomResponseDoc
import jsondoc.annotation.ApiMethodLight
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import jsondoc.pojo.ApiMethodDocLight
import jsondoc.pojo.ApiObjectDocLight
import org.jsondoc.core.util.JSONDocUtils



import java.lang.reflect.Method

import org.jsondoc.core.annotation.*;
import org.jsondoc.core.pojo.ApiBodyObjectDoc;
import org.jsondoc.core.pojo.ApiDoc;
import org.jsondoc.core.pojo.ApiErrorDoc;
import org.jsondoc.core.pojo.ApiHeaderDoc;
import org.jsondoc.core.pojo.ApiMethodDoc;
import org.jsondoc.core.pojo.ApiObjectDoc;
import org.jsondoc.core.pojo.ApiParamDoc;
import org.jsondoc.core.pojo.ApiParamType;
import org.jsondoc.core.pojo.ApiResponseObjectDoc
import org.reflections.Reflections

public class JSONDocUtilsLight extends JSONDocUtils {

    @Override
    public static Set<ApiDoc> getApiDocs(Set<Class<?>> classes) {
        Set<ApiDoc> apiDocs = new TreeSet<ApiDoc>();

        //build map with "controller.action" => path and verb
        BuildPathMap buildPathMap = new BuildPathMap()
        RulesLight rules = buildPathMap.build()

        for (Class<?> controller : classes) {
            ApiDoc apiDoc = ApiDoc.buildFromAnnotation(controller.getAnnotation(Api.class));
            apiDoc.setMethods(getApiMethodDocs(controller,rules));
            apiDocs.add(apiDoc);
        }
        return apiDocs;
    }

    @Override
    public static Set<ApiObjectDoc> getApiObjectDocs(Set<Class<?>> classes) {
        Set<ApiObjectDoc> pojoDocs = new TreeSet<ApiObjectDoc>();
        for (Class<?> pojo : classes) {
            ApiObject annotation = pojo.getAnnotation(ApiObject.class);
            ApiObjectDoc pojoDoc = ApiObjectDocLight.buildFromAnnotation(annotation, pojo);
            if(annotation.show()) {
                pojoDocs.add(pojoDoc);
            }
        }


        //if response is not "standard" (with json) simply use CustomResponseDoc class
        CustomResponseDoc customResponseDoc = new  CustomResponseDoc()

        customResponseDoc.class.declaredFields.each { field ->
            if(field.isAnnotationPresent(ApiObjectFieldLight.class)) {
                def annotation = field.getAnnotation(ApiObjectFieldLight.class)
                ApiObjectDoc pojoDoc = ApiObjectDocLight.buildFromAnnotation(field.name, annotation.description(),customResponseDoc.class, true);
                pojoDocs.add(pojoDoc);
            }
        }



        return pojoDocs;
    }

    @Override
    private static List<ApiMethodDoc> getApiMethodDocs(Class<?> controller, RulesLight rules) {
        List<ApiMethodDoc> apiMethodDocs = new ArrayList<ApiMethodDoc>();
        Method[] methods = controller.getMethods();

        for (Method method : methods) {

            if(method.isAnnotationPresent(ApiMethodLight.class)) {
                RuleLight rule = rules.getRule(controller.simpleName,method.name)
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
                if(method.isAnnotationPresent(ApiParams.class)) {

                    def urlParams = ApiParamDoc.buildFromAnnotation(method.getAnnotation(ApiParams.class), ApiParamType.PATH)
                    apiMethodDoc.setPathparameters(urlParams.minus(null));

                    queryParameters = ApiParamDoc.buildFromAnnotation(method.getAnnotation(ApiParams.class), ApiParamType.QUERY)
                }

                if(method.getAnnotation(ApiMethodLight.class).listing()) {
                    queryParameters.add(new ApiParamDoc("max", "Pagination: Number of record per page (default 0 = no pagination)", "int", "false", new String[0], ""))
                    queryParameters.add(new ApiParamDoc("offset", "Pagination: Offset of first record (default 0 = first record)", "int", "false", new String[0], ""))
                }

                apiMethodDoc.setQueryparameters(queryParameters.minus(null));

                if(method.isAnnotationPresent(ApiBodyObject.class)) {
                    apiMethodDoc.setBodyobject(ApiBodyObjectDoc.buildFromAnnotation(method.getAnnotation(ApiBodyObject.class)));
                } else if(verb.equals("POST") || verb.equals("PUT")) {
                    String currentDomain = controller.newInstance().currentDomainName()
                    apiMethodDoc.setBodyobject(new ApiBodyObjectDoc(currentDomain, "", "", "Unknow", ""));
                }

                if(method.isAnnotationPresent(ApiResponseObject.class)) {
                    apiMethodDoc.setResponse(ApiResponseObjectDoc.buildFromAnnotation(method.getAnnotation(ApiResponseObject.class), method));
                } else {
                    String currentDomain = controller.newInstance().currentDomainName()
                    apiMethodDoc.setResponse(new ApiResponseObjectDoc(currentDomain, "", "", "Unknow", ""))
                }

                List<ApiErrorDoc> errors = []
                if(method.isAnnotationPresent(ApiErrors.class)) {
                    errors = ApiErrorDoc.buildFromAnnotation(method.getAnnotation(ApiErrors.class))
                }

                //add 401,403,404 by default and 409 only if POST/PUT
                def error400 = errors.find{it.code.equals("400")}
                if(!error400) {
                    errors.add(new ApiErrorDoc("400", "Bad Request: missing parameters or bad message format"));
                }

                def error401 = errors.find{it.code.equals("401")}
                if(!error401) {
                    errors.add(new ApiErrorDoc("401", "Unauthorized: must be auth"));
                }

                def error403 = errors.find{it.code.equals("403")}
                if(!error403) {
                    errors.add(new ApiErrorDoc("403", "Forbidden: role error"));
                }

                def error404 = errors.find{it.code.equals("401")}
                if(!error404) {
                    errors.add(new ApiErrorDoc("404", "Object not found"));
                }

                if(verb.equals("POST") || verb.equals("PUT")) {
                    def error409 = errors.find{it.code.equals("409")}
                    if(!error409) {
                        errors.add(new ApiErrorDoc("409", "Object already exist"));
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