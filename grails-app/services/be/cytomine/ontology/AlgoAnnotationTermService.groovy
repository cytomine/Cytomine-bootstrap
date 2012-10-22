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
import be.cytomine.AnnotationDomain

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

    def list(AnnotationDomain annotation) {
        AlgoAnnotationTerm.findAllByAnnotationIdent(annotation.id)
    }

    def read(AnnotationDomain annotation, Term term, UserJob userJob) {
        AlgoAnnotationTerm.findWhere(annotationIdent: annotation.id, term: term, userJob: userJob)
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
        def result = deleteAlgoAnnotationTerm(json.userannotation, json.term, json.user, currentUser,null)
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
    def deleteAlgoAnnotationTermFromAllUser(AnnotationDomain annotation, User currentUser, Transaction transaction) {
        //Delete all annotation term
        def suggestedterm = AlgoAnnotationTerm.findAllByAnnotationIdent(annotation)
        log.info "Delete old suggestedterm= " + suggestedterm.size()

        suggestedterm.each { sugterm ->
            log.info "unlink sugterm:" + sugterm.id
            deleteAlgoAnnotationTerm(sugterm.retrieveAnnotationDomain().id, sugterm.term.id, sugterm.userJob.id, currentUser,transaction)
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
            deleteAlgoAnnotationTerm(algoterm.retrieveAnnotationDomain().id, algoterm.term.id, algoterm.userJob.id, currentUser,transaction)
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
        return responseService.createResponseMessage(domain, [domain.term?.name, domain.retrieveAnnotationDomain().id, domain.userJob], printMessage, "Add", domain.getCallBack())
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
        def response = responseService.createResponseMessage(domain, [domain.term.name, domain.retrieveAnnotationDomain().id, domain.userJob], printMessage, "Delete", domain.getCallBack())
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
        def response = responseService.createResponseMessage(domain, [domain.term.name, domain.retrieveAnnotationDomain().id, domain.userJob], printMessage, "Edit", domain.getCallBack())
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
        Long idAnnotation = Long.parseLong(json.annotationIdent)
        Term term = Term.read(json.term)
        UserJob userJob = UserJob.read(json.userJob)
        AlgoAnnotationTerm domain = AlgoAnnotationTerm.findWhere(annotationIdent: idAnnotation, term: term, userJob: userJob)
        if (!domain) throw new ObjectNotFoundException("SuggestedTerm was not found with annotation:$json.annotationIdent,term:$term,userJob:$userJob")
        return domain
    }

     double computeAVG(def userJob) {
        log.info "userJob="+userJob
        log.info "userJob.id="+userJob.id
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
         log.info "nbTermNotCorrect="+nbTermCorrect +" nbTermTotal="+nbTermTotal
        return (double) (nbTermCorrect / nbTermTotal)
    }

    double computeAVG(def userJob, Term term) {
       log.info "userJob="+userJob
       log.info "userJob.id="+userJob.id
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
        log.info "nbTermNotCorrect="+nbTermCorrect +" nbTermTotal="+nbTermTotal
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
            if (it.term && it.expectedTerm) matrix.incrementEntry(it.expectedTerm?.id + "", it.term?.id + "")
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
        if(userJobs.isEmpty()) return null

        def data = []
        int count = 0;
        def annotations = null;
        //List<Annotation> annotations = Annotation.findAllByProject(project,[sort:'created', order:"desc"])
        if(!term) {
            annotations = UserAnnotation.executeQuery("select a.created from UserAnnotation a where a.project = ?  order by a.created desc", [project])
        }
        else {
            log.info "Search on term " + term.name
            annotations = UserAnnotation.executeQuery("select b.created from UserAnnotation b where b.project = ? and b.id in (select x.userAnnotation.id from AnnotationTerm x where x.term = ?)  order by b.created desc", [project,term])
        }
        userJobs.each {
            def userJobIt = it
            def item = [:]
            Date stopDate = userJobIt.created

            //we browse userjob (oreder desc creation).
            //For each userjob, we browse annotation (oreder desc creation) and we count the number of annotation
            //that are most recent than userjob, we subsitute this count from annotation.list()
            //=> not needed to browse n times annotations list, juste 1 time.
            while(count<annotations.size()) {
                if(annotations.get(count)<stopDate) break;
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
