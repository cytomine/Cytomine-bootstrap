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
    def retrievalSuggestedTermJobService
    def retrievalEvolutionJobService


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
    
    
    def statAnnotationEvolution = {

        Project project = Project.read(params.id)
        if (project == null) responseNotFound("Project", params.id)
        int daysRange = params.daysRange!=null ? params.getInt('daysRange') : 1
        log.info "statAnnotationEvolution:"+  project.name
        def data = []
        int count = 0;

        //List<Annotation> annotations = Annotation.findAllByProject(project,[sort:'created', order:"desc"])
        def annotations = Annotation.executeQuery("select a.created from Annotation a where a.project = ? order by a.created desc", [project])
        log.info "annotations="+annotations

        Date creation = project.created
        //stop today
        Date current = new Date()
        
        while(current.getTime()>=creation.getTime()) {
            def item = [:]
            while(count<annotations.size()) {
                if(annotations.get(count).getTime()<current.getTime()) break;
                count++;
            }

            item.date = current.getTime()
            item.size = annotations.size()-count;
            data << item

            Calendar cal = Calendar.getInstance();
            cal.setTime(current);
            cal.add(Calendar.DATE, -daysRange);
            current = cal.getTime();
        }
        responseSuccess(data)
    }
}
