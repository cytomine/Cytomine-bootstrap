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
        RetrievalServer server = RetrievalServer.findByDescription("stevben-server")
        String URL = server.url + "/search.json"

        def json = JSON.parse(getResponse(URL,Annotation.read(idAnnotation)))

        def data = []

        //TODO: for perf => getAll(allID)?
        for(int i=0;i<json.length();i++) {
            def annotationjson = json.get(i)  //{"id":6754,"url":"http://beta.cytomine.be:48/api/annotation/6754/crop.jpg","sim":6.922589484181173E-6},{"id":5135,"url":"http://beta.cytomine.be:48/api/annotation/5135/crop.jpg","sim":6.912057598973113E-6}]
            Annotation annotation = Annotation.read(annotationjson.id)
            if(annotation && annotation.id!=Long.parseLong(idAnnotation)) {
                annotation.similarity = new Double(annotationjson.sim)
                data << annotation
            }
        }
        return data
    }

    public static String getResponse(String URL, Annotation annotation) {
        HttpClient client = new HttpClient();
        client.connect(URL,"xxx","xxx");
        client.post(annotation.encodeAsJSON())
        int code  = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        return response
    }

    public static def indexAnnotationSynchronous(Annotation annotation) {
        println "index synchronous"
        RetrievalServer server = RetrievalServer.findByDescription("stevben-server")
        String URL = server.url + "/index.json"
        getResponse(URL,annotation)
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
            //Future result=annotationIndexing()
        }
    }



}
