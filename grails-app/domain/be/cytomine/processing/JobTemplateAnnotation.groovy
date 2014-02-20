package be.cytomine.processing

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.utils.JSONUtils
import jsondoc.annotation.ApiObjectFieldLight
import jsondoc.annotation.ApiObjectFieldsLight
import org.jsondoc.core.annotation.ApiObject

/**
 * A link between a ROI and a job template
 * The ROI may be a ROIAnnotation or antoher kind of annotation.
 */
@ApiObject(name = "software project", description = "A link between a ROI and a job template")
class JobTemplateAnnotation extends CytomineDomain implements Serializable{

    @ApiObjectFieldLight(description = "The template")
    JobTemplate jobTemplate

    @ApiObjectFieldLight(description = "The annotation class type (roi,user,algo,...)", useForCreation = false)
    String annotationClassName

    @ApiObjectFieldLight(description = "The annotation id")
    Long annotationIdent

    static transients = []

    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
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
     * Get annotation thanks to domainClassName and annotationIdent
     * @return Annotation concerned with this prediction
     */
    public AnnotationDomain retrieveAnnotationDomain() {
        Class.forName(annotationClassName, false, Thread.currentThread().contextClassLoader).read(annotationIdent)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static JobTemplateAnnotation insertDataIntoDomain(def json, def domain = new JobTemplateAnnotation()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        //Extract and read the correct annotation
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(JSONUtils.getJSONAttrLong(json, 'annotationIdent', -1))
        domain.annotationClassName = annotation.class.getName()
        domain.annotationIdent = annotation.id
        domain.jobTemplate = JSONUtils.getJSONAttrDomain(json, "jobTemplate",JobTemplate,true)

        if(domain.jobTemplate?.project!=annotation.project) {
            throw new WrongArgumentException("The project of the jobtemplate (${domain.jobTemplate?.project?.name} is not the same as the annotation project ${annotation.project?.name}")
        }

        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['annotationIdent'] = domain?.annotationIdent
        returnArray['annotationClassName'] = domain?.annotationClassName
        returnArray['jobTemplate'] = domain?.jobTemplate?.id
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return jobTemplate.project
    }
}
