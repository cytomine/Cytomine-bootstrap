package be.cytomine.test.http

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

import be.cytomine.test.Infos

class SearchAPI extends DomainAPI {

    //LIST - Project ; Annotation ; Image
    static def listDomain(String keywords, String operator, String filter, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/search.json?${keywords? "keywords=$keywords" : ""}&operator=$operator" + (filter? "&filter=$filter": "")
        return doGET(URL, username, password)
    }

    //v2
    static def search(List<String> words, String domain, List<String> types, List<Long> idProjects,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/search-engine.json?&expr=${words.collect{URLEncoder.encode(it, "UTF-8")}.join(",")}"
        URL = URL + (domain? "&domain=$domain":"")
        URL = URL + (types? "&types=${types.join(",")}":"")
        URL = URL + (idProjects? "&projects=${idProjects.join(",")}":"")
        return doGET(URL, username, password)
    }
    static def searchResults(List<Long> ids, List<String> words, String domain, List<String> types, List<Long> idProjects , String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/search-result.json?&expr=${words.collect{URLEncoder.encode(it, "UTF-8")}.join(",")}&ids=${ids.join(",")}"
        URL = URL + (domain? "&domain=$domain":"")
        URL = URL + (types? "&types=${types.join(",")}":"")
        URL = URL + (idProjects? "&projects=${idProjects.join(",")}":"")
        return doGET(URL, username, password)
    }
}
