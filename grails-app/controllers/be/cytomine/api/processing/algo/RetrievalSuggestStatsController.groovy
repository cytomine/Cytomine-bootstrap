package be.cytomine.api.processing.algo

import be.cytomine.api.RestController
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.processing.Software
import be.cytomine.processing.structure.ConfusionMatrix
import be.cytomine.project.Project
import be.cytomine.security.SecUser
import be.cytomine.security.UserJob
import be.cytomine.utils.Utils

import java.util.TreeMap.Entry

import be.cytomine.AnnotationDomain

class RetrievalSuggestStatsController extends RestController {

    def termService
    def algoAnnotationTermService
    def jobService
    def retrievalSuggestedTermJobService
    def retrievalEvolutionJobService
    def projectService

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
        log.info "get userjob.id=" + userJob.id
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
        long start = System.currentTimeMillis()

        println "1="+ (System.currentTimeMillis()-start)
        def worstTerms = listWorstTermWithSuggestedTerm(userJob)
        println "2="+ (System.currentTimeMillis()-start)
        def avg =  retrievalSuggestedTermJobService.computeRate(userJob.job)
        println "3="+ (System.currentTimeMillis()-start)
        def avgAveragedPerClass =  retrievalSuggestedTermJobService.computeAVGAveragePerClass(userJob)
        println "4="+ (System.currentTimeMillis()-start)

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
        //TODO:: could be optim with no .each loop and a single request
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

        def allCouple = AlgoAnnotationTerm.executeQuery("SELECT at1.expectedTerm.id, at2.term.id, count(*) as sumterm  " +
                "FROM AlgoAnnotationTerm at1, AlgoAnnotationTerm at2  " +
                "WHERE at1.id = at2.id " +
                "AND at1.userJob.id = :userJob " +
                "GROUP BY at1.expectedTerm.id, at2.term.id " +
                "ORDER BY at1.expectedTerm.id, sumterm desc, at2.term.id",[userJob:userJob.id])

        /**
         * All couple =
         * [idTerm1, idTerm1, sum(idTerm1,idTerm1),
         *  idTerm1, idTerm2, sum(idTerm1,idTerm2),
         *  ...
         *  idTerm2, idTerm...
         *  ]
         */

        Map<Long,Map<Long,Long>> resultBySum = [:]
        Map<Long,Long> totalPerTerm = [:]
        Map<Long,SortedSet<Map.Entry<Long, Double>>> resultByAverage = [:]

        //browse each couple <termX,termY,SumPrediction and put it on a map (key = termX, value = Map of all predicted term with sum as value
        allCouple.each { couple ->
            Long expectedTerm = couple[0]
            Long predictedTerm = couple[1]
            Long sum = couple[2]

            if(!resultBySum.containsKey(expectedTerm))
                resultBySum.put(expectedTerm,new HashMap<Long,Long>())

            resultBySum.get(expectedTerm)put(predictedTerm,sum)

            //for each term, compute sum of all predicted term entries (for all terms)
            if(!totalPerTerm.get(expectedTerm))
                totalPerTerm.put(expectedTerm,0)

            totalPerTerm.put(expectedTerm,totalPerTerm.get(expectedTerm)+sum)
        }

        //browse each term...
        resultBySum.each {
            def expectedTerm = it.key
            def allPredictedTerm = it.value
            def totalForTerm = totalPerTerm.get(expectedTerm)

            def predictedTermMap = [:]

            //replace sum by avg
            allPredictedTerm.each { term, sum ->
                predictedTermMap.put(term,(Math.round(((double)sum/(double)totalForTerm)*100))+"#"+term)
            }
            //sort predicted term map on avg (desc). We use suffix #termid because entriesSortedByValuesDesc will
            //erase data if values are equal (term1:3, term2:3,...=> will only keep term1:3 or term2:3).
            //with #termid we don't have similar value
            SortedSet<Map.Entry<Long, Double>> mapSorted = Utils.entriesSortedByValuesDesc(predictedTermMap)
            def list = []
            mapSorted.each {
                def item = [:]
                item.put(it.key,Integer.parseInt(it.value.split("#")[0]))
                list.add(item)
            }

            resultByAverage.put(expectedTerm,list)
        }

        def projectTerms = termService.list(userJob.job.project)

        projectTerms.each {
            if(!resultByAverage.containsKey(it.id))
                resultByAverage.put(it.id,[])
        }
        resultByAverage
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
            AnnotationDomain annotation = suggest.retrieveAnnotationDomain()
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
