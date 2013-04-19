package be.cytomine.image.multidim

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.image.ImageInstance
import be.cytomine.project.Project
import be.cytomine.utils.JSONUtils
import grails.converters.JSON
import org.apache.log4j.Logger

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * A position of an image in its group
 */
class ImageSequence extends CytomineDomain implements Serializable {

    ImageInstance image

    Integer channel
    Integer zStack
    Integer slice
    Integer time

    ImageGroup imageGroup

    static constraints = {
    }

    static mapping = {
        id generator: "assigned"
        sort "id"
    }

    /**
     * Check if this domain will cause unique constraint fail if saving on database
     */
    void checkAlreadyExist() {
        ImageSequence.withNewSession {
            ImageSequence imageAlreadyExist = ImageSequence.findByImageAndImageGroup(image,imageGroup)
            if (imageAlreadyExist != null && (imageAlreadyExist.id != id)) {
                throw new AlreadyExistException("ImageGroup with image=" + image.id + " and imageGroup=" + imageGroup.id + "  already exists")
            }
        }
    }

    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     */
    static ImageSequence insertDataIntoDomain(def json, def domain = new ImageSequence()) {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.created = JSONUtils.getJSONAttrDate(json, "created")
        domain.updated = JSONUtils.getJSONAttrDate(json, "updated")
        domain.image = JSONUtils.getJSONAttrDomain(json, "image", new ImageInstance(), true)
        domain.zStack =  JSONUtils.getJSONAttrInteger(json,"zStack",0)
        domain.slice =  JSONUtils.getJSONAttrInteger(json,"slice",0)
        domain.time =  JSONUtils.getJSONAttrInteger(json,"time",0)
        domain.channel =  JSONUtils.getJSONAttrInteger(json,"channel",0)
        domain.imageGroup = JSONUtils.getJSONAttrDomain(json, "imageGroup", new ImageGroup(), true)

        if(domain.image.project!=domain.imageGroup.project) {
            throw new WrongArgumentException("ImageInstance must have the same project as ImageGroup:"+domain.image.project.id+" <> "+ domain.imageGroup.project.id)
        }

        return domain;
    }

    /**
     * Define fields available for JSON response
     * This Method is called during application start
     */
    static void registerMarshaller() {
        Logger.getLogger(this).info("Register custom JSON renderer for " + ImageSequence.class)
        JSON.registerObjectMarshaller(ImageSequence) {
            def returnArray = [:]
            returnArray['class'] = it.class
            returnArray['id'] = it.id
            returnArray['image'] = it.image.id
            returnArray['zStack'] = it.zStack
            returnArray['slice'] = it.slice
            returnArray['time'] = it.time
            returnArray['channel'] = it.channel
            returnArray['imageGroup'] = it.imageGroup.id
            returnArray['created'] = it.created?.time?.toString()
            returnArray['updated'] = it.updated?.time?.toString()
            return returnArray
        }
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return imageGroup.container();
    }
}
