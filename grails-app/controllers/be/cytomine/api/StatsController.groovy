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

class StatsController extends RestController {

    def annotationService
    def termService

    def statRetrievalAVG = {
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }
        SecUser lastUserJob = getLastUserJob(project)
        double avg = computeAVG(lastUserJob)
        def data = ['avg': avg]
        responseSuccess(data)
    }

    def statRetrievalConfusionMatrix = {
        def data = []
        println "statRetrievalConfusionMatrix"
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }
        SecUser lastUserJob = getLastUserJob(project)
        if (lastUserJob) {
            ConfusionMatrix matrix = computeConfusionMatrix(termService.list(project), lastUserJob)
            String matrixJSON = matrix.toJSON()
            data = ['matrix': matrixJSON]
        }
        responseSuccess(data)
    }

    def statRetrievalWorstTerm = {
        def data = []
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }
        SecUser lastUserJob = getLastUserJob(project)
        if (lastUserJob) {
            def worstTerms = listWorstTerm(lastUserJob, project)
            data = ['worstTerms': worstTerms]
        }
        responseSuccess(data)
    }

    def statWorstTermWithSuggestedTerm = {
        def data = []
        log.info "statWorstTermWithSuggestedTerm"
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }
        SecUser lastUserJob = getLastUserJob(project)
        if (lastUserJob) {
            def worstTerms = listWorstTermWithSuggestedTerm(lastUserJob, project)
            data = ['worstTerms': worstTerms]
        }
        responseSuccess(data)
    }

    def statRetrievalWorstAnnotation = {
        def data = []
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }
        SecUser lastUserJob = getLastUserJob(project)
        if (lastUserJob) {
            def worstTerms = listWorstAnnotationTerm(lastUserJob, 30)
            data = ['worstAnnotations': worstTerms]
        }

        responseSuccess(data)
    }

    def statRetrievalEvolution = {
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }
        def evolution = listAVGEvolution(project)
        def data = ['evolution': evolution]
        responseSuccess(data)
    }

    private SecUser getLastUserJob(Project project) {
        def users = AlgoAnnotationTerm.createCriteria().list {
            createAlias("userJob", "u")
            eq("project", project)
            join("u")
            order "u.created", "desc"
            projections {
                groupProperty('userJob')
                groupProperty('u.created')
            }
        }
        return users.isEmpty() ? null : users.first().first()
    }

    private double computeAVG(def userJob) {
        long correct = 0
        long totalWithTerm = 0
        def algoAnnotationsTerm = AlgoAnnotationTerm.findAllByUserJob(userJob);
        algoAnnotationsTerm.each {
            if (it.term) {
                totalWithTerm++
                if (it.term == it.expectedTerm) correct++
            }
        }
        double avg = (double) (correct / totalWithTerm)
        return avg
    }

    private ConfusionMatrix computeConfusionMatrix(def projectTerms, def userJob) {
        def projectTermsId = projectTerms.collect {it.id + ""}
        println projectTermsId
        ConfusionMatrix matrix = new ConfusionMatrix(projectTermsId);
        def algoAnnotationsTerm = AlgoAnnotationTerm.findAllByUserJob(userJob);

        algoAnnotationsTerm.each {
            if (it.term && it.expectedTerm) matrix.addEntry(it.getIdExpectedTerm() + "", it.getIdTerm() + "")
            //matrix.print()
        }
        println matrix.getDiagonalSum()
        println matrix.getTotalSum()
        println(matrix.getDiagonalSum() / matrix.getTotalSum())
        return matrix
    }

    private def listWorstTerm(def userJob, Project project) {
        Map<Term, Integer> termMap = new HashMap<Term, Integer>()
        List<Term> termList = termService.list(project)
        termList.each {termMap.put(it, 0)}

        def algoAnnotationsTerm = AlgoAnnotationTerm.createCriteria().list {
            eq("userJob", userJob)
            neProperty("term", "expectedTerm")
        }

        algoAnnotationsTerm.each {
            termMap.put(it.term, termMap.get(it.term) + 1);
        }
        termList.clear()
        termMap.each {  key, value ->
            key.rate = value
            termList.add(key)
        }
        return termList
    }

    private def listWorstTermWithSuggestedTerm(def userJob, Project project) {
        TreeMap<Long, TreeMap<Long, Integer>> termMap = new TreeMap<Long, TreeMap<Long, Integer>>()
        List<Term> termList = termService.list(project)
        log.info "listWorstTermWithSuggestedTerm:" + termList.size()
        termList.each {
            log.info "add term " + it + " class=" + it.class
            termMap.put(it.id, new TreeMap<Long, Integer>())

        }
        log.info "termList"

        def algoAnnotationsTerm = AlgoAnnotationTerm.createCriteria().list {
            eq("userJob", userJob)
            //neProperty("term","expectedTerm")
        }

        algoAnnotationsTerm.each {
            TreeMap<Long, Integer> subMap = termMap.get(it.getIdExpectedTerm());
            log.info it.getIdTerm()
            log.info it.getIdExpectedTerm()
            log.info subMap
            Integer oldValue = subMap.get(it.getIdTerm())
            if (!oldValue) oldValue = 0
            subMap.put(it.getIdTerm(), oldValue + 1)

            termMap.put(it.getIdExpectedTerm(), subMap)
            log.info "termMap= " + termMap
        }
        def data = [:]
        termMap.each {
            SortedSet<Entry<Long, Integer>> mapSorted = Utils.entriesSortedByValuesDesc(termMap.get(it.key));
            data[it.key] = []
            long sum = computeSumOfValue(mapSorted)



            mapSorted.each { entry ->
                //log.info "entry="+entry
                Map map = [:]
                map[entry.key] = Math.round(((entry.value/sum)*100))
                data[it.key].add(map)

            }
            ///log.info "mapSorted= " + mapSorted
        }
        log.info "data=" + data
        return data
    }

    private long computeSumOfValue(SortedSet<Entry<Long, Integer>> mapSorted) {
        long sum = 0
        mapSorted.each { entry ->
            sum = sum + entry.value
        }
        return sum
    }



    private def listWorstAnnotationTerm(def userJob, def max) {
        def results = []

        def algoAnnotationsTerm = AlgoAnnotationTerm.createCriteria().list {
            eq("userJob", userJob)
            neProperty("term", "expectedTerm")
            order "rate", "desc"
        }

        for (int i = 0; i < algoAnnotationsTerm.size() && max > results.size(); i++) {
            def suggest = algoAnnotationsTerm.get(i)
            def annotation = suggest.annotation
            def realTerm = termService.list(annotation, annotation.user)
            println "id=" + suggest.id
            println "annotation=" + annotation.id
            println "realTerm=" + realTerm
            println "suggest.expectedTermId=" + suggest.termId
            println "realTerm.contains(suggest.expectedTermId)=" + realTerm.contains(suggest.termId)
            results.add(suggest);
        }

        return results

    }

    private def listAVGEvolution(Project project) {
        //TODO: must be implemented :-)

        //Get all avg from all job for this projet

        //Sort by date!

        def data = [[date: 1325576699000, avg: 65], [date: 1326607200000, avg: 72], [date: 1328594400000, avg: 73], [date: 1330754400000, avg: 75]]
        return data
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
        project.users().each { user ->
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
            def item = result.get(stat[0])
            item.terms.each {
                if (it.id == stat[1]) {
                    it.value = stat[2]
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
        project.users().each { user ->
            def item = [:]
            item.id = user.id
            item.key = user.firstname + " " + user.lastname
            item.value = 0
            result.put(item.id, item)
        }
        userAnnotations.each { item ->
            result.get(item[1]).value = item[0]
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
        def annotations = AnnotationTerm.createCriteria().list {
            inList("term", terms)
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
        project.users().each { user ->
            def item = [:]
            item.id = user.id
            item.key = user.firstname + " " + user.lastname
            item.value = 0
            result.put(item.id, item)
        }
        annotations.each { item ->
            def user = item[1].id
            result.get(user).value++;
        }

        responseSuccess(result.values())
    }
}
