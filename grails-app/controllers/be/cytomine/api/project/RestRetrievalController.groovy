package be.cytomine.api.project

import be.cytomine.api.RestController
import be.cytomine.test.HttpClient
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import be.cytomine.ontology.Annotation

class RestRetrievalController extends RestController {

    def search = {
        log.info "List with id annotation:"+params.idannotation
        def data = loadAnnotationSimilarities(params.idannotation)
        //def stats = loadStatsSimilarities(data)
        //data << stats
        responseSuccess(data)
    }

    private def loadAnnotationSimilarities(def idAnnotation) {
        println "get similarities"
        String URL = "http://localhost:8090/retrieval-web/annotation/"+idAnnotation+".json"
        HttpClient client = new HttpClient();
        client.connect(URL,"xxx","xxx");
        client.get()
        int code  = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();

        println ("check response, code="+code)

        def json = JSON.parse(response)
        assert json instanceof JSONArray
        def data = []
        for(int i=0;i<json.length();i++) {
            def annotationjson = json.get(i)  //{"id":6754,"url":"http://beta.cytomine.be:48/api/annotation/6754/crop.jpg","sim":6.922589484181173E-6},{"id":5135,"url":"http://beta.cytomine.be:48/api/annotation/5135/crop.jpg","sim":6.912057598973113E-6}]
            Annotation annotation = Annotation.read(annotationjson.id)
            println "read annotation " + annotationjson.id + " = " + annotation
            annotation.similarity = new Double(annotationjson.sim)
            data << annotation
        }
        return data
    }

    /*private def loadStatsSimilarities(def annotations) {
        println "get stats"
        def stats = [:]
        annotations.each { annotation ->
            stats = incrementTermStats(stats,annotation.terms(),annotation.similarity)
        }
        return stats;
    }

    private def incrementTermStats(def stats, def terms, double similarity) {
        terms.each { term ->
            if(stats[(term.id)]) stats[(term.id)]=stats[(term.id)]+similarity
            else stats[(term.id)] = similarity
        }
        return stats;
    } */


}
