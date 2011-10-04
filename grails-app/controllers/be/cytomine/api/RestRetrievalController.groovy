package be.cytomine.api

import be.cytomine.api.RestController
import be.cytomine.test.HttpClient
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.ontology.Annotation
import groovyx.gpars.Asynchronizer
import java.util.concurrent.Future
import be.cytomine.image.server.RetrievalServer
import org.codehaus.groovy.grails.web.json.JSONElement

class RestRetrievalController extends RestController {

    def search = {
        log.info "List with id annotation:"+params.idannotation
        def data = loadAnnotationSimilarities(params.idannotation)
        responseSuccess(data)
    }

    def index = {
        log.info "index with id annotation:"+params.idannotation
        indexAnnotationAsynchronous(Annotation.read(params.idannotation))
        responseSuccess([])
    }

    private def loadAnnotationSimilarities(def idAnnotation) {
        log.info  "get similarities for annotation " +idAnnotation
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String URL = server.url + "/search.json"

        Annotation searchAnnotation = Annotation.read(idAnnotation);
        def json = JSON.parse(getPostResponse(URL,searchAnnotation))

        def data = []

        //TODO: for perf => getAll(allID)?
        for(int i=0;i<json.length();i++) {
            def annotationjson = json.get(i)  //{"id":6754,"url":"http://beta.cytomine.be:48/api/annotation/6754/crop.jpg","sim":6.922589484181173E-6},{"id":5135,"url":"http://beta.cytomine.be:48/api/annotation/5135/crop.jpg","sim":6.912057598973113E-6}]
            Annotation annotation = Annotation.read(annotationjson.id)
            if(annotation && annotation.id!=Long.parseLong(idAnnotation) && (annotation.image.getIdProject()==searchAnnotation.image.getIdProject())) {
                annotation.similarity = new Double(annotationjson.sim)
                data << annotation
            }
        }
        return data
    }

    public static String getPostResponse(String URL, Annotation annotation) {
        HttpClient client = new HttpClient();
        client.connect(URL,"xxx","xxx");
        client.post(annotation.encodeAsJSON())
        int code  = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return response
    }

    public static String getDeleteResponse(String URL) {
        HttpClient client = new HttpClient();
        client.connect(URL,"xxx","xxx");
        client.delete()
        int code  = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return response
    }

    public static def indexAnnotationSynchronous(Annotation annotation) {
        println "index synchronous"
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String URL = server.url + "/index.json"
        getPostResponse(URL,annotation)
    }

    public static def indexAnnotationSynchronous(Long id) {
        println "index synchronous"
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String URL = server.url + "/index.json"
        getPostResponse(URL,Annotation.read(id))
    }

     public static def deleteAnnotationSynchronous(Long id) {
        println "delete synchronous"
        RetrievalServer server = RetrievalServer.findByDescription("retrieval")
        String URL = server.url + "/" + id + ".json"
        getDeleteResponse(URL)
    }

     public static def updateAnnotationSynchronous(Long id) {
        println "update synchronous"
        deleteAnnotationSynchronous(id)
        indexAnnotationSynchronous(id)
    }

    public static def indexAnnotationAsynchronous(Annotation annotation) {
        println "index asynchronous"
        Asynchronizer.doParallel() {
            Closure indexAnnotation = {
                try {
                 indexAnnotationSynchronous(annotation)
                } catch(Exception e) {e.printStackTrace()}
            }
            Closure annotationIndexing = indexAnnotation.async()  //create a new closure, which starts the original closure on a thread pool
            Future result=annotationIndexing()
        }
    }

    public static def deleteAnnotationAsynchronous(Long id) {
        println "delete asynchronous"
        Asynchronizer.doParallel() {
            Closure deleteAnnotation = {
                try {
                 deleteAnnotationSynchronous(id)
                } catch(Exception e) {e.printStackTrace()}
            }
            Closure annotationIndexing = deleteAnnotation.async()  //create a new closure, which starts the original closure on a thread pool
            //Future result=annotationIndexing()
        }
    }


    public static def updateAnnotationAsynchronous(Long id) {
        println "update asynchronous"
        Asynchronizer.doParallel() {
            Closure deleteAnnotation = {
                try {
                 deleteAnnotationSynchronous(id)
                 indexAnnotationSynchronous(id)
                } catch(Exception e) {e.printStackTrace()}
            }
            Closure annotationIndexing = deleteAnnotation.async()  //create a new closure, which starts the original closure on a thread pool
            //Future result=annotationIndexing()
        }
    }

}
