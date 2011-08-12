package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.test.HttpClient
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.ontology.Annotation
import groovyx.gpars.Asynchronizer
import java.util.concurrent.Future

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
        println "get similarities"
        String URL = "http://localhost:8090/retrieval-web/api/resource/search.json"
        HttpClient client = new HttpClient();
        client.connect(URL,"xxx","xxx");
        client.post(Annotation.read(idAnnotation).encodeAsJSON())
        int code  = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();

        println ("check response, code="+code)

        def json = JSON.parse(response)
        assert json instanceof JSONArray
        def data = []

        //TODO: for perf => getAll(allID)?
        for(int i=0;i<json.length();i++) {
            def annotationjson = json.get(i)  //{"id":6754,"url":"http://beta.cytomine.be:48/api/annotation/6754/crop.jpg","sim":6.922589484181173E-6},{"id":5135,"url":"http://beta.cytomine.be:48/api/annotation/5135/crop.jpg","sim":6.912057598973113E-6}]
            println annotationjson
            Annotation annotation = Annotation.read(annotationjson.id)
            println "read annotation " + annotationjson.id + " = " + annotation
            annotation.similarity = new Double(annotationjson.sim)
            data << annotation
        }
        return data
    }

    public static def indexAnnotationSynchronous(Annotation annotation) {
        println "index annotation synchron"
        String URL = "http://localhost:8090/retrieval-web/api/resource.json"
        HttpClient client = new HttpClient();
        client.connect(URL,"xxx","xxx");
        client.post(annotation.encodeAsJSON());
        int code  = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
    }

    public static def indexAnnotationAsynchronous(Annotation annotation) {
        println "index annotation asynchron"
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



}
