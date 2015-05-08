package be.cytomine.utils

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import org.restapidoc.annotation.RestApiObject
import org.restapidoc.annotation.RestApiObjectField

@RestApiObject(name = "config", description = "A key-value entry that save the global config of the application")
class Config extends CytomineDomain implements Serializable {

    @RestApiObjectField(description = "The property key")
    String key

    @RestApiObjectField(description = "The property value")
    String value

    static constraints = {
        key(blank: false, unique: true)
        value(blank: false)
    }
    static mapping = {
        id(generator: 'assigned', unique: true)
        value type: 'text'
        sort "id"
        cache true
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['key'] = domain?.key
        returnArray['value'] = domain?.value
        return returnArray
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static Config insertDataIntoDomain(def json, def domain = new Config()){
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.key = JSONUtils.getJSONAttrStr(json,'key')
        domain.value = JSONUtils.getJSONAttrStr(json,'value')

        return domain
    }

    public CytomineDomain container() {
        return this;
    }

}
