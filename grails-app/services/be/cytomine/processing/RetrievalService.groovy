package be.cytomine.processing

import be.cytomine.image.server.RetrievalServer
import be.cytomine.ontology.Annotation
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.test.HttpClient
import be.cytomine.utils.ValueComparator
import grails.converters.JSON
import groovyx.gpars.Asynchronizer
import java.util.concurrent.Future
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.ontology.Ontology
import be.cytomine.ontology.OntologyService
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.model.NotFoundException

class RetrievalService {

    static transactional = true
    def projectService

    /**
     * Search similar annotation and best term for an annotation
     * @param project project which will provide annotation learning set
     * @param annotation annotation to search
     * @return [annotation: #list of similar annotation#, term: #map with best term#]
     * @throws Exception
     */
    def listSimilarAnnotationAndBestTerm(Project project, Annotation annotation) throws Exception {

        def data = [:]
        //Get similar annotation
        def similarAnnotations = loadAnnotationSimilarities(annotation)
        data.annotation = similarAnnotations

        //Get all term from project
        def projectTerms = project.ontology.terms()
        def bestTermNotOrdered = getTermMap(projectTerms)
        ValueComparator bvc = new ValueComparator(bestTermNotOrdered);

        //browse annotation
        similarAnnotations.each { similarAnnotation ->
            //for each annotation, browse annotation terms
            def terms = similarAnnotation.terms()
            terms.each { term ->
                if (projectTerms.contains(term)) {
                    Double oldValue = bestTermNotOrdered.get(term)
                    //for each term, add similarity value
                    bestTermNotOrdered.put(term, oldValue + similarAnnotation.similarity)
                }
            }
        }

        //Sort [term:rate] by rate (desc)
        TreeMap<Term, Double> bestTerm = new TreeMap(bvc);
        bestTerm.putAll(bestTermNotOrdered)
        def bestTermList = []

        //Put them in a list
        for (Map.Entry<Term, Double> entry: bestTerm.entrySet()) {
            Term term = entry.getKey()
            term.rate = entry.getValue()
            bestTermList << term
        }
        data.term = bestTermList
        return data
    }

    def getTermMap(List<Term> termList) {
        def map = [:]
        termList.each {
            map.put(it, 0d)
        }
        map
    }

    private def loadAnnotationSimilarities(Annotation searchAnnotation) {
        log.info "get similarities for annotation " + searchAnnotation.id
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String URL = server.url + "/search.json"

        def json = JSON.parse(getPostSearchResponse(URL, searchAnnotation))
        def data = []

        for (int i = 0; i < json.length(); i++) {
            def annotationjson = json.get(i)  //{"id":6754,"url":"http://beimport java.util.concurrent.Futureta.cytomine.be:48/api/annotation/6754/crop.jpg","sim":6.922589484181173E-6},{"id":5135,"url":"http://beta.cytomine.be:48/api/annotation/5135/crop.jpg","sim":6.912057598973113E-6}]

            try {
                Annotation annotation = Annotation.read(annotationjson.id)
                if (annotation && annotation.id != searchAnnotation.id) {
                    projectService.checkAuthorization(annotation.project)
                    annotation.similarity = new Double(annotationjson.sim)
                    data << annotation
                }
            }
            catch(AccessDeniedException ex) {log.info "User cannot have access to this annotation"}
            catch(NotFoundException ex) {log.info "User cannot have access to this annotation"}
        }
        return data
    }

    public String getPostSearchResponse(String URL, Annotation annotation) {
        HttpClient client = new HttpClient();
        client.connect(URL, "xxx", "xxx");

        //Get project id with same ontology
        List<Long> projectWithSameOntology = projectService.list( annotation.project.ontology).collect {it.id}

//        def json = ["id": annotation.id, "url" : annotation.getCropURL(), "container" : projectWithSameOntology]
//        println json.toString()

        def json = JSON.parse(annotation.encodeAsJSON())
        json.containers = projectWithSameOntology
        println json.toString()
        client.post(json.toString())
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return response
    }

    public static String getPostResponse(String URL, Annotation annotation) {
        HttpClient client = new HttpClient();
        client.connect(URL, "xxx", "xxx");
        client.post(annotation.encodeAsJSON())
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return response
    }

    public static String getDeleteResponse(String URL) {
        HttpClient client = new HttpClient();
        client.connect(URL, "xxx", "xxx");
        client.delete()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return response
    }

    public static def indexAnnotationSynchronous(Annotation annotation) {
        println "index synchronous"
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String URL = server.url + "/index.json"
        getPostResponse(URL, annotation)
    }

    public static def indexAnnotationSynchronous(Long id) {
        println "index synchronous"
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String URL = server.url + "/index.json"
        getPostResponse(URL, Annotation.read(id))
    }

    public static def deleteAnnotationSynchronous(Long id) {
        println "delete synchronous"
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String URL = server.url + "/" + id + ".json"
        getDeleteResponse(URL)
    }

    public static def deleteContainerSynchronous(Long id) {
        println "delete container synchronous"
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String URL = server.url + "/container/" + id + ".json"
        getDeleteResponse(URL)
    }

    public static def updateAnnotationSynchronous(Long id) {
        println "update synchronous"
        deleteAnnotationSynchronous(id)
        indexAnnotationSynchronous(id)
    }

    public static def indexAnnotationAsynchronous(Annotation annotation) {
        //indexAnnotationSynchronous(annotation)
        println "index asynchronous"
        Asynchronizer.withAsynchronizer() {
            Closure indexAnnotation = {try {indexAnnotationSynchronous(annotation)} catch (Exception e) {e.printStackTrace()}}
            Closure annotationIndexing = indexAnnotation.async()  //create a new closure, which starts the original closure on a thread pool
            Future result = annotationIndexing()
        }
    }

    public static def deleteAnnotationAsynchronous(Long id) {
        println "delete asynchronous"
        Asynchronizer.withAsynchronizer() {
            Closure deleteAnnotation = {
                try {
                    deleteAnnotationSynchronous(id)
                } catch (Exception e) {e.printStackTrace()}
            }
            Closure annotationDeleting = deleteAnnotation.async()  //create a new closure, which starts the original closure on a thread pool
            Future result = annotationDeleting()
        }
    }

    public static def deleteContainerAsynchronous(Long id) {
        println "delete asynchronous"
        Asynchronizer.withAsynchronizer() {
            Closure deleteContainer = {
                try {
                    deleteContainerSynchronous(id)
                } catch (Exception e) {e.printStackTrace()}
            }
            Closure containerDeleting = deleteContainer.async()  //create a new closure, which starts the original closure on a thread pool
            Future result = containerDeleting()
        }
    }


    public static def updateAnnotationAsynchronous(Long id) {
        println "update asynchronous"
        Asynchronizer.doParallel() {
            Closure deleteAnnotation = {
                try {
                    deleteAnnotationSynchronous(id)
                    indexAnnotationSynchronous(id)
                } catch (Exception e) {e.printStackTrace()}
            }
            Closure annotationUpdating = deleteAnnotation.async()  //create a new closure, which starts the original closure on a thread pool
            Future result = annotationUpdating()
        }
    }


    public void indexMissingAnnotation() {
        //Get indexed resources
        List<Long> resources = getIndexedResource()
        //Check if each annotation is well indexed
        def annotations = Annotation.list()
        int i = 1
        annotations.each { annotation ->
            log.debug "Annotation $i/" + annotations.size()
            if (!resources.contains(annotation.id)) {
                log.debug "Annotation $annotation.id IS NOT INDEXED"
                try {indexAnnotationSynchronous(annotation)} catch (Exception e) {e.printStackTrace()}
            } else {
                log.debug "Annotation $annotation.id IS INDEXED"
            }
            i++
        }
    }

    List<Long> getIndexedResource() {
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String URL = server.url + ".json"
        List json = JSON.parse(getGetResponse(URL))
        List<Long> resources = new ArrayList<Long>()
        json.each { subserver ->
            subserver.each { resource ->
                log.debug "resource=" + Long.parseLong(resource.key)
                resources.add(Long.parseLong(resource.key))
            }
        }
        resources
    }

    public static String getGetResponse(String URL) {
        HttpClient client = new HttpClient();
        client.connect(URL, "xxx", "xxx");
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return response
    }
}
