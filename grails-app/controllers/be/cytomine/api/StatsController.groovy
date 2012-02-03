package be.cytomine.api

import be.cytomine.ontology.Annotation
import be.cytomine.ontology.AnnotationTerm
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.security.UserJob
import be.cytomine.processing.structure.ConfusionMatrix
import be.cytomine.security.SecUser

class StatsController extends RestController {

    def annotationService
    def termService


    def statRetrievalsuggestion = {
        //Get project
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }

        //Get all userjob from project
        //List<UserJob> allUserJob = UserJob.list()
        def users = AnnotationTerm.createCriteria().list {
            join("annotation")
            join("user")
            createAlias("annotation", "a")
            eq("a.project", project)
            eq("algo", true)
            //inList("user", allUserJob)
            projections {
                groupProperty('user')
            }
        }

        //Get last userJob
        SecUser lastUserJob = null
        println "****************************"
        users.each {
            if(!lastUserJob || lastUserJob.created<it.created)  {
                lastUserJob = it
            }
        }
        println "****************************"
        println "Last: "+ lastUserJob?.username
        println "****************************"

        //Get all data from this job
        def annotationsTerm = AnnotationTerm.findAllByUser(lastUserJob, [sort: "rate", order: "desc"]);

        //Get all term from this project, structure: [idterm,i,...]



        double avg = computeAVG(annotationsTerm)
        println "AVG="+avg

        ConfusionMatrix matrix = computeConfusionMatrix(termService.list(project),annotationsTerm)
        matrix.print()
        String matrixJSON =  matrix.toJSON()

        def worstTerms = listWorstTerm(annotationsTerm,project)

        def worstAnnotations = listWorstAnnotationTerm(annotationsTerm,30)


        def data = ['avg':avg, 'matrix':matrixJSON, "worstTerms":worstTerms, "worstAnnotations":worstAnnotations ]
        responseSuccess(data)
    }

    private double computeAVG(def annotationsTerm){
        long correct=0
        long totalWithTerm=0

        annotationsTerm.each {
            if(it.term) {
                totalWithTerm++
                if(it.term==it.expectedTerm) correct++
            }

        }
        double avg = (double)(correct/totalWithTerm)
        return avg
    }

    private ConfusionMatrix computeConfusionMatrix(def projectTerms, def annotationsTerm){
        def projectTermsId = projectTerms.collect {it.id+""}
        ConfusionMatrix matrix = new ConfusionMatrix(projectTermsId);

        annotationsTerm.each {
            if(it.term && it.expectedTerm) matrix.addEntry(it.termId+"",it.expectedTermId+"")
            //matrix.print()
        }
        println matrix.getDiagonalSum()
        println matrix.getTotalSum()
        println (matrix.getDiagonalSum()/matrix.getTotalSum())
        return matrix
    }

    private def listWorstTerm(def annotationsTerms, Project project) {
        Map<Term, Integer> termMap = new HashMap<Term, Integer>()
        List<Term> termList = termService.list(project)
        termList.each {termMap.put(it, 0)}

        annotationsTerms.each {
            if (it.term && it.expectedTerm && it.term!=it.expectedTerm) {
                Term term = it.term
                termMap.put(term, termMap.get(term) + 1);
            }
        }
        termList.clear()
        termMap.each {  key, value ->
            key.rate = value
            termList.add(key)
        }
        return termList
    }

    private def listWorstAnnotationTerm(def annotationsTerms,def max) {
       def results = []

        for (int i = 0; i < annotationsTerms.size() && max > results.size(); i++) {
            def suggest = annotationsTerms.get(i)
            def annotation = suggest.annotation
            def realTerm = termService.list(annotation,annotation.user)
            println "id="+suggest.id
            println "annotation="+annotation.id
            println "realTerm="+realTerm
            println "suggest.expectedTermId="+suggest.termId
            println "realTerm.contains(suggest.expectedTermId)="+realTerm.contains(suggest.termId)
            if (suggest.term && suggest.expectedTerm && !realTerm.contains(suggest.termId)) {
                results.add(suggest);
            }
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
