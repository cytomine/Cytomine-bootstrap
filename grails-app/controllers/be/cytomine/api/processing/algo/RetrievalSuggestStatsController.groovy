package be.cytomine.api.processing.algo

import be.cytomine.api.RestController
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.processing.structure.ConfusionMatrix
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.utils.Utils

import java.util.TreeMap.Entry

class RetrievalSuggestStatsController extends RestController {

    def annotationService
    def termService
    def algoAnnotationTermService
    def jobService
    def retrievalSuggestedTermJobService
    def retrievalEvolutionJobService

    /**
     * If params.project && params.software, get the last userJob from this software from this project
     * If params.job, get userjob with job
     * @param params
     * @return
     */
    private UserJob retrieveUserJobFromParams(def params) {
        log.info "retrieveUserJobFromParams:" + params
        SecUser userJob = null
        if (params.project != null && params.software != null) {
            Project project = Project.read(params.project)
            Software software = Software.read(params.software)
            if(project && software) userJob = jobService.getLastUserJob(project, software)
        } else if (params.job != null) {
            userJob = UserJob.findByJob(Job.read(params.long('job')))
        }
        return userJob
    }

    def statRetrievalAVG = {

        UserJob userJob = retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        if(userJob.rate==-1 && userJob.job.status==Job.SUCCESS) {
            //avg is not yet compute for this userjob
            userJob.rate = retrievalSuggestedTermJobService.computeRate(userJob.job)
            userJob.save(flush:true)
        }    
        def data = ['avg': userJob.rate]
        responseSuccess(data)
    }

    def statRetrievalConfusionMatrix = {
        def data = []
        UserJob userJob = retrieveUserJobFromParams(params)
        println "get userjob.id=" + userJob.id
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        ConfusionMatrix matrix = retrievalSuggestedTermJobService.computeConfusionMatrix(termService.list(userJob?.job?.project), userJob)
        String matrixJSON = matrix.toJSON()
        data = ['matrix': matrixJSON]
        responseSuccess(data)
    }

    def statRetrievalWorstTerm = {
        def data = []
        UserJob userJob = retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def worstTerms = listWorstTerm(userJob)
        data = ['worstTerms': worstTerms]
        responseSuccess(data)
    }

    def statWorstTermWithSuggestedTerm = {
        log.info "statWorstTermWithSuggestedTerm"
        UserJob userJob = retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def worstTerms = listWorstTermWithSuggestedTerm(userJob)
        def avg =  retrievalSuggestedTermJobService.computeRate(userJob.job)
        def avgAveragedPerClass =  retrievalSuggestedTermJobService.computeAVGAveragePerClass(userJob)
        log.info "avg = " + avg + " avgAveragedPerClass=" + avgAveragedPerClass
        def data = ['worstTerms': worstTerms, 'avg':avg, 'avgMiddlePerClass' : avgAveragedPerClass]
        responseSuccess(data)
    }

    def statRetrievalWorstAnnotation = {
        def data = []
        UserJob userJob = retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def worstTerms = listWorstAnnotationTerm(userJob, 30)
        data = ['worstAnnotations': worstTerms]

        responseSuccess(data)
    }

    def statRetrievalEvolution = {
        UserJob userJob = retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def data = []
        def evolution = retrievalSuggestedTermJobService.listAVGEvolution(userJob)
        if (evolution) data = ['evolution': evolution]
        responseSuccess(data)
    }

    def listWorstTerm(UserJob userJob) {
        Map<Term, Integer> termMap = new HashMap<Term, Integer>()
        List<Term> termList = termService.list(userJob?.job?.project)
        termList.each {
            termMap.put(it, 0)
        }

        def algoAnnotationsTerm = AlgoAnnotationTerm.createCriteria().list {
            eq("userJob", userJob)
            neProperty("term", "expectedTerm")
        }

        algoAnnotationsTerm.each {
            termMap.put(it.expectedTerm, termMap.get(it.expectedTerm) + 1);
        }

        termList.clear()

        termMap.each {  key, value ->
            key.rate = value
            termList.add(key)
        }
        return termList
    }

    def listWorstTermWithSuggestedTerm(def userJob) {
        TreeMap<Long, TreeMap<Long, Integer>> termMap = new TreeMap<Long, TreeMap<Long, Integer>>()
        List<Term> termList = termService.list(userJob?.job?.project)
        termList.each {
            termMap.put(it.id, new TreeMap<Long, Integer>())

        }

        def algoAnnotationsTerm = AlgoAnnotationTerm.createCriteria().list {
            eq("userJob", userJob)
        }

        algoAnnotationsTerm.each {
            TreeMap<Long, Integer> subMap = termMap.get(it.expectedTerm?.id);
            Integer oldValue = subMap.get(it.term?.id)
            if (!oldValue) oldValue = 0
            subMap.put(it.term?.id, oldValue + 1)

            termMap.put(it.expectedTerm?.id, subMap)
        }
        def data = [:]
        termMap.each {
            SortedSet<Entry<Long, Integer>> mapSorted = Utils.entriesSortedByValuesDesc(termMap.get(it.key));
            data[it.key] = []
            long sum = algoAnnotationTermService.computeSumOfValue(mapSorted)

            mapSorted.each { entry ->
                Map map = [:]
                map[entry.key] = Math.round(((entry.value / sum) * 100))
                data[it.key].add(map)
            }
            ///log.info "mapSorted= " + mapSorted
        }
        return data
    }

    def listWorstAnnotationTerm(def userJob, def max) {
        def results = []


        def algoAnnotationsTerm = AlgoAnnotationTerm.createCriteria().list {
            eq("userJob", userJob)
            neProperty("term", "expectedTerm")
            order "rate", "desc"
        }

        for (int i = 0; i < algoAnnotationsTerm.size() && max > results.size(); i++) {
            def result = [:]
            def suggest = algoAnnotationsTerm.get(i)
            //def annotation = suggest.annotation
            //def realTerm = termService.list(annotation, annotation.user)
            result['id'] = suggest.id
            Annotation annotation = suggest.annotation
            result['annotation'] = annotation.id
            result['project'] = annotation.image.id
            result['cropURL'] = annotation.toCropURL()
            result['term'] = suggest.term.id
            result['expectedTerm'] = suggest.expectedTerm.id
            result['rate'] = suggest.rate
            result['user'] = suggest.userJob.id
            result['project'] = suggest.project.id
            results << result;
        }

        return results

    }

}
