package be.cytomine.ontology

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.AnnotationDomain
import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.UserJob
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

/**
 * Term added to an annotation by a job
 * Annotation can be:
 * -algo annotation (create by a job)
 * -user annotation (create by a real user)
 */
@RestApiObject(name = "algo annotation term", description ="Term added to an annotation by a job. Annotation can be: -algo annotation (create by a job) or -user annotation (create by a real user)")
class AlgoAnnotationTerm extends CytomineDomain implements Serializable {

    /**
     * Term can be add to user or algo annotation
     * Rem: 'AnnotationDomain annotation' diden't work because user and algo annotation
     * are store in different table
     * So we store annotation type and annotation id
     */
    @RestApiObjectField(description = "The annotation class type (user or algo)")
    String annotationClassName

    @RestApiObjectField(description = "The annotation id")
    Long annotationIdent

    /**
     * Predicted term
     */
    @RestApiObjectField(description = "The term id")
    Term term

    /**
     * Real term (added by user)
     */
    @RestApiObjectField(description = "The real term id, the term added by the user previously")
    Term expectedTerm

    /**
     * Certainty rate
     */
    @RestApiObjectField(description = "The reliability of the prediction")
    Double rate

    /**
     * Virtual user that made the prediction
     */
    @RestApiObjectField(description = "The user job id", apiFieldName = "user")
    UserJob userJob

    /**
     * Project for the prediction
     * rem: redundance for optim (we should get it with retrieveAnnotationDomain().project)
     */
    @RestApiObjectField(description = "The project id")
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

        if(domain.term?.ontology!=annotation.project.ontology) {
            throw new WrongArgumentException("Term ${domain.term} from ontology ${domain.term?.ontology} is not in ontology from the annotation project (${annotation?.project?.ontology?.id}")
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
        returnArray['annotation'] = domain?.annotationIdent
        returnArray['term'] = domain?.term?.id
        returnArray['expectedTerm'] = domain?.expectedTerm?.id
        returnArray['rate'] = domain?.rate
        returnArray['user'] = domain?.userJob?.id
        returnArray['project'] = domain?.project?.id
        return returnArray
    }
}
