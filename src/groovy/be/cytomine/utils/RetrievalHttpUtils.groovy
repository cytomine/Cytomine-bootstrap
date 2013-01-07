package be.cytomine.utils

import be.cytomine.AnnotationDomain
import groovyx.net.http.HTTPBuilder
import grails.converters.JSON

import static groovyx.net.http.Method.POST
import groovyx.net.http.ContentType
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * User: lrollus
 * Date: 7/01/13
 * GIGA-ULg
 * 
 */
class RetrievalHttpUtils {

    private static Log log = LogFactory.getLog(RetrievalHttpUtils.class)

    public static String getPostSearchResponse(String URL) {
        println "getPostSearchResponse not mocked"
    }

    public static String getPostSearchResponse(String URL, String resource, AnnotationDomain annotation, String urlAnnotation, List<Long> projectsSearch) {
        println "getPostSearchResponse not mocked"
        return
        def http = new HTTPBuilder(URL)
        http.auth.basic 'xxx', 'xxx'
        def params = ["id": annotation.id, "url": urlAnnotation, "containers": projectsSearch]
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

    public static void print() {
        println "RetrievalHttpUtils.println"
    }

}
