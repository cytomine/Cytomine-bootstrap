package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.CytomineException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.api.UrlApi
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger
import org.springframework.cache.annotation.AnnotationCacheOperationSource

import java.lang.annotation.Annotation

class AnnotationProperty extends CytomineDomain implements Serializable{

    String annotationClassName
    Long annotationIdent

    String key
    String value

    static constraints = {
        annotationClassName(nullable: false, blank:  false)
        key(blank: false)
        value(blank: false)
    }

    /**
     * Set annotation (storing class + id)
     * With groovy, you can do: this.annotation = ...
     * @param annotation Annotation to add
     */
    public void setAnnotation(AnnotationDomain annotation) {
        annotationClassName = annotation.class.getName()
        annotationIdent = annotation.id
    }

    /**
     * Get annotation thanks to annotationClassName and annotationIdent
     * @return Annotation concerned with this prediction
     */
    public AnnotationDomain retrieveAnnotationDomain() {
        Class.forName(annotationClassName, false, Thread.currentThread().contextClassLoader).read(annotationIdent)
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist(){
        AnnotationProperty.withNewSession {
            AnnotationProperty annotationProperty = AnnotationProperty.findByAnnotationIdentAndKeyAndValue(annotationIdent, key, value)
            if (annotationProperty != null && (annotationProperty.id!=id))
            {
                throw new AlreadyExistException("AnnotationProperty " + annotationProperty.annotationIdent
                        + "-" + annotationProperty.key + " already exist!")
            }
        }
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller(){
        JSON.registerObjectMarshaller(AnnotationProperty){
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['annotationIdent'] = it.annotationIdent
            returnArray['annotationClassName'] = it.annotationClassName
            returnArray['key'] = it.key
            returnArray['value'] = it.value
            returnArray['created'] = it.created?.time
            returnArray['updated'] = it.updated?.time
            return returnArray
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AnnotationProperty insertDataIntoDomain(def json, def domain = new AnnotationProperty()){
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)

        Long annotationId = JSONUtils.getJSONAttrLong(json, 'annotationIdent', -1)
        if (annotationId == -1) {
            annotationId = JSONUtils.getJSONAttrLong(json, 'annotation', -1)
        }
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(annotationId)
        domain.annotationClassName = annotation.class.getName()
        domain.annotationIdent = annotation.id

        domain.key = JSONUtils.getJSONAttrStr(json,'key')
        domain.value = JSONUtils.getJSONAttrStr(json,'value')

        return domain
    }

}
