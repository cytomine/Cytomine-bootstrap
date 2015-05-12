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

/**
 * User: lrollus
 * Date: 2013/10/07
 * GIGA-ULg
 * This class implement all method to easily get/create/update/delete/manage AttachedFile to Cytomine with HTTP request during functional test
 */
class AttachedFileAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/attachedfile/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/attachedfile.json"
        return doGET(URL, username, password)
    }

    static def listByDomain(String domainClassName, Long domainIdent, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/domain/$domainClassName/$domainIdent/attachedfile.json"
        return doGET(URL, username, password)
    }

    static def download(Long id,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/attachedfile/${id}/download"
        return doGET(URL, username, password)
    }

    static def upload(String domainClassName, Long domainIdent, File file, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/attachedfile.json?domainClassName=$domainClassName&domainIdent=$domainIdent"
        return doPOSTUpload(URL,file,username,password)
    }
}
