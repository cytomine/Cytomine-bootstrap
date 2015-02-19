package be.cytomine.middleware

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.security.User
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

    @RestApiObjectField(description = "The author of the message broker server")
    User user

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
        domain.user = JSONUtils.getJSONAttrDomain(json, "user", new User(), true)
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
        returnArray['user'] = domain?.user?.id
        return returnArray
    }

}
