package be.cytomine.image.server

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

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 5/02/13
 * Time: 11:40
 */
class ImageServerStorage {
    ImageServer imageServer
    Storage storage

    def getZoomifyUrl() {
        println "imageServer.url=${imageServer.url}"
        return imageServer.url + imageServer.service + "?zoomify=" + storage.getBasePath()
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static def getDataFromDomain(def is) {
        def returnArray = [:]
        returnArray['imageServer'] = is?.imageServer
        returnArray['storage'] = is?.storage
        returnArray
    }
}
