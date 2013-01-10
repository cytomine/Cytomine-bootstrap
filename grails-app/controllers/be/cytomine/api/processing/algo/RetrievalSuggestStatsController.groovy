package be.cytomine.api.processing.algo

import be.cytomine.AnnotationDomain
import be.cytomine.api.RestController
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.processing.Job
import be.cytomine.processing.structure.ConfusionMatrix
import be.cytomine.security.UserJob
import be.cytomine.utils.Utils

/**
 * Controller that provide stats service for retrieval result
 */
class RetrievalSuggestStatsController extends RestController {

    def termService
    def algoAnnotationTermService
    def jobService
    def retrievalSuggestedTermJobService
    def projectService

    /**
     * Get the AVG of prediction for this job
     */
    def statRetrievalAVG = {
        UserJob userJob = jobService.retrieveUserJobFromParams(params)
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

    /**
     * Compute confusion matrix for retrieval prediction
     * Example:
     * X ----------- Term 1 - Term 2 - Term 3
     * Exp Term 1      3        2        0
     * Exp Term 2      0        5        0
     * Exp Term 3      1        0        1
     */
    def statRetrievalConfusionMatrix = {
        UserJob userJob = jobService.retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        ConfusionMatrix matrix = retrievalSuggestedTermJobService.computeConfusionMatrix(termService.list(userJob?.job?.project), userJob)
        String matrixJSON = matrix.toJSON()
        def data = ['matrix': matrixJSON]
        responseSuccess(data)
    }

    /**
     * Compute the term that have poor prediction
     */
    def statRetrievalWorstTerm = {
        UserJob userJob = jobService.retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def worstTerms = listWorstTerm(userJob)
        def data = ['worstTerms': worstTerms]
        responseSuccess(data)
    }

    /**
     *  Compute the term that have poor prediction and compute the predicted term for each case
     */
    def statWorstTermWithSuggestedTerm = {
        log.info "statWorstTermWithSuggestedTerm"
        UserJob userJob = jobService.retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def worstTerms = listWorstTermWithSuggestedTerm(userJob)
        def avg =  retrievalSuggestedTermJobService.computeRate(userJob.job)
        def avgAveragedPerClass = retrievalSuggestedTermJobService.computeAVGAveragePerClass(userJob)

        log.info "avg = " + avg + " avgAveragedPerClass=" + avgAveragedPerClass
        def data = ['worstTerms': worstTerms, 'avg':avg, 'avgMiddlePerClass' : avgAveragedPerClass]
        responseSuccess(data)
    }

    /**
     * Compute the worst annotation (annotation with wrong prediction and hight similarity rate)
     */
    def statRetrievalWorstAnnotation = {
        UserJob userJob = jobService.retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def worstTerms = listWorstAnnotationTerm(userJob, 30)
        def data = ['worstAnnotations': worstTerms]
        responseSuccess(data)
    }

    /**
     * Compute the all retrieval AVG during the time (for all job)
     */
    def statRetrievalEvolution = {
        UserJob userJob = jobService.retrieveUserJobFromParams(params)
        if(!userJob) {
            responseNotFound("UserJob","Params", params)
            return null
        }
        def data = []
        def evolution = retrievalSuggestedTermJobService.listAVGEvolution(userJob)
        if (evolution) {
            data = ['evolution': evolution]
        }
        responseSuccess(data)
    }

    /**
     * List terms sort by "worst prediction" for the userjob data
     * @param userJob User
     * @return Term
     * //TODO:: could be optim with no .each loop and a single request
     */
    def listWorstTerm(UserJob userJob) {

        //get a map with all term and a 0 value
        Map<Term, Integer> termMap = new HashMap<Term, Integer>()
        List<Term> termList = termService.list(userJob?.job?.project)
        termList.each {
            termMap.put(it, 0)
        }

        //get all prediction for this job that are not good
        def algoAnnotationsTerm = AlgoAnnotationTerm.createCriteria().list {
            eq("userJob", userJob)
            neProperty("term", "expectedTerm")
        }

        //increment the value for each term
        algoAnnotationsTerm.each {
            termMap.put(it.expectedTerm, termMap.get(it.expectedTerm) + 1);
        }

        //make a new list with the rate for each term
        termList.clear()
        termMap.each {  key, value ->
            key.rate = value
            termList.add(key)
        }
        return termList
    }

    /**
     * List term ordered by worst term prediction
     * @param userJob User job
     * @return term list
     * //TODO: could be improve with a SQL request (without hibernate)
     */
    def listWorstTermWithSuggestedTerm(def userJob) {

        //for each term, compute the total predicted term for all term
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

    /**
     * Do list of max 'max' annotation order by worst prediction
     * worst prediction = (exp term <> term & rate is hight)
     * @param userJob
     * @param max
     * @return
     * //TODO:: can be optim with a single SQL request
     */
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
