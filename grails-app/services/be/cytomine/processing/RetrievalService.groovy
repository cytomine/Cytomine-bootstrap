package be.cytomine.processing

import be.cytomine.Exception.ServerException
import be.cytomine.api.UrlApi
import be.cytomine.image.server.RetrievalServer
import be.cytomine.ontology.Term
import be.cytomine.project.Project
import be.cytomine.test.HttpClient
import be.cytomine.utils.ValueComparator
import grails.converters.JSON
import groovyx.gpars.Asynchronizer
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.model.NotFoundException


import static groovyx.net.http.Method.DELETE
import static groovyx.net.http.Method.POST
import org.apache.log4j.Logger

import be.cytomine.AnnotationDomain
import be.cytomine.ontology.UserAnnotation
import groovy.sql.Sql
import be.cytomine.ontology.Ontology

class RetrievalService {

    static transactional = true
    def projectService
    def grailsApplication
    def dataSource

    private long printTimeAndReset(long timestamp, String name) {
        println "$name=${System.currentTimeMillis()-timestamp}ms"
        return System.currentTimeMillis()
    }

    //=>imageinstance service
    public List<Long> getAllProjectId(Ontology ontology) {
        String request = "SELECT p.id FROM project p WHERE ontology_id="+ontology.id
        def data = []
        new Sql(dataSource).eachRow(request) {
            data << it[0]
        }
        return data
    }

    /**
     * Search similar annotation and best term for an annotation
     * @param project project which will provide annotation learning set
     * @param annotation annotation to search
     * @return [annotation: #list of similar annotation#, term: #map with best term#]
     * @throws Exception
     */
    def listSimilarAnnotationAndBestTerm(Project project, AnnotationDomain annotation) throws Exception {
        long start = System.currentTimeMillis()
        start = printTimeAndReset(start,"1")
        def data = [:]

        if(annotation.location.numPoints<3) {
            data.term = []
            return data
        }
        start = printTimeAndReset(start,"2")

        //find project used for retrieval
        List<Long> projectSearch = []
        if(project.retrievalDisable) return data
        else if(project.retrievalAllOntology)
            projectSearch=getAllProjectId(annotation.project.ontology)
        else projectSearch=project.retrievalProjects.collect {it.id}
        start = printTimeAndReset(start,"3")

        //Get similar annotation
        def similarAnnotations = loadAnnotationSimilarities(annotation,projectSearch)
        data.annotation = similarAnnotations
        start = printTimeAndReset(start,"4")

        //Get all term from project
        def projectTerms = project.ontology.terms()
        def bestTermNotOrdered = getTermMap(projectTerms)
        ValueComparator bvc = new ValueComparator(bestTermNotOrdered);
        start = printTimeAndReset(start,"5")

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
        start = printTimeAndReset(start,"6")

        //Sort [term:rate] by rate (desc)
        TreeMap<Term, Double> bestTerm = new TreeMap(bvc);
        bestTerm.putAll(bestTermNotOrdered)
        def bestTermList = []
        start = printTimeAndReset(start,"7")

        //Put them in a list
        for (Map.Entry<Term, Double> entry: bestTerm.entrySet()) {
            Term term = entry.getKey()
            term.rate = entry.getValue()
            bestTermList << term
        }
        start = printTimeAndReset(start,"8")
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

    def loadAnnotationSimilarities(AnnotationDomain searchAnnotation,List<Long> projectSearch) {
        long start = System.currentTimeMillis()
        start = printTimeAndReset(start,"a")
        log.info "get similarities for userAnnotation " + searchAnnotation.id + " on " + projectSearch
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        start = printTimeAndReset(start,"b")
        def response = getPostSearchResponse(server.url,'/retrieval-web/api/retrieval/search.json', searchAnnotation,projectSearch)
        start = printTimeAndReset(start,"c")
        def json = JSON.parse(response)
        start = printTimeAndReset(start,"d")
        def result =  readRetrievalResponse(searchAnnotation,json)
        start = printTimeAndReset(start,"e")
        return result
    }

    private def readRetrievalResponse(AnnotationDomain searchAnnotation,def responseJSON) {
        def data = []
        long start = System.currentTimeMillis()
        start = printTimeAndReset(start,"a")
        for (int i = 0; i < responseJSON.length(); i++) {
            def annotationjson = responseJSON.get(i)  //{"id":6754,"url":"http://beimport java.util.concurrent.Futureta.cytomine.be:48/api/annotation/6754/crop.jpg","sim":6.922589484181173E-6},{"id":5135,"url":"http://beta.cytomine.be:48/api/annotation/5135/crop.jpg","sim":6.912057598973113E-6}]

            try {
                UserAnnotation annotation = UserAnnotation.read(annotationjson.id)
                if (annotation && annotation.id != searchAnnotation.id) {
                    projectService.checkAuthorization(annotation.project)
                    annotation.similarity = new Double(annotationjson.sim)
                    data << annotation
                }
            }
            catch (AccessDeniedException ex) {log.info "User cannot have access to this userAnnotation"}
            catch (NotFoundException ex) {log.info "User cannot have access to this userAnnotation"}
        }
        start = printTimeAndReset(start,"b")
        return data
    }

    public String getPostSearchResponse(String URL, String resource, AnnotationDomain annotation,List<Long> projectsSearch) {

        def http = new HTTPBuilder(URL)
        http.auth.basic 'xxx', 'xxx'
        def params = ["id": annotation.id, "url": annotation.getCropUrl(grailsApplication.config.grails.serverURL), "containers": projectsSearch]
        def paramsJSON = params as JSON

        http.request(POST) {
            uri.path = resource
            send ContentType.JSON, paramsJSON.toString()

            response.success = { resp, json ->
                log.info "response succes: ${resp.statusLine}"
                return json.toString()
            }
            response.failure = { resp ->
                log.info "response error: ${resp.statusLine}"
                return ""
            }
        }
    }


    public static String getPostResponse(String URL, String resource, def jsonStr) {
        def http = new HTTPBuilder(URL)
        http.auth.basic 'xxx', 'xxx'

        http.request(POST) {
            uri.path = resource
            send ContentType.JSON, jsonStr

            response.success = { resp, json ->
                Logger.getLogger(this).info("response succes: ${resp.statusLine}")
                return json.toString()
            }
            response.failure = { resp ->
                Logger.getLogger(this).info("response error: ${resp.statusLine}")
                return ""
            }
        }
    }

    public static String getDeleteResponse(String URL, String resource) {

            def http = new HTTPBuilder(URL)
            http.auth.basic 'xxx', 'xxx'

            http.request(DELETE) {
                uri.path = resource

                response.success = { resp, json ->
                    Logger.getLogger(this).info("response succes: ${resp.statusLine}")
                    return json
                }
                response.failure = { resp ->
                    Logger.getLogger(this).info("response error: ${resp.statusLine}")
                    return ""
                }
            }
    }

    public static def indexAnnotationSynchronous(String json, String url) {
        Logger.getLogger(this).info("index synchronous json")
        Logger.getLogger(this).info("url = " + url)
        String res = "/retrieval-web/api/resource.json"
        getPostResponse(url, res, json)
    }

    public static def indexAnnotationSynchronous(Long id) {
        Logger.getLogger(this).info("index synchronous id")
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String res = "/retrieval-web/api/resource.json"
        getPostResponse(server.url, res, UserAnnotation.read(id))
    }

    public static def deleteAnnotationSynchronous(Long id) {
        Logger.getLogger(this).info("delete synchronous")
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String res = "/retrieval-web/api/resource/"+id+".json"
        getDeleteResponse(server.url,res)
    }

    public static def deleteContainerSynchronous(Long id) {
        Logger.getLogger(this).info("delete container synchronous")
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String res = "/retrieval-web/api/container/" + id + ".json"
        getDeleteResponse(server.url,res)
    }

    public static def updateAnnotationSynchronous(Long id) {
        Logger.getLogger(this).info("update synchronous")
        deleteAnnotationSynchronous(id)
        indexAnnotationSynchronous(id)
    }

    public static def indexAnnotationAsynchronous(AnnotationDomain annotation,RetrievalServer server) {
        //indexAnnotationSynchronous(annotation)
        Logger.getLogger(this).info("index asynchronous")
        String url = server.url
        def json = annotation.encodeAsJSON()

        Asynchronizer.withAsynchronizer() {
            Closure indexAnnotation = {
                try {
                indexAnnotationSynchronous(json,url)
            } catch (Exception e) {throw new ServerException("Retrieval Exception: "+e)}}
            Closure annotationIndexing = indexAnnotation.async()  //create a new closure, which starts the original closure on a thread pool
            annotationIndexing()
        }
    }

    public static def deleteAnnotationAsynchronous(Long id) {
        Logger.getLogger(this).info("delete asynchronous")
        Asynchronizer.withAsynchronizer() {
            Closure deleteAnnotation = {
                try {
                    deleteAnnotationSynchronous(id)
                } catch (Exception e) {e.printStackTrace()}
            }
            Closure annotationDeleting = deleteAnnotation.async()  //create a new closure, which starts the original closure on a thread pool
            annotationDeleting()
        }
    }

    public static def deleteContainerAsynchronous(Long id) {
        Logger.getLogger(this).info("delete asynchronous")
        Asynchronizer.withAsynchronizer() {
            Closure deleteContainer = {
                try {
                    deleteContainerSynchronous(id)
                } catch (Exception e) {e.printStackTrace()}
            }
            Closure containerDeleting = deleteContainer.async()  //create a new closure, which starts the original closure on a thread pool
            containerDeleting()
        }
    }


    public static def updateAnnotationAsynchronous(Long id) {
        Logger.getLogger(this).info("update asynchronous")
        Asynchronizer.doParallel() {
            Closure deleteAnnotation = {
                try {
                    deleteAnnotationSynchronous(id)
                    indexAnnotationSynchronous(id)
                } catch (Exception e) {e.printStackTrace()}
            }
            Closure annotationUpdating = deleteAnnotation.async()  //create a new closure, which starts the original closure on a thread pool
            annotationUpdating()
        }
    }


    public void indexMissingAnnotation() {
        //Get indexed resources
        List<Long> resources = getIndexedResource()
        //Check if each annotation is well indexed
        def annotations = UserAnnotation.list()
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
