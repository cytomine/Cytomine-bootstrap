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
 * Date : 03/03/15
 * Time : 09:03
 */

@RestApiObject(name = "AMQP Queue Config Instance", description = "A real configuration value for a specific queue")
class AmqpQueueConfigInstance extends CytomineDomain implements Serializable{

    @RestApiObjectField(description = "The queue that will be created with a specific value for a specific parameter")
    AmqpQueue queue

    @RestApiObjectField(description = "The overridden parameter")
    AmqpQueueConfig config

    @RestApiObjectField(description = "The value of the overridden parameter")
    String value


    static constraints = {
        queue(blank: false)
        config(blank: false)
        value(nullable: true)
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
        AmqpQueueConfigInstance.withNewSession {
            AmqpQueueConfigInstance amqpQueueConfigInstance = AmqpQueueConfigInstance.findByQueueAndConfig(queue, config)
            if(amqpQueueConfigInstance && (amqpQueueConfigInstance.id != id))
                throw new AlreadyExistException("The configuration instance '" + name + "' already exists!")
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AmqpQueueConfigInstance insertDataIntoDomain(def json, def domain = new AmqpQueueConfigInstance()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id', null)
        domain.queue = JSONUtils.getJSONAttrDomain(json, "queue", new AmqpQueue(), true)
        domain.config = JSONUtils.getJSONAttrDomain(json, "config", new AmqpQueueConfig(), true)
        domain.value = JSONUtils.getJSONAttrStr(json, 'value')
        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['queue'] = domain?.queue?.id
        returnArray['config'] = domain?.config?.id
        returnArray['value'] = domain?.value
        return returnArray
    }

}
