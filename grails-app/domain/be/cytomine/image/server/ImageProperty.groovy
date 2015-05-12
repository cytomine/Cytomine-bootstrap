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

import be.cytomine.image.AbstractImage

/**
 * Property (key-value) map with an image
 */
class ImageProperty {

    String key
    String value
    AbstractImage image

    static constraints = {
        key(nullable: false, empty: false)
        value(nullable: false, empty: false)
        image(nullable: false)
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static def getDataFromDomain(def prop) {
        def returnArray = [:]
        returnArray["id"] = prop?.id
        returnArray["key"] = prop?.key
        returnArray["value"] = prop?.value
        returnArray
    }
}
