package be.cytomine.image

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.security.Group
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Association between Image and Group.
 * All groups that have credential to access an image (view, add to project,...)
 */
class AbstractImageGroup extends CytomineDomain implements Serializable {

    AbstractImage abstractImage
    Group group

    static mapping = {
        id(generator: 'assigned', unique: true)
    }

    /**
     * Thanks to the json, create an new domain of this class
     * Set the new domain id to json.id value
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static AbstractImageGroup createFromDataWithId(json) {
        def domain = createFromData(json)
        try {domain.id = json.id} catch (Exception e) {}
        return domain
    }

    /**
     * Thanks to the json, create a new domain of this class
     * If json.id is set, the method ignore id
     * @param json JSON with data to create domain
     * @return The created domain
     */
    static AbstractImageGroup createFromData(def json) {
        def abstractImageGroup = new AbstractImageGroup()
        insertDataIntoDomain(abstractImageGroup, json)
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static AbstractImageGroup insertDataIntoDomain(def domain, def json) {
        domain.abstractImage = AbstractImage.get(json.abstractImage.toString())
        domain.group = Group.get(json.group.toString())
        return domain;
    }

    /**
     * Create callback metadata
     * Callback will be send whith request response when add/update/delete on this send
     * @return Callback for this domain
     */
    def getCallBack() {
        HashMap<String, Object> callback = new HashMap<String, Object>();
        callback.put("abstractImageID", this.abstractImage.id)
        callback.put("groupID", this.group.id)        
        callback.put("imageID", this.abstractImage.id)
        return callback
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + AbstractImageGroup.class)
        JSON.registerObjectMarshaller(AbstractImageGroup) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['abstractImage'] = it.abstractImage?.id
            returnArray['group'] = it.group?.id
            return returnArray
        }
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        AbstractImageGroup.withNewSession {
            if(abstractImage && group) {
                AbstractImageGroup aig = AbstractImageGroup.findByAbstractImageAndGroup(abstractImage,group)
                if(aig!=null && (aig.id!=id))  {
                    throw new AlreadyExistException("AbstractImageGroup with image=${abstractImage.id} and group ${group.id} already exist!")
                }
            }
        }
    }
}
