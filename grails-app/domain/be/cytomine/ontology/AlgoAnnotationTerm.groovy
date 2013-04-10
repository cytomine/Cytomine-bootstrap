package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.project.Project
import be.cytomine.security.UserJob
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Term added to an annotation by a job
 * Annotation can be:
 * -algo annotation (create by a job)
 * -user annotation (create by a real user)
 */
class AlgoAnnotationTerm extends CytomineDomain implements Serializable {

    /**
     * Term can be add to user or algo annotation
     * Rem: 'AnnotationDomain annotation' diden't work because user and algo annotation
     * are store in different table
     * So we store annotation type and annotation id
     */
    String annotationClassName
    Long annotationIdent

    /**
     * Predicted term
     */
    Term term

    /**
     * Real term (added by user)
     */
    Term expectedTerm

    /**
     * Certainty rate
     */
    Double rate

    /**
     * Virtual user that made the prediction
     */
    UserJob userJob

    /**
     * Project for the prediction
     * rem: redundance for optim (we should get it with retrieveAnnotationDomain().project)
     */
    Project project

    static constraints = {
        annotationClassName nullable: false
        annotationIdent nullable: false
        term nullable: true
        expectedTerm nullable: true
        rate(min: 0d, max: 1d)
        userJob nullable: false
        project nullable: true
    }

    public beforeInsert() {
        super.beforeInsert()
        if (project == null) project = retrieveAnnotationDomain()?.image?.project;
    }

    public String toString() {
        return annotationClassName + " " + annotationIdent + " with term " + term + " from userjob " + userJob + " and  project " + project
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

    public static AnnotationDomain retrieveAnnotationDomain(String id, String className) {
        Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
    }

    public static AnnotationDomain retrieveAnnotationDomain(Long id, String className) {
        Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AlgoAnnotationTerm insertDataIntoDomain(def json, def domain = new AlgoAnnotationTerm()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        //Extract and read the correct annotation
        Long annotationId = JSONUtils.getJSONAttrLong(json, 'annotationIdent', -1)
        if (annotationId == -1) {
            annotationId = JSONUtils.getJSONAttrLong(json, 'annotation', -1)
        }
        AnnotationDomain annotation = AnnotationDomain.getAnnotationDomain(annotationId)
        domain.annotationClassName = annotation.class.getName()
        domain.annotationIdent = annotation.id
        domain.term = JSONUtils.getJSONAttrDomain(json, "term", new Term(), false)
        domain.expectedTerm = JSONUtils.getJSONAttrDomain(json, "expectedTerm", new Term(), false)
        domain.userJob = JSONUtils.getJSONAttrDomain(json, "user", new UserJob(), false)
        domain.rate = JSONUtils.getJSONAttrDouble(json, 'rate', 0)
        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + AlgoAnnotationTerm.class)
        JSON.registerObjectMarshaller(AlgoAnnotationTerm) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['annotationIdent'] = it.annotationIdent
            returnArray['annotationClassName'] = it.annotationClassName
            returnArray['annotation'] = it.annotationIdent
            returnArray['term'] = it.term?.id
            returnArray['expectedTerm'] = it.expectedTerm?.id
            returnArray['rate'] = it.rate
            returnArray['user'] = it.userJob?.id
            returnArray['project'] = it.project?.id
            return returnArray
        }
    }
}
