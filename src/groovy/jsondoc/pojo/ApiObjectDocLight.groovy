package jsondoc.pojo

import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import grails.util.Holders
import jsondoc.utils.JSONDocUtilsLight
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.jsondoc.core.annotation.ApiObject
import org.jsondoc.core.pojo.*

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

        //map that store: key=json field name and value = [type: field class, description: field desc,...]
        Map<String,Map<String,String>> annotationsMap = new HashMap<String,Map<String,String>>()

        def domain = Holders.getGrailsApplication().getDomainClasses().find {
            it.shortName.equals("Project")
        }
        println "domain=$domain"

        //build map field (with super class too)
        fillAnnotationMap(domain.clazz,annotationsMap)
        fillAnnotationMap(domain.clazz.superclass,annotationsMap)

        //build map with json
        def jsonMap = Project.getDataFromDomain(null)

        jsonMap.each {
            def metadata = annotationsMap.get(it.key)
            def type = "Undefined"
            def description = "Undefined"
            def useForCreation = false
            def mandatory = false
            def defaultValue = "Undefined"
            def presentInResponse = true

            if(metadata) {
                type = metadata.type
                description = metadata.description
                useForCreation = metadata.useForCreation
                mandatory = metadata.mandatory
                defaultValue = metadata.defaultValue
                presentInResponse = metadata.presentInResponse
            }
            println "Field " + it.key + " => " + type + " " + description
            fieldDocs.add(buildFieldDocs(it.key.toString(),description,type,useForCreation,mandatory,defaultValue,presentInResponse));
            annotationsMap.remove(it.key)
        }

        //not in json but defined in project domain
        annotationsMap.each {
            def value = it.value
            fieldDocs.add(buildFieldDocs(it.key.toString(),value['description'],value['type'],value['useForCreation'],value['mandatory'],value['defaultValue'],false));
        }


        return new ApiObjectDoc(annotation.name(), annotation.description(), fieldDocs);
    }

    static ApiObjectFieldDocLight buildFieldDocs(String name, String description, String type, Boolean useForCreation, Boolean mandatory, String defaultValue, Boolean presentInResponse) {
        ApiObjectFieldDocLight apiPojoFieldDoc = new ApiObjectFieldDocLight();
        apiPojoFieldDoc.setName(name)
        apiPojoFieldDoc.setDescription(description)
        apiPojoFieldDoc.setType(type)
        apiPojoFieldDoc.setMultiple(String.valueOf(JSONDocUtilsLight.isMultiple(type)))
        apiPojoFieldDoc.useForCreation = useForCreation
        apiPojoFieldDoc.mandatory = mandatory
        apiPojoFieldDoc.defaultValue = defaultValue
        apiPojoFieldDoc.presentInResponse = presentInResponse
        return apiPojoFieldDoc;
    }

    //take clas and fill the map with field metadata (from annotation)
    static void fillAnnotationMap(def domainClass, def annotationsMap) {
        domainClass.declaredFields.each { field ->
            println "fields="+field.name
            if(field.isAnnotationPresent(ApiObjectFieldLight.class)) {
                def annotation = field.getAnnotation(ApiObjectFieldLight.class)
                addAnnotationToMap(annotationsMap,field,annotation)
            }
            if(field.isAnnotationPresent(ApiObjectFieldsLight.class)) {
                def annotation = field.getAnnotation(ApiObjectFieldsLight.class)
                annotation.params().each { apiObjectFieldsLight ->
                    addAnnotationToMap(annotationsMap,field,apiObjectFieldsLight)
                }
            }
        }
    }

    //add field metadata to a map. Use field data if annotation data is missing
    static def addAnnotationToMap(Map<String,Map<String,String>> map, Field field, ApiObjectFieldLight annotation) {
        def annotationData = [:]

        String[] typeChecks = ApiObjectFieldDocLight.getFieldObject(field);
        if(annotation.allowedType().equals("")) {
            if(CytomineDomain.isGrailsDomain(field.type.name)) {
                annotationData['type'] = "long"
            } else {
                annotationData['type'] = typeChecks[0];
            }
        } else {
            annotationData['type'] = annotation.allowedType()
        }

        if(annotation.apiFieldName().equals("")) {
            annotationData['name'] = field.getName()
        } else {
            annotationData['name'] = annotation.apiFieldName()
        }

        annotationData["description"] = annotation.description()

        annotationData["useForCreation"] = annotation.useForCreation()
        annotationData["mandatory"] = annotation.useForCreation()? annotation.mandatory() : false

        annotationData["presentInResponse"] = annotation.presentInResponse()

        if(annotationData["useForCreation"] && !annotationData["mandatory"]) {
            annotationData["defaultValue"] = findDefaultValue(annotationData['type'].toString(),annotation.defaultValue())

        }


        println annotationData['name'] + "=> " + annotationData
        map.put(annotationData['name'],annotationData)
    }

    static String findDefaultValue(String type, String defaultValue) {
        if(!defaultValue.equals("")) {
            return defaultValue
        } else {
            if(type.equals("long") || type.equals("int")) return "0 or null if domain"
            if(type.equals("list")) return "[]"
            if(type.equals("boolean")) return "false"

        }
        return "Undefined"
    }




}