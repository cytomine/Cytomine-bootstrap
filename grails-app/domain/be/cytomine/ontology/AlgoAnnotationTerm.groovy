package be.cytomine.ontology

import be.cytomine.CytomineDomain
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.project.Project
import be.cytomine.security.UserJob
import grails.converters.JSON

class AlgoAnnotationTerm extends CytomineDomain implements Serializable {

    Annotation annotation
    Term term
    Term expectedTerm
    Double rate
    UserJob userJob
    Project project

    static constraints = {
        annotation nullable: false
        term nullable: true
        expectedTerm nullable: true
        rate(min: 0d, max: 1d)
        userJob nullable: false
        project nullable: true
    }

    public beforeInsert() {
        super.beforeInsert()
        project = annotation?.image?.project;
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
        println "=========jsonAlgoAnnotationTerm======="  + jsonAlgoAnnotationTerm

        String annotationId = jsonAlgoAnnotationTerm.annotation.toString()
        if (!annotationId.equals("null")) {
            algoAnnotationTerm.annotation = Annotation.read(annotationId)
            if (algoAnnotationTerm.annotation == null) throw new WrongArgumentException("Annotation was not found with id:" + annotationId)
        }
        else algoAnnotationTerm.annotation = null

        String termId = jsonAlgoAnnotationTerm.term.toString()
        println "termID="+termId
        if (!termId.equals("null")) {
            algoAnnotationTerm.term = Term.read(termId)
            if (algoAnnotationTerm.term == null) throw new WrongArgumentException("Term was not found with id:" + termId)
        }
        else algoAnnotationTerm.term = null

        String expectedTermId = jsonAlgoAnnotationTerm.expectedTerm.toString()
        println "expectedTermId="+expectedTermId
        if (!expectedTermId.equals("null")) {
            algoAnnotationTerm.expectedTerm = Term.read(expectedTermId)
            if (algoAnnotationTerm.expectedTerm == null) throw new WrongArgumentException("Expected Term was not found with id:" + termId)
        }
        else algoAnnotationTerm.expectedTerm = null


        String userJobId = jsonAlgoAnnotationTerm.user.toString()
        println "userJobId="+userJobId
        if (!userJobId.equals("null")) {
            algoAnnotationTerm.userJob = UserJob.read(userJobId)
            if (algoAnnotationTerm.userJob == null) throw new WrongArgumentException("UserJob was not found with id:" + userJobId)
        }
        else algoAnnotationTerm.userJob = null

        if (!jsonAlgoAnnotationTerm.rate) jsonAlgoAnnotationTerm.rate = 0
        algoAnnotationTerm.rate = Double.parseDouble(jsonAlgoAnnotationTerm.rate + "")

        println ">>>>> user " + algoAnnotationTerm.userJob + "<<<<"
        println ">>>>> term " + algoAnnotationTerm.term + "<<<<"
        return algoAnnotationTerm;
    }


    def getIdAnnotation() {
//        if (this.annotationId) return this.annotationId
//        else return this.annotation?.id
        return this.annotation?.id
    }

    def getIdTerm() {
//        if (this.termId) return this.termId
//        else return this.term?.id
        return this.term?.id
    }

    def getIdExpectedTerm() {
//        if (this.expectedTermId) return this.expectedTermId
//        else return this.expectedTerm?.id
        return this.expectedTerm?.id
    }

    def getIdUserJob() {
//        if (this.userJobId) return this.userJobId
//        else return this.userJob?.id
        return this.userJob?.id
    }

    def getIdProject() {
//        if (this.projectId) return this.projectId
//        else return this.project?.id
        return this.project?.id
    }

    static void registerMarshaller(String cytomineBaseUrl) {
        println "Register custom JSON renderer for " + AlgoAnnotationTerm.class
        JSON.registerObjectMarshaller(AlgoAnnotationTerm) {
            def returnArray = [:]
            returnArray['id'] = it.id
            returnArray['annotation'] = it.getIdAnnotation()
            returnArray['term'] = it.getIdTerm()
            returnArray['expectedTerm'] = it.getIdExpectedTerm()
            returnArray['rate'] = it.rate
            returnArray['user'] = it.getIdUserJob()
            returnArray['project'] = it.getIdProject()
            return returnArray
        }
    }
}
