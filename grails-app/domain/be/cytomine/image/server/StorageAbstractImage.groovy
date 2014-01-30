package be.cytomine.image.server

import be.cytomine.CytomineDomain
import be.cytomine.image.AbstractImage
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import jsondoc.annotation.ApiObjectFieldLight
import org.apache.log4j.Logger
import org.jsondoc.core.annotation.ApiObject

@ApiObject(name = "storage abstract image", description="A link between a storage and some images")
class StorageAbstractImage extends CytomineDomain {

    @ApiObjectFieldLight(description = "The storage id")
    Storage storage

    @ApiObjectFieldLight(description = "The abstractimage id", apiFieldName = "abstractimage")
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
            getDataFromDomain(it)
        }
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['storage'] = domain?.storage?.id
        returnArray['abstractimage'] = domain?.abstractImage?.id
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return storage.container();
    }
}
