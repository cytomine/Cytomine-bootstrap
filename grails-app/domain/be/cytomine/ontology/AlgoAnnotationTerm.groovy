package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.UserJob
import grails.converters.JSON
import org.apache.log4j.Logger
import be.cytomine.AnnotationDomain

class AlgoAnnotationTerm extends CytomineDomain implements Serializable {

    String annotationClassName
    Long annotationIdent

    Term term
    Term expectedTerm
    Double rate
    UserJob userJob
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

    public String toString() {
        return annotationClassName + " " + annotationIdent + " with term " + term + " from userjob " + userJob + " and  project " + project
    }

    public def setAnnotation(AnnotationDomain annotation) {
        annotationClassName = annotation.class.getName()
        annotationIdent = annotation.id
    }

    public beforeInsert() {
        println "beforeInsert"
        println "getRetrieveAnnotationDomain()="+retrieveAnnotationDomain()
        println "getRetrieveAnnotationDomain()="+retrieveAnnotationDomain()?.image
        println "getRetrieveAnnotationDomain()="+retrieveAnnotationDomain()?.image?.project
        super.beforeInsert()
        println "beforeInsert"
        println "getRetrieveAnnotationDomain()="+retrieveAnnotationDomain()
        println "getRetrieveAnnotationDomain()="+retrieveAnnotationDomain()?.image
        println "getRetrieveAnnotationDomain()="+retrieveAnnotationDomain()?.image?.project
        if(project==null) project = retrieveAnnotationDomain()?.image?.project;
    }

    public AnnotationDomain retrieveAnnotationDomain() {
        Class.forName(annotationClassName, false, Thread.currentThread().contextClassLoader).read(annotationIdent)
    }

    public static AnnotationDomain retrieveAnnotationDomain(String id, String className) {
        Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
    }

    static AlgoAnnotationTerm createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    static AlgoAnnotationTerm createFromData(jsonAlgoAnnotationTerm) {
        def algoAnnotationTerm = new AlgoAnnotationTerm()
        getFromData(algoAnnotationTerm, jsonAlgoAnnotationTerm)
    }

    public boolean suggestionCorrect() {
        return term==expectedTerm
    }

    static AlgoAnnotationTerm getFromData(AlgoAnnotationTerm algoAnnotationTerm, jsonAlgoAnnotationTerm) {

        String annotationId = jsonAlgoAnnotationTerm.annotationIdent.toString()
            def annotation = UserAnnotation.read(annotationId)
            if(!annotation){
                annotation = AlgoAnnotation.read(annotationId)
            }
            if (annotation == null) throw new WrongArgumentException("Annotation was not found with id:" + annotationId)
            algoAnnotationTerm.annotationClassName = annotation.class.getName()
            algoAnnotationTerm.annotationIdent = annotation.id

        String termId = jsonAlgoAnnotationTerm.term.toString()
        if (!termId.equals("null")) {
            algoAnnotationTerm.term = Term.read(termId)
            if (algoAnnotationTerm.term == null) throw new WrongArgumentException("Term was not found with id:" + termId)
        }
        else algoAnnotationTerm.term = null

        String expectedTermId = jsonAlgoAnnotationTerm.expectedTerm.toString()
        if (!expectedTermId.equals("null")) {
            algoAnnotationTerm.expectedTerm = Term.read(expectedTermId)
            if (algoAnnotationTerm.expectedTerm == null) throw new WrongArgumentException("Expected Term was not found with id:" + termId)
        }
        else algoAnnotationTerm.expectedTerm = null


        String userJobId = jsonAlgoAnnotationTerm.user.toString()
        if (!userJobId.equals("null")) {
            algoAnnotationTerm.userJob = UserJob.read(userJobId)
            if (algoAnnotationTerm.userJob == null) throw new WrongArgumentException("UserJob was not found with id:" + userJobId)
        }
        else algoAnnotationTerm.userJob = null

        if (!jsonAlgoAnnotationTerm.rate) jsonAlgoAnnotationTerm.rate = 0
        algoAnnotationTerm.rate = Double.parseDouble(jsonAlgoAnnotationTerm.rate + "")
        return algoAnnotationTerm;
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        Logger.getLogger(this).info("Register custom JSON renderer for " + AlgoAnnotationTerm.class)
        JSON.registerObjectMarshaller(AlgoAnnotationTerm) {
            def returnArray = [:]
            returnArray['id'] = it.id

            returnArray['annotationIdent'] = it.annotationIdent
            returnArray['annotationClassName'] = it.annotationClassName

            returnArray['term'] = it.term?.id
            returnArray['expectedTerm'] = it.expectedTerm?.id
            returnArray['rate'] = it.rate
            returnArray['user'] = it.userJob?.id
            returnArray['project'] = it.project?.id
            return returnArray
        }
    }
}
