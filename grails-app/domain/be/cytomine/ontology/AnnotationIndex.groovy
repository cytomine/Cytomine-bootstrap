package be.cytomine.ontology

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

import be.cytomine.CytomineDomain
import be.cytomine.image.ImageInstance
import be.cytomine.security.SecUser
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

@RestApiObject(name = "annotation index", description="A index entry that store, for an image and a user, the number of annotation created/reviewed")
class AnnotationIndex implements Serializable {

    @RestApiObjectField(description = "The user criteria", useForCreation = false)
    SecUser user

    @RestApiObjectField(description = "The image criteria",useForCreation = false)
    ImageInstance image

    @RestApiObjectField(description = "The number of annotation added by the user (auto incr with trigger)",useForCreation = false)
    Long countAnnotation

    @RestApiObjectField(description = "The number of review added by the user (auto incr with trigger)",useForCreation = false)
    Long countReviewedAnnotation

    static constraints = {
    }

    static mapping = {
        id generator: "assigned"
        sort "id"
        cache false
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['user'] = domain?.user?.id
        returnArray['image'] = domain?.image?.id
        returnArray['countAnnotation'] = domain?.countAnnotation
        returnArray['countReviewedAnnotation'] = domain?.countReviewedAnnotation
        return returnArray
    }
}
