package be.cytomine.image.multidim

import be.cytomine.CytomineDomain
import be.cytomine.Exception.AlreadyExistException
import be.cytomine.Exception.WrongArgumentException
import be.cytomine.image.ImageInstance
import be.cytomine.utils.JSONUtils
import org.restapidoc.annotation.RestApiObjectField
import org.restapidoc.annotation.RestApiObjectFields
import org.restapidoc.annotation.RestApiObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 18/05/11
 * Time: 8:33
 * A position of an image in its group
 */
@RestApiObject(name = "image sequence", description = "A position of an image in the image group")
class ImageSequence extends CytomineDomain implements Serializable {

    @RestApiObjectField(description = "The image")
    ImageInstance image

    @RestApiObjectField(description = "The image channel", mandatory = false)
    Integer channel

    @RestApiObjectField(description = "The image zStack", mandatory = false)
    Integer zStack

    @RestApiObjectField(description = "The image slice", mandatory = false)
    Integer slice

    @RestApiObjectField(description = "The image time", mandatory = false)
    Integer time

    @RestApiObjectField(description = "The image group")
    ImageGroup imageGroup

    @RestApiObjectFields(params=[
        @RestApiObjectField(apiFieldName = "model", description = "The image instance full data (see image instance for more details)",allowedType = "domain",useForCreation = false)
    ])
    static transients = []

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
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def domain) {
        def returnArray = CytomineDomain.getDataFromDomain(domain)
        returnArray['image'] = domain?.image?.id
        returnArray['zStack'] = domain?.zStack
        returnArray['slice'] = domain?.slice
        returnArray['time'] = domain?.time
        returnArray['channel'] = domain?.channel
        returnArray['imageGroup'] = domain?.imageGroup?.id
        returnArray['model'] = domain?.image
        return returnArray
    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain container() {
        return imageGroup.container();
    }
}
