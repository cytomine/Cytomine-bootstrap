package be.cytomine.api

import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.project.Project

import be.cytomine.processing.structure.ConfusionMatrix
import be.cytomine.security.SecUser
import be.cytomine.ontology.AlgoAnnotationTerm
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.utils.ValueComparator
import be.cytomine.utils.Utils
import java.util.TreeMap.Entry
import be.cytomine.security.UserJob
import be.cytomine.processing.Job
import be.cytomine.processing.Software

class StatsController extends RestController {

    def annotationService
    def termService
    def algoAnnotationTermService
    def jobService

    /**
     * If params.project && params.software, get the last userJob from this software from this project
     * If params.job, get userjob with job
     * @param params
     * @return
     */
    UserJob retrieveUserJobFromParams(def params) {
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
        double avg = algoAnnotationTermService.computeAVG(userJob)
        def data = ['avg': avg]
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
        ConfusionMatrix matrix = algoAnnotationTermService.computeConfusionMatrix(termService.list(userJob?.job?.project), userJob)
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
        def avg =  algoAnnotationTermService.computeAVG(userJob)
        def avgAveragedPerClass =  algoAnnotationTermService.computeAVGAveragePerClass(userJob)
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
        def evolution = algoAnnotationTermService.listAVGEvolution(userJob)
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
            TreeMap<Long, Integer> subMap = termMap.get(it.getIdExpectedTerm());
            Integer oldValue = subMap.get(it.getIdTerm())
            if (!oldValue) oldValue = 0
            subMap.put(it.getIdTerm(), oldValue + 1)

            termMap.put(it.getIdExpectedTerm(), subMap)
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
            def suggest = algoAnnotationsTerm.get(i)
            //def annotation = suggest.annotation
            //def realTerm = termService.list(annotation, annotation.user)
            results.add(suggest);
        }

        return results

    }


    def statUserAnnotations = {
        Project project = Project.read(params.id)
        if (project == null) responseNotFound("Project", params.id)
        def terms = Term.findAllByOntology(project.getOntology())
        def nbAnnotationsByUserAndTerms = AnnotationTerm.createCriteria().list {
            inList("term", terms)
            join("annotation")
            createAlias("annotation", "a")
            projections {
                eq("a.project", project)
                groupProperty("a.user.id")
                groupProperty("term.id")
                count("term")
            }
        }

        Map<Long, Object> result = new HashMap<Long, Object>()
        project.userLayers().each { user ->
            def item = [:]
            item.id = user.id
            item.key = user.firstname + " " + user.lastname
            item.terms = []
            terms.each { term ->
                def t = [:]
                t.id = term.id
                t.name = term.name
                t.color = term.color
                t.value = 0
                item.terms << t
            }
            result.put(user.id, item)
        }
        nbAnnotationsByUserAndTerms.each { stat ->
            def user = result.get(stat[0])
            if(user) {
                user.terms.each {
                    if (it.id == stat[1]) {
                        it.value = stat[2]
                    }
                }
            }
        }
        responseSuccess(result.values())
    }

    def statUser = {
        Project project = Project.read(params.id)
        if (project == null) { responseNotFound("Project", params.id) }
        def userAnnotations = Annotation.createCriteria().list {
            eq("project", project)
            join("user")  //right join possible ? it will be sufficient
            projections {
                countDistinct('id')
                groupProperty("user.id")
            }
        }
        Map<Long, Object> result = new HashMap<Long, Object>()
        project.userLayers().each { user ->
            def item = [:]
            item.id = user.id
            item.key = user.firstname + " " + user.lastname
            item.value = 0
            result.put(item.id, item)
        }
        userAnnotations.each { item ->
            def user = result.get(item[1])
            if(user) user.value = item[0]
        }

        responseSuccess(result.values())
    }

    def statTerm = {
        Project project = Project.read(params.id)
        if (project == null) responseNotFound("Project", params.id)

        def terms = project.ontology.terms()
        def annotations = project.annotations()
        def stats = [:]
        def color = [:]
        def ids = [:]
        def list = []

        //init list
        terms.each { term ->
            if (!term.hasChildren()) {
                stats[term.name] = 0
                color[term.name] = term.color
                ids[term.name] = term.id
            }
        }

        //compute stat
        annotations.each { annotation ->
            def termOfAnnotation = annotation.terms()
            termOfAnnotation.each { term ->
                if (term.ontology.id == project.ontology.id && !term.hasChildren())
                    stats[term.name] = stats[term.name] + 1
            }
        }
        stats.each {
            list << ["id": ids.get(it.key), "key": it.key, "value": it.value, "color": color.get(it.key)]
        }
        responseSuccess(list)
    }

    /* Pour chaque terme, le nombre de slides dans lesquels ils ont été annotés. */
    def statTermSlide = {
        Project project = Project.read(params.id)
        if (project == null) responseNotFound("Project", params.id)
        def terms = Term.findAllByOntology(project.getOntology())
        def userLayers = project.userLayers()
        def annotations = AnnotationTerm.createCriteria().list {
            inList("term", terms)
            inList("user", userLayers)
            join("annotation")
            createAlias("annotation", "a")
            projections {
                eq("a.project", project)
                groupProperty("a.image.id")
                groupProperty("term.id")
                count("term.id")
            }
        }
        Map<Long, Object> result = new HashMap<Long, Object>()
        terms.each { term ->
            def item = [:]
            item.id = term.id
            item.key = term.name
            item.value = 0
            result.put(item.id, item)
        }
        annotations.each { item ->
            def term = item[1]
            result.get(term).value++;
        }

        responseSuccess(result.values())
    }

    /*Pour chaque user, le nombre de slides dans lesquels ils ont fait des annotations.*/
    def statUserSlide = {
        Project project = Project.read(params.id)
        if (project == null) responseNotFound("Project", params.id)
        def terms = Term.findAllByOntology(project.getOntology())
        def annotations = AnnotationTerm.createCriteria().list {
            inList("term", terms)
            join("annotation")
            createAlias("annotation", "a")
            projections {
                eq("a.project", project)
                groupProperty("a.image.id")
                groupProperty("a.user")
                count("a.user")
            }
        }
        Map<Long, Object> result = new HashMap<Long, Object>()
        project.userLayers().each { user ->
            def item = [:]
            item.id = user.id
            item.key = user.firstname + " " + user.lastname
            item.value = 0
            result.put(item.id, item)
        }
        annotations.each { item ->
            def user = result.get(item[1].id)
            if(user) user.value++;
        }

        responseSuccess(result.values())
    }
}
