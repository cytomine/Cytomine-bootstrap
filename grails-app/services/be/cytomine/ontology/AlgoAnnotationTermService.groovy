package be.cytomine.ontology

import be.cytomine.AnnotationDomain
import be.cytomine.Exception.ObjectNotFoundException
import be.cytomine.SecurityCheck
import be.cytomine.command.AddCommand
import be.cytomine.command.DeleteCommand
import be.cytomine.command.Transaction
import be.cytomine.processing.Job
import be.cytomine.processing.structure.ConfusionMatrix
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.User
import be.cytomine.security.UserJob
import be.cytomine.utils.ModelService
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize

class AlgoAnnotationTermService extends ModelService {

    static transactional = true
    def cytomineService
    def commandService

    def currentDomain() {
        return AlgoAnnotationTerm
    }

    @PreAuthorize("#annotation.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def list(AnnotationDomain annotation) {
        AlgoAnnotationTerm.findAllByAnnotationIdent(annotation.id)
    }

    @PreAuthorize("#job.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def count(Job job) {
        long total = 0
        List<UserJob> users = UserJob.findAllByJob(job)
        users.each {
            total = total + AlgoAnnotationTerm.countByUserJob(it)
        }
        total
    }

    @PreAuthorize("#annotation.project.hasPermission('READ') or hasRole('ROLE_ADMIN')")
    def read(AnnotationDomain annotation, Term term, UserJob userJob) {
        if (userJob) {
            AlgoAnnotationTerm.findWhere(annotationIdent: annotation.id, term: term, userJob: userJob)
        } else {
            AlgoAnnotationTerm.findWhere(annotationIdent: annotation.id, term: term)
        }

    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    @PreAuthorize("#security.checkProjectAccess()")
    def add(def json, SecurityCheck security) {
        SecUser currentUser = cytomineService.getCurrentUser()
        SecUser creator = SecUser.read(json.user)
        if (!creator)
            json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser), json)
    }

    def delete(AlgoAnnotationTerm at, Transaction transaction = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        def json = JSON.parse("{id: ${at.id}}")
        return executeCommand(new DeleteCommand(user: currentUser,transaction:transaction), json)
    }

    def afterAdd(def domain, def response) {
        response.data['annotation'] = response.data.userannotation
        response.data.remove('algoannotation')
    }

    def afterDelete(def domain, def response) {
        response.data['annotation'] = response.data.userannotation
        response.data.remove('algoannotation')
    }

    def afterUpdate(def domain, def response) {
        response.data['annotation'] = response.data.userannotation
        response.data.remove('algoannotation')
    }


    def getStringParamsI18n(def domain) {
        return [domain.term.name, domain.retrieveAnnotationDomain().id, domain.userJob]
    }

    /**
     * Compute Success rate AVG for all algo annotation term of userJob
     */
    double computeAVG(def userJob) {
        log.info "userJob=" + userJob?.id

        def nbTermTotal = AlgoAnnotationTerm.createCriteria().count {
            eq("userJob", userJob)
            isNotNull("expectedTerm")
        }
        if (nbTermTotal == 0) {
            throw new Exception("UserJob has no algo-annotation-term!")
        }

        def nbTermCorrect = AlgoAnnotationTerm.createCriteria().count {
            eq("userJob", userJob)
            isNotNull("term")
            isNotNull("expectedTerm")
            eqProperty("term", "expectedTerm")
        }
        return (double) (nbTermCorrect / nbTermTotal)
    }

    /**
     * Compute Success rate AVG for all algo annotation term of userJob and a term
     */
    double computeAVG(def userJob, Term term) {

        def nbTermTotal = AlgoAnnotationTerm.createCriteria().count {
            eq("userJob", userJob)
            eq("expectedTerm", term)
        }
        if (nbTermTotal == 0) {
            throw new Exception("UserJob has no algo-annotation-term!")
        }

        def nbTermCorrect = AlgoAnnotationTerm.createCriteria().count {
            eq("userJob", userJob)
            eq("expectedTerm", term)
            eqProperty("term", "expectedTerm")
        }
        return (double) (nbTermCorrect / nbTermTotal)
    }

    /**
     * Compute suceess rate AVG per term for a userjob
     * if AVG success for Term x = 90% && Term y = 20%,
     * Return will be ((90+20)/2)%
     */
    double computeAVGAveragePerClass(def userJob) {
        def terms = userJob.job.project.ontology.terms()
        double total = 0
        int nbTermNotEmpty = 0

        terms.each { term ->
            def nbTermCorrect = AlgoAnnotationTerm.createCriteria().count {
                eq("userJob", userJob)
                eq("expectedTerm", term)
                eqProperty("term", "expectedTerm")
            }
            def nbTermTotal = AlgoAnnotationTerm.createCriteria().count {
                eq("userJob", userJob)
                eq("expectedTerm", term)
            }

            if (nbTermTotal != 0) {
                total = total + (double) (nbTermCorrect / nbTermTotal)
                nbTermNotEmpty++
            }
        }
        double avg = 0
        if (nbTermNotEmpty != 0)
            avg = (double) (total / nbTermNotEmpty)
        return avg
    }

    /**
     * Compute full Confusion Matrix for all terms from projectTerms and all algo annotation term from userJob
     */
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

    /**
     * Get AlgoAnnotationTerm prediction success AVG evolution for all userJobs and a project
     * For each userJobs, map its date with the success rate of its result
     */
    def listAVGEvolution(List<UserJob> userJobs, Project project) {
        listAVGEvolution(userJobs, project, null)
    }

    /**
     * Get AlgoAnnotationTerm prediction success AVG evolution for all userJobs for a specific term
     * For each userJobs, map its date with the success rate of its result
     * if term is null, compute success rate for all term
     */
    def listAVGEvolution(List<UserJob> userJobs, Project project, Term term) {

        if (userJobs.isEmpty()) {
            return null
        }

        def data = []
        int count = 0;
        def annotations = null;

        if (!term) {
            annotations = UserAnnotation.executeQuery("select a.created from UserAnnotation a where a.project = ?  order by a.created desc", [project])
        }
        else {
            annotations = UserAnnotation.executeQuery("select b.created from UserAnnotation b where b.project = ? and b.id in (select x.userAnnotation.id from AnnotationTerm x where x.term = ?)  order by b.created desc", [project, term])
        }
        userJobs.each {
            def userJobIt = it
            def item = [:]
            Date stopDate = userJobIt.created

            //we browse userjob (oreder desc creation).
            //For each userjob, we browse annotation (oreder desc creation) and we count the number of annotation
            //that are most recent than userjob, we subsitute this count from annotation.list()
            //=> not needed to browse n times annotations list, juste 1 time.
            while (count < annotations.size()) {
                if (annotations.get(count) < stopDate) break;
                count++;
            }
            item.size = annotations.size() - count;

            try {
                item.date = userJobIt.created.getTime()
                if (term)
                    item.avg = computeAVG(userJobIt, term)
                else {
                    if (userJobIt.rate == -1 && userJobIt.job.status == Job.SUCCESS) {
                        userJobIt.rate = computeAVG(userJobIt)
                        userJobIt.save(flush: true)
                    }
                    item.avg = userJobIt.rate
                }

                data << item
            } catch (Exception e) {
                log.info e
            }
        }
        return data
    }
}
