package be.cytomine.utils

import be.cytomine.AnnotationDomain
import be.cytomine.test.HttpClient
import grails.converters.JSON
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 7/01/13
 * GIGA-ULg
 * 
 */
@groovy.util.logging.Log
class RetrievalHttpUtils {

    public static String getPostSearchResponse(String URL, String resource, AnnotationDomain annotation, String urlAnnotation, List<Long> projectsSearch) {
        log.info "getPostSearchResponse1"
        HttpClient client = new HttpClient()
        def url = URL.replace("/retrieval-web/api/resource.json",resource)
        client.connect(url,'xxx','xxx')
        log.info url
        def params = ["id": annotation.id, "url": urlAnnotation, "containers": projectsSearch]
        def paramsJSON = params as JSON

        client.post(paramsJSON.toString())
        String response = client.getResponseData()
        int code = client.getResponseCode()
        log.info "code=$code response=$response"
        return response
    }

    public static String getPostResponse(String URL, String resource, def jsonStr) {
        log.info "getPostSearchResponse2"
        HttpClient client = new HttpClient()
        def url = URL.replace("/retrieval-web/api/resource.json",resource)
        client.connect(url,'xxx','xxx')
        client.post(jsonStr)
        String response = client.getResponseData()
        int code = client.getResponseCode()
        log.info "code=$code response=$response"
        return response
    }

    public static String getDeleteResponse(String URL, String resource) {
        HttpClient client = new HttpClient();
        client.connect(URL+resource,'xxx','xxx')
        client.delete()
        String response = client.getResponseData()
        int code = client.getResponseCode()
        return response
     }
}
