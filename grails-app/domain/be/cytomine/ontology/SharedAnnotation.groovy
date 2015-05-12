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
import be.cytomine.security.User

/**
 * A shared annotation is a comment on a specific annotation
 * (e.g. is it the good term?, ...)
 * Receiver user can see the comment and add answer
 */
class SharedAnnotation extends CytomineDomain {

    /**
     * User that ask the question
     */
    User sender

    /**
     * Comment that will be share with other user
     */
    String comment

    /**
     * Only user annotation for now (not reviewed/algo annotation)
     */
    UserAnnotation userAnnotation

    static hasMany = [receivers : User]

    static constraints = {
        comment(type: 'text', nullable: true)
    }
    
    String toString() {
        "Annotation " + userAnnotation + " shared by " + sender
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['comment'] = domain?.comment
        returnArray['sender'] = domain?.sender?.toString()
        returnArray['userannotation'] = domain?.userAnnotation?.id
        returnArray['receivers'] = domain?.receivers?.collect { it.toString() }
        returnArray
    }
}
