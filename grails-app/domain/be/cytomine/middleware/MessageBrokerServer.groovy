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
 * Created by jconfetti on 04/02/15.
 * An instance of a message broker.
 */
@RestApiObject(name = "message broker server", description = "An instance of a message broker.")
class MessageBrokerServer extends CytomineDomain implements Serializable{

    @RestApiObjectField(description = "The host of the message broker")
    String host

    @RestApiObjectField(description = "The port to which the message broker is connected")
    Integer port

    @RestApiObjectField(description = "The name of the message broker server")
    String name


    static constraints = {
        name(blank: false, unique: true)
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
        MessageBrokerServer.withNewSession {
            if(name) {
                MessageBrokerServer messageBrokerServerAlreadyExist = MessageBrokerServer.findByName(name)
                if(messageBrokerServerAlreadyExist && (messageBrokerServerAlreadyExist.id != id))
                    throw new AlreadyExistException("Message Broker Server " + name + " already exists!")
            }
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static MessageBrokerServer insertDataIntoDomain(def json, def domain = new MessageBrokerServer()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id', null)
        domain.host = JSONUtils.getJSONAttrStr(json, 'host')
        domain.port = JSONUtils.getJSONAttrInteger(json, 'port', null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name')
        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['host'] = domain?.host
        returnArray['port'] = domain?.port
        returnArray['name'] = domain?.name
        return returnArray
    }

}
