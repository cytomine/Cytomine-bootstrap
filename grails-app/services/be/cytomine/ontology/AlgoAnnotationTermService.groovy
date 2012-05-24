package be.cytomine.ontology

import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.ModelService
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.processing.Job
import be.cytomine.processing.structure.ConfusionMatrix
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.security.UserJob
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

import java.util.TreeMap.Entry

class AlgoAnnotationTermService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService
    def domainService
    def jobService

    boolean saveOnUndoRedoStack = true

    def list() {
        AlgoAnnotationTerm.list()
    }

    def list(Annotation annotation) {
        AlgoAnnotationTerm.findAllByAnnotation(annotation)
    }

    def read(Annotation annotation, Term term, UserJob userJob) {
        AlgoAnnotationTerm.findWhere(annotation: annotation, term: term, userJob: userJob)
    }

    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecUser creator = SecUser.read(json.user)
        if(!creator)
            json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def delete(def domain,def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def result = deleteAlgoAnnotationTerm(json.annotation, json.term, json.userJob, currentUser,null)
        return result
    }

    def update(def domain,def json) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Delete an annotation term
     */
    def deleteAlgoAnnotationTerm(def idAnnotation, def idTerm, def idUserJob, User currentUser, Transaction transaction) {
        def json = JSON.parse("{annotation: $idAnnotation, term: $idTerm, userJob: $idUserJob}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    /**
     * Delete all term map for annotation
     */
    def deleteAlgoAnnotationTermFromAllUser(Annotation annotation, User currentUser, Transaction transaction) {
        //Delete all annotation term
        def suggestedterm = AlgoAnnotationTerm.findAllByAnnotation(annotation)
        log.info "Delete old suggestedterm= " + suggestedterm.size()

        suggestedterm.each { sugterm ->
            log.info "unlink sugterm:" + sugterm.id
            deleteAlgoAnnotationTerm(sugterm.annotation.id, sugterm.term.id, sugterm.userJob.id, currentUser,transaction)
        }
    }

    /**
     * Delete all term map by user for term
     */
    def deleteAlgoAnnotationTermFromAllUser(Term term, User currentUser,Transaction transaction) {
        //Delete all annotation term
        def algoannotationterm = AlgoAnnotationTerm.findAllByTerm(term)
        log.info "Delete old algoannotationterm= " + algoannotationterm.size()

        algoannotationterm.each { algoterm ->
            log.info "unlink sugterm:" + algoterm.id
            deleteAlgoAnnotationTerm(algoterm.annotation.id, algoterm.term.id, algoterm.userJob.id, currentUser,transaction)
        }
    }

    /**
     * Restore domain which was previously deleted
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def create(JSONObject json, boolean printMessage) {
        create(AlgoAnnotationTerm.createFromDataWithId(json), printMessage)
    }

    def create(AlgoAnnotationTerm domain, boolean printMessage) {
        //Save new object
        domainService.saveDomain(domain)
        //Build response message
        return responseService.createResponseMessage(domain, [domain.term?.name, domain.annotation.id, domain.userJob], printMessage, "Add", domain.getCallBack())
    }
    /**
     * Destroy domain which was previously added
     * @param json domain info

     * @param printMessage print message or not
     * @return response
     */
    def destroy(JSONObject json, boolean printMessage) {
        //Get object to delete
        destroy(AlgoAnnotationTerm.get(json.id), printMessage)
    }

    def destroy(AlgoAnnotationTerm domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.term.name, domain.annotation.id, domain.userJob], printMessage, "Delete", domain.getCallBack())
        //Delete object
        domainService.deleteDomain(domain)
        return response
    }

    /**
     * Edit domain which was previously edited
     * @param json domain info
     * @param printMessage print message or not
     * @return response
     */
    def edit(JSONObject json, boolean printMessage) {
        //Rebuilt previous state of object that was previoulsy edited
        edit(fillDomainWithData(new AlgoAnnotationTerm(), json), printMessage)
    }

    def edit(AlgoAnnotationTerm domain, boolean printMessage) {
        //Build response message
        def response = responseService.createResponseMessage(domain, [domain.term.name, domain.annotation.id, domain.userJob], printMessage, "Edit", domain.getCallBack())
        //Save update
        domainService.saveDomain(domain)
        return response
    }

    /**
     * Create domain from JSON object
     * @param json JSON with new domain info
     * @return new domain
     */
    AlgoAnnotationTerm createFromJSON(def json) {
        return AlgoAnnotationTerm.createFromData(json)
    }

    /**
     * Retrieve domain thanks to a JSON object
     * @param json JSON with new domain info
     * @return domain retrieve thanks to json
     */
    def retrieve(JSONObject json) {
        //Retrieve domain
        Annotation annotation = Annotation.read(json.annotation)
        Term term = Term.read(json.term)
        UserJob userJob = UserJob.read(json.userJob)
        AlgoAnnotationTerm domain = AlgoAnnotationTerm.findWhere(annotation: annotation, term: term, userJob: userJob)
        if (!domain) throw new ObjectNotFoundException("SuggestedTerm was not found with annotation:$annotation,term:$term,userJob:$userJob")
        return domain
    }

     double computeAVG(def userJob) {
        println "userJob="+userJob
        println "userJob.id="+userJob.id
        def nbTermCorrect = AlgoAnnotationTerm.createCriteria().count {
            eq("userJob", userJob)
            isNotNull("term")
            isNotNull("expectedTerm")
            eqProperty("term", "expectedTerm")
        }
        def nbTermTotal = AlgoAnnotationTerm.createCriteria().count {
            eq("userJob", userJob)
            isNotNull("expectedTerm")
        }
         if(nbTermTotal==0) throw new Exception("UserJob has no algo-annotation-term!")
         println "nbTermNotCorrect="+nbTermCorrect +" nbTermTotal="+nbTermTotal
        return (double) (nbTermCorrect / nbTermTotal)
    }

    double computeAVG(def userJob, Term term) {
       println "userJob="+userJob
       println "userJob.id="+userJob.id
       def nbTermCorrect = AlgoAnnotationTerm.createCriteria().count {
           eq("userJob", userJob)
           eq("expectedTerm",term)
           eqProperty("term", "expectedTerm")
       }
       def nbTermTotal = AlgoAnnotationTerm.createCriteria().count {
           eq("userJob", userJob)
           eq("expectedTerm",term)
       }
        if(nbTermTotal==0) throw new Exception("UserJob has no algo-annotation-term!")
        println "nbTermNotCorrect="+nbTermCorrect +" nbTermTotal="+nbTermTotal
       return (double) (nbTermCorrect / nbTermTotal)
   }

    double computeAVGAveragePerClass(def userJob) {

        def terms = userJob.job.project.ontology.terms()

        double total = 0
        int nbTermNotEmpty = 0

        terms.each { term ->

            def nbTermCorrect = AlgoAnnotationTerm.createCriteria().count {
                eq("userJob", userJob)
                eq("expectedTerm",term)
                eqProperty("term", "expectedTerm")
            }
            def nbTermTotal = AlgoAnnotationTerm.createCriteria().count {
                eq("userJob", userJob)
                eq("expectedTerm",term)
            }

            if(nbTermTotal!=0) {
                total = total + (double)(nbTermCorrect/nbTermTotal)
                nbTermNotEmpty++
            }


        }
        double avg = 0
        if(nbTermNotEmpty!=0)
            avg = (double)(total/nbTermNotEmpty)
        return avg
   }

     ConfusionMatrix computeConfusionMatrix(List<Term> projectTerms, def userJob) {
        Collections.sort(projectTerms);
        def projectTermsId = projectTerms.collect {it.id + ""}
        ConfusionMatrix matrix = new ConfusionMatrix(projectTermsId);
        def algoAnnotationsTerm = AlgoAnnotationTerm.findAllByUserJob(userJob);

        algoAnnotationsTerm.each {
            if (it.term && it.expectedTerm) matrix.addEntry(it.expectedTerm?.id + "", it.term?.id + "")
            //matrix.print()
        }
        return matrix
    }



    long computeSumOfValue(SortedSet<Entry<Long, Integer>> mapSorted) {
        long sum = 0
        mapSorted.each { entry ->
            sum = sum + entry.value
        }
        return sum
    }

    def listAVGEvolution(List<UserJob> userJobs, Project project) {
        listAVGEvolution(userJobs,project,null)
    }
    def listAVGEvolution(List<UserJob> userJobs, Project project, Term term) {
        def data = []
        int count = 0;

        List<Annotation> annotations = null
        if(term==null)
            annotations = Annotation.findAllByProject(project,[sort:'created', order:"desc"])
        else {
            annotations = Annotation.withCriteria() {
                eq('project', project)
                annotationTerm {
                    eq('term', term)
                }
                order("created", "desc")
            }.unique()
        }

        if(userJobs.isEmpty()) return null

        userJobs.each {
            def userJobIt = it
            def item = [:]

            Date stopDate = userJobIt.created

            //we browse userjob (oreder desc creation).
            //For each userjob, we browse annotation (oreder desc creation) and we count the number of annotation
            //that are most recent than userjob, we subsitute this count from annotation.list()
            //=> not needed to browse n times annotations list, juste 1 time.
            while(count<annotations.size()) {
                if(annotations.get(count).created<stopDate) break;
                count++;
            }
            item.size = annotations.size()-count;

            try {
                item.date = userJobIt.created.getTime()
                if(term)
                    item.avg = computeAVG(userJobIt,term)
                else {
                    if(userJobIt.rate==-1 && userJobIt.job.status==Job.SUCCESS) {
                        userJobIt.rate = computeAVG(userJobIt)
                        userJobIt.save(flush: true)
                    }
                    item.avg = userJobIt.rate
                }
                    
                data << item
            } catch(Exception e) {
                log.info e
            }
        }
        return data
    }



}
