package jsondoc

import be.cytomine.project.Project
import grails.converters.JSON
import grails.util.Holders
import org.codehaus.groovy.grails.commons.UrlMappingsArtefactHandler
import org.codehaus.groovy.grails.web.mapping.ResponseCodeMappingData
import org.codehaus.groovy.grails.web.mapping.ResponseCodeUrlMapping
import org.codehaus.groovy.grails.web.mapping.UrlMapping
import org.jsondoc.core.annotation.ApiHeaders
import org.jsondoc.core.pojo.ApiHeaderDoc

import java.lang.reflect.Field

class JsondocController {

    def grailsApplication

    def index() {
    }

    def api() {
        render(ApiRegistry.jsondoc as JSON)
    }


    def build() {
        BuildPathMap buildPathMap = new BuildPathMap()
        RulesLight rules = buildPathMap.build()
        println rules
        render rules.rules
        APIUtils.buildApiRegistry(grailsApplication.mainContext, grailsApplication)
    }


//    def build2() {
//
//        //map that store: key=json field name and value = [type: field class, description: field desc,...]
//        Map<String,Map<String,String>> annotationsMap = new HashMap<String,Map<String,String>>()
//
//        def domain = Holders.getGrailsApplication().getDomainClasses().find {
//            it.shortName.equals("Project")
//        }
//        println "domain=$domain"
//
//        //build map field (with super class too)
//        fillAnnotationMap(domain.clazz,annotationsMap)
//        fillAnnotationMap(domain.clazz.superclass,annotationsMap)
//
//        //build map with json
//        def jsonMap = Project.getDataFromDomain(null)
//
//        jsonMap.each {
//            def metadata = annotationsMap.get(it.key)
//            def type = "Undefined"
//            def description = "Undefined"
//            if(metadata) {
//                type = metadata.type
//                description = metadata.description
//            }
//            println "Field " + it.key + " => " + type + " " + description
//        }
//
//    }
//
//    //take clas and fill the map with field metadata (from annotation)
//    void fillAnnotationMap(def domainClass, def annotationsMap) {
//        domainClass.declaredFields.each { field ->
//            println "fields="+field.name
//            if(field.isAnnotationPresent(ApiObjectFieldLight.class)) {
//                def annotation = field.getAnnotation(ApiObjectFieldLight.class)
//                addAnnotationToMap(annotationsMap,field,annotation)
//            }
//            if(field.isAnnotationPresent(ApiObjectFieldsLight.class)) {
//                def annotation = field.getAnnotation(ApiObjectFieldsLight.class)
//                annotation.params().each { apiObjectFieldsLight ->
//                    addAnnotationToMap(annotationsMap,field,apiObjectFieldsLight)
//                }
//            }
//        }
//    }
//
//    //add field metadata to a map. Use field data if annotation data is missing
//    def addAnnotationToMap(Map<String,Map<String,String>> map, Field field, ApiObjectFieldLight annotation) {
//        def annotationData = [:]
//
//        String[] typeChecks = ApiObjectFieldDocLight.getFieldObject(field);
//        if(annotation.allowedType().equals("")) {
//            if(ApiObjectFieldDocLight.isGrailsDomain(field.type.name)) {
//                annotationData['type'] = "long"
//            } else {
//                annotationData['type'] = typeChecks[0];
//            }
//        } else {
//            annotationData['type'] = annotation.allowedType()
//        }
//
//        if(annotation.apiFieldName().equals("")) {
//            annotationData['name'] = field.getName()
//        } else {
//            annotationData['name'] = annotation.apiFieldName()
//        }
//
//        annotationData["description"] = annotation.description()
//
//        map.put(annotationData['name'],annotationData)
//    }



}
