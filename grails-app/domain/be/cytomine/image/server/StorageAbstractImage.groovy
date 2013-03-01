package be.cytomine.image.server

import be.cytomine.image.AbstractImage
import be.cytomine.CytomineDomain
import be.cytomine.security.Group
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * TODOSTEVBEN: doc
 */
class StorageAbstractImage extends CytomineDomain {

    Storage storage
    AbstractImage abstractImage

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static StorageAbstractImage insertDataIntoDomain(def json, def domain = new StorageAbstractImage()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.storage = JSONUtils.getJSONAttrDomain(json, 'storage', new Storage(), true)
        domain.abstractImage = JSONUtils.getJSONAttrDomain(json, 'abstractimage', new AbstractImage(), true)
        return domain
    }

    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + StorageAbstractImage.class)
        JSON.registerObjectMarshaller(StorageAbstractImage) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['created'] = it.created ? it.created.time.toString() : null
            returnArray['updated'] = it.updated ? it.updated.time.toString() : null
            returnArray['id'] = it.id
            returnArray['storage'] = it.storage.id
            returnArray['abstractimage'] = it.abstractImage.id
            return returnArray
        }
    }

    public Storage storageDomain() {
        storage
    }


}
