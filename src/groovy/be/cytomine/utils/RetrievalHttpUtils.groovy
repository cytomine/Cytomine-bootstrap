package be.cytomine.utils

import be.cytomine.AnnotationDomain
import grails.converters.JSON
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.log4j.Logger

import static groovyx.net.http.Method.DELETE
import static groovyx.net.http.Method.POST

/**
 * User: lrollus
 * Date: 7/01/13
 * GIGA-ULg
 * 
 */
class RetrievalHttpUtils {

    private static Log log = LogFactory.getLog(RetrievalHttpUtils.class)

    public static String getPostSearchResponse(String URL, String resource, AnnotationDomain annotation, String urlAnnotation, List<Long> projectsSearch) {
        println "getPostSearchResponse not mocked"
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

    public static String getPostResponse(String URL, String resource, def jsonStr) {
        println "getPostSearchResponse not mocked"
        def http = new HTTPBuilder(URL)
        http.auth.basic 'xxx', 'xxx'

        http.request(POST) {
            uri.path = resource
            send ContentType.JSON, jsonStr

            response.success = { resp, json ->
                log.info "RESPONSEX=${resp.statusLine}"
                return json.toString()
            }
            response.failure = { resp ->
                log.info "RESPONSEERRORX=${resp.statusLine}"
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

}
