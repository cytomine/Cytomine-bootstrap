package be.cytomine.middleware

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
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

/**
 * Created by julien 
 * Date : 24/02/15
 * Time : 14:36
 */
@RestApiObject(name = "AMQP Queue", description = "A queue that supports Advanced Message Queuing Protocol")
class AmqpQueue extends CytomineDomain implements Serializable {

    @RestApiObjectField(description = "The name of the queue")
    String name

    @RestApiObjectField(description = "The host of the queue")
    String host

    @RestApiObjectField(description = "The name of the exchange bound to the queue")
    String exchange

    static constraints = {
        name(blank: false, unique: true)
        host(blank: false)
        exchange(blank: false, unique: true)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
        cache true
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        AmqpQueue.withNewSession {
            if(name) {
                AmqpQueue amqpQueueAlreadyExist = AmqpQueue.findByName(name)
                if(amqpQueueAlreadyExist && (amqpQueueAlreadyExist.id != id))
                    throw new AlreadyExistException("The queue " + name + " already exists!")
            }
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AmqpQueue insertDataIntoDomain(def json, def domain = new AmqpQueue()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id', null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name')
        domain.host = JSONUtils.getJSONAttrStr(json, 'host')
        domain.exchange = JSONUtils.getJSONAttrStr(json, 'exchange')
        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['name'] = domain?.name
        returnArray['host'] = domain?.host
        returnArray['exchange'] = domain?.exchange
        return returnArray
    }


}
