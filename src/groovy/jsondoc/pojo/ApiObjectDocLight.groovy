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
import java.lang.reflect.Method

/**
 * ApiMethodDocLight must be used instead of ApiMethodDoc to use a light rest api doc
 * @author Loïc Rollus
 *
 */
public class ApiObjectDocLight {


    @SuppressWarnings("rawtypes")
    public static ApiObjectDoc buildFromAnnotation(ApiObject annotation, Class clazz) {
        buildFromAnnotation(annotation.name(),annotation.description(),clazz)
    }

    @SuppressWarnings("rawtypes")
    public static ApiObjectDoc buildFromAnnotation(String name, String description, Class clazz, boolean custom = false) {
        List<ApiObjectFieldDoc> fieldDocs = new ArrayList<ApiObjectFieldDoc>();

        //map that store: key=json field name and value = [type: field class, description: field desc,...]
        Map<String,Map<String,String>> annotationsMap = new TreeMap<String,Map<String,String>>()

        def domain = Holders.getGrailsApplication().getDomainClasses().find {
            it.shortName.equals(clazz.simpleName)
        }

        //build map field (with super class too)
        if(domain) {

            //analyse all fields for each classes and superclass
            Class classToProcess = domain.clazz
            while(classToProcess.simpleName!="Object") {
                //for exemple: nested image instance EXTEND image instance EXTEND cytomine domain EXTEND object
                //move throught the class flow
                fillAnnotationMap(classToProcess,annotationsMap)
                classToProcess = classToProcess.superclass
            }



            //build map with json by calling getDataFromDomain
            Method m = clazz.getDeclaredMethod("getDataFromDomain", Object);
            def arrayWithNull = new String[1]
            arrayWithNull[0] = null
            Object o = m.invoke(null,arrayWithNull );
            def jsonMap = o
            jsonMap.each {
                def metadata = annotationsMap.get(it.key)
                def type = "Undefined"
                def desc = "Undefined"
                def useForCreation = true
                def mandatory = false
                def defaultValue = "Undefined"
                def presentInResponse = true

                if(metadata) {
                    type = metadata.type
                    desc = metadata.description
                    useForCreation = metadata.useForCreation
                    mandatory = metadata.mandatory
                    defaultValue = metadata.defaultValue
                    presentInResponse = metadata.presentInResponse
                }
                println "Field " + it.key + " => " + type + " " + desc
                fieldDocs.add(buildFieldDocs(it.key.toString(),desc,type,useForCreation,mandatory,defaultValue,presentInResponse));
                annotationsMap.remove(it.key)
            }

        } else {
            //custom response doc, don't use json
            fillAnnotationMap(clazz,annotationsMap,name)
        }

        //not in json but defined in project domain
        annotationsMap.each {
            def value = it.value
            fieldDocs.add(buildFieldDocs(it.key.toString(),value['description'],value['type'],value['useForCreation'],value['mandatory'],value['defaultValue'],false));
        }

        return new ApiObjectDoc(custom ? "["+name+"]" : name, description, fieldDocs);
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
    static void fillAnnotationMap(def domainClass, def annotationsMap,String fieldname=null) {
        domainClass.declaredFields.each { field ->
            println "fields="+field.name
            if(fieldname==null || field.name.equals(fieldname)) {

                if(fieldname==null) {
                    //if fieldname!=null => custom field from CustomResponseDoc, so skip this annotation
                    if(field.isAnnotationPresent(ApiObjectFieldLight.class)) {
                        def annotation = field.getAnnotation(ApiObjectFieldLight.class)
                        addAnnotationToMap(annotationsMap,field,annotation)
                    }
                }
                if(field.isAnnotationPresent(ApiObjectFieldsLight.class)) {
                    def annotation = field.getAnnotation(ApiObjectFieldsLight.class)
                    annotation.params().each { apiObjectFieldsLight ->
                        addAnnotationToMap(annotationsMap,field,apiObjectFieldsLight)
                    }
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
        map.put(annotationData['name'],annotationData)
    }

    static String findDefaultValue(String type, String defaultValue) {
        if(!defaultValue.equals("")) {
            return defaultValue
        } else {
            if(type.equals("long") || type.equals("int") || type.equals("integer")) return "0 or null if domain"
            if(type.equals("list")) return "[]"
            if(type.equals("boolean")) return "false"

        }
        return "Undefined"
    }




}