package be.cytomine.utils

/*
* Copyright (c) 2009-2015. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import be.cytomine.AnnotationDomain
import be.cytomine.test.HttpClient
import grails.converters.JSON

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
