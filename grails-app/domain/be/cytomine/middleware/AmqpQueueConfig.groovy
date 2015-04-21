package be.cytomine.middleware

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

/**
 * Created by julien 
 * Date : 26/02/15
 * Time : 10:01
 */

@RestApiObject(name = "AMQP Queue Config", description = "Possible rabbitMQ configurations for any AMQP queue in the system")
class AmqpQueueConfig extends CytomineDomain implements Serializable {

    @RestApiObjectField(description = "The name of the parameter")
    String name

    @RestApiObjectField(description = "The default value for the parameter")
    String defaultValue

    @RestApiObjectField(description = "The position of the parameter in the createQueue method")
    Integer index

    @RestApiObjectField(description = "True if the parameter has to be inside the args map")
    Boolean isInMap

    @RestApiObjectField(description = "The parameter data type")
    String type


    static constraints = {
        name(blank: false, unique: true, matches: "[a-zA-Z0-9-_]+")
        defaultValue(nullable: true)
        index(blank: false)
        isInMap(blank : false)
        type(blank: false, inList: ["String", "Boolean", "Number"])
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
        sort "id"
        cache true
        isInMap defaultValue: "false"
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        AmqpQueueConfig.withNewSession {
            if(name) {
                AmqpQueueConfig amqpQueueConfigAlreadyExist = AmqpQueueConfig.findByName(name)
                if(amqpQueueConfigAlreadyExist && (amqpQueueConfigAlreadyExist.id != id))
                    throw new AlreadyExistException("The configuration '" + name + "' already exists!")
            }
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AmqpQueueConfig insertDataIntoDomain(def json, def domain = new AmqpQueueConfig()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id', null)
        domain.name = JSONUtils.getJSONAttrStr(json, 'name')
        /*if(!domain.defaultValue)*/
            domain.defaultValue = JSONUtils.getJSONAttrStr(json, 'defaultValue')
        domain.index = JSONUtils.getJSONAttrInteger(json, 'index', -1)
        domain.isInMap = JSONUtils.getJSONAttrBoolean(json, 'isInMap', false)
        domain.type = JSONUtils.getJSONAttrStr(json, 'type')
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
        returnArray['defaultValue'] = domain?.defaultValue
        returnArray['index'] = domain?.index
        returnArray['isInMap'] = domain?.isInMap
        returnArray['type'] = domain?.type
        return returnArray
    }

    String toString() {
        return "Name : " + this.name + ", default value : " + this.defaultValue + ", type : " + this.type
    }

}
